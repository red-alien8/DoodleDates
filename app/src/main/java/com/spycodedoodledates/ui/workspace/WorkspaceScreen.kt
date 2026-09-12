package com.spycodedoodledates.ui.workspace

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spycodedoodledates.data.local.entities.NoteEntity
import com.spycodedoodledates.domain.model.DrawingMode
import com.spycodedoodledates.ui.workspace.components.*
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    monthKey: String?,
    onBack: () -> Unit,
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val currentColor by viewModel.currentColor.collectAsState()
    val currentWidth by viewModel.currentWidth.collectAsState()
    val strokes by viewModel.strokes.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var showDatePicker by remember { mutableStateOf<NoteEntity?>(null) }
    var showTimePicker by remember { mutableStateOf<NoteEntity?>(null) }
    var fullScreenNote by remember { mutableStateOf<NoteEntity?>(null) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Scaffold(
        bottomBar = {
            WorkspaceToolbar(
                currentMode = currentMode,
                onModeChange = { viewModel.setMode(it) },
                currentColor = currentColor,
                onColorChange = { viewModel.setColor(it) },
                currentWidth = currentWidth,
                onWidthChange = { viewModel.setWidth(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onClearDrawing = { viewModel.clearStrokes() },
                onClearAll = { viewModel.clearAll() },
                onBack = onBack,
                canUndo = viewModel.undoStack.isNotEmpty(),
                canRedo = viewModel.redoStack.isNotEmpty()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CalendarBackground(monthKey = monthKey ?: "")

            DigitalClock(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            DrawingLayer(
                strokes = strokes,
                onDrawEnd = { viewModel.addStroke(it) },
                enabled = currentMode == DrawingMode.PEN || currentMode == DrawingMode.ERASER,
                currentStrokeColor = currentColor,
                currentStrokeWidth = currentWidth
            )

            NoteLayer(
                notes = notes,
                onNoteUpdate = { viewModel.updateNote(it) },
                onNoteDelete = { viewModel.deleteNote(it) },
                onSetAlarm = { note ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    showDatePicker = note
                },
                onFullScreen = { fullScreenNote = it },
                enabled = currentMode == DrawingMode.TEXT
            )
            
            AnimatedVisibility(
                visible = fullScreenNote != null,
                enter = fadeIn() + expandIn(),
                exit = fadeOut() + shrinkOut()
            ) {
                fullScreenNote?.let { note ->
                    val currentNote = notes.find { it.id == note.id } ?: note
                    
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(android.graphics.Color.parseColor(currentNote.paperColorHex)).copy(alpha = 0.98f)
                    ) {
                        Column(modifier = Modifier.padding(32.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Full Screen Edit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { fullScreenNote = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            StickyNote(
                                note = currentNote,
                                onUpdate = { viewModel.updateNote(it) },
                                onDelete = { 
                                    viewModel.deleteNote(currentNote)
                                    fullScreenNote = null
                                },
                                onAlarmClick = { /* Alarm set from note layer or here if needed */ },
                                onFullScreen = { /* Already in full screen */ },
                                maxWidthPx = 1000f,
                                maxHeightPx = 1000f,
                                isFullscreenMode = true
                            )
                        }
                    }
                }
            }
            
            if (currentMode == DrawingMode.TEXT) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                viewModel.addNote(offset.x / size.width, offset.y / size.height)
                            }
                        }
                )
            }

            if (showDatePicker != null) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = null },
                    confirmButton = {
                        TextButton(onClick = {
                            showTimePicker = showDatePicker
                            showDatePicker = null
                        }) {
                            Text("OK")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker != null) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = null },
                    confirmButton = {
                        TextButton(onClick = {
                            val selectedDate = datePickerState.selectedDateMillis?.let {
                                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), ZoneId.systemDefault())
                            } ?: LocalDateTime.now()
                            
                            val alarmTime = selectedDate
                                .withHour(timePickerState.hour)
                                .withMinute(timePickerState.minute)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                            
                            viewModel.setAlarm(showTimePicker!!, alarmTime)
                            showTimePicker = null
                        }) {
                            Text("Set Alarm")
                        }
                    },
                    text = {
                        TimePicker(state = timePickerState)
                    }
                )
            }
        }
    }
}
