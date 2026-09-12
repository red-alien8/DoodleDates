package com.spycodedoodledates.ui.workspace

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spycodedoodledates.data.local.entities.NoteEntity
import com.spycodedoodledates.data.local.entities.StrokeEntity
import com.spycodedoodledates.data.repository.DiaryRepository
import com.spycodedoodledates.domain.model.DrawingMode
import com.spycodedoodledates.domain.model.DrawingPoint
import com.spycodedoodledates.notifications.NoteAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val repository: DiaryRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val monthKey: String = checkNotNull(savedStateHandle["monthKey"])

    private val _currentMode = MutableStateFlow(DrawingMode.PEN)
    val currentMode: StateFlow<DrawingMode> = _currentMode

    private val _currentColor = MutableStateFlow(Color.Black)
    val currentColor: StateFlow<Color> = _currentColor

    private val _currentWidth = MutableStateFlow(5f)
    val currentWidth: StateFlow<Float> = _currentWidth

    val strokes: StateFlow<List<StrokeEntity>> = repository.getStrokesForMonth(monthKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.getNotesForMonth(monthKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val undoStack = mutableStateListOf<Action>()
    val redoStack = mutableStateListOf<Action>()

    sealed class Action {
        data class AddStroke(val stroke: StrokeEntity) : Action()
        data class AddNote(val note: NoteEntity) : Action()
        data class DeleteNote(val note: NoteEntity) : Action()
        data class UpdateNote(val oldNote: NoteEntity, val newNote: NoteEntity) : Action()
    }

    fun setMode(mode: DrawingMode) {
        _currentMode.value = mode
    }

    fun setColor(color: Color) {
        _currentColor.value = color
    }

    fun setWidth(width: Float) {
        _currentWidth.value = width
    }

    fun addStroke(points: List<DrawingPoint>) {
        val stroke = StrokeEntity(
            monthKey = monthKey,
            type = if (_currentMode.value == DrawingMode.ERASER) "erase" else "pen",
            colorHex = String.format("#%06X", (0xFFFFFF and _currentColor.value.toArgb())),
            widthDp = _currentWidth.value,
            pointsJson = Json.encodeToString(points)
        )
        viewModelScope.launch {
            val id = repository.insertStroke(stroke)
            val strokeWithId = stroke.copy(id = id)
            undoStack.add(Action.AddStroke(strokeWithId))
            redoStack.clear()
        }
    }

    fun addNote(xFraction: Float, yFraction: Float) {
        val note = NoteEntity(
            monthKey = monthKey,
            xFraction = xFraction,
            yFraction = yFraction,
            widthDp = 150f,
            heightDp = 100f,
            contentHtml = "",
            paperColorHex = "#FFF9C4", // Default yellow
            fontSizeSp = 14f
        )
        viewModelScope.launch {
            val id = repository.insertNote(note)
            val noteWithId = note.copy(id = id)
            undoStack.add(Action.AddNote(noteWithId))
            redoStack.clear()
        }
    }

    fun updateNote(note: NoteEntity) {
        val oldNote = notes.value.find { it.id == note.id }
        viewModelScope.launch {
            repository.updateNote(note)
            if (oldNote != null && oldNote.contentHtml != note.contentHtml) {
                // Simplified update tracking: only track text changes for undo
                // In a real app, you might debounce this or track all property changes
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            cancelAlarm(note)
            repository.deleteNote(note)
            undoStack.add(Action.DeleteNote(note))
            redoStack.clear()
        }
    }

    fun clearStrokes() {
        viewModelScope.launch {
            repository.clearStrokesForMonth(monthKey)
            undoStack.removeAll { it is Action.AddStroke }
            redoStack.removeAll { it is Action.AddStroke }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearStrokesForMonth(monthKey)
            notes.value.forEach { cancelAlarm(it) }
            repository.clearNotesForMonth(monthKey)
            undoStack.clear()
            redoStack.clear()
        }
    }

    fun setAlarm(note: NoteEntity, epochMillis: Long) {
        val updatedNote = note.copy(
            alarmEpochMillis = epochMillis,
            alarmLabel = note.contentHtml.lineSequence().firstOrNull()?.take(20) ?: "Reminder"
        )
        viewModelScope.launch {
            repository.updateNote(updatedNote)
            scheduleAlarm(updatedNote)
        }
    }

    private fun scheduleAlarm(note: NoteEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NoteAlarmReceiver::class.java).apply {
            putExtra("noteId", note.id)
            putExtra("monthKey", note.monthKey)
            putExtra("alarmLabel", note.alarmLabel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        note.alarmEpochMillis?.let {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it,
                    pendingIntent
                )
            }
        }
    }

    private fun cancelAlarm(note: NoteEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NoteAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val action = undoStack.removeAt(undoStack.size - 1)
            viewModelScope.launch {
                when (action) {
                    is Action.AddStroke -> {
                        repository.deleteStroke(action.stroke)
                        redoStack.add(action)
                    }
                    is Action.AddNote -> {
                        cancelAlarm(action.note)
                        repository.deleteNote(action.note)
                        redoStack.add(action)
                    }
                    is Action.DeleteNote -> {
                        val id = repository.insertNote(action.note)
                        val restoredNote = action.note.copy(id = id)
                        restoredNote.alarmEpochMillis?.let { scheduleAlarm(restoredNote) }
                        redoStack.add(Action.DeleteNote(restoredNote))
                    }
                    is Action.UpdateNote -> {
                        repository.updateNote(action.oldNote)
                        redoStack.add(action)
                    }
                }
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.removeAt(redoStack.size - 1)
            viewModelScope.launch {
                when (action) {
                    is Action.AddStroke -> {
                        val id = repository.insertStroke(action.stroke)
                        undoStack.add(Action.AddStroke(action.stroke.copy(id = id)))
                    }
                    is Action.AddNote -> {
                        val id = repository.insertNote(action.note)
                        undoStack.add(Action.AddNote(action.note.copy(id = id)))
                    }
                    is Action.DeleteNote -> {
                        cancelAlarm(action.note)
                        repository.deleteNote(action.note)
                        undoStack.add(action)
                    }
                    is Action.UpdateNote -> {
                        repository.updateNote(action.newNote)
                        undoStack.add(action)
                    }
                }
            }
        }
    }
}
