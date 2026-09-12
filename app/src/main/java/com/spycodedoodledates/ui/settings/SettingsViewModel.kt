package com.spycodedoodledates.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spycodedoodledates.data.repository.DiaryRepository
import com.spycodedoodledates.ui.theme.settings.CustomTheme
import com.spycodedoodledates.ui.theme.settings.DefaultTheme
import com.spycodedoodledates.ui.theme.settings.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class BackupData(
    val strokes: List<com.spycodedoodledates.data.local.entities.StrokeEntity>,
    val notes: List<com.spycodedoodledates.data.local.entities.NoteEntity>
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeSettings: ThemeSettings,
    private val repository: DiaryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val appSettings = themeSettings.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.spycodedoodledates.ui.theme.settings.AppSettingsState(DefaultTheme, true, null))

    val currentTheme: StateFlow<CustomTheme> = appSettings.map { it.theme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultTheme)

    fun updateVibration(enabled: Boolean) {
        viewModelScope.launch {
            themeSettings.updateVibration(enabled)
        }
    }

    fun updateRingtone(uri: String?) {
        viewModelScope.launch {
            themeSettings.updateRingtone(uri)
        }
    }

    fun updateTheme(newTheme: CustomTheme) {
        viewModelScope.launch {
            themeSettings.updateTheme(newTheme)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            themeSettings.resetToDefault()
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            // This is a simplified export. Ideally, we'd query all data.
            // For now, let's assume we export all strokes and notes for 2026-2030
            val allStrokes = mutableListOf<com.spycodedoodledates.data.local.entities.StrokeEntity>()
            val allNotes = mutableListOf<com.spycodedoodledates.data.local.entities.NoteEntity>()
            
            for (year in 2026..2030) {
                for (month in 1..12) {
                    val key = "$year-${month.toString().padStart(2, '0')}"
                    allStrokes.addAll(repository.getStrokesForMonth(key).first())
                    allNotes.addAll(repository.getNotesForMonth(key).first())
                }
            }
            
            val backup = BackupData(allStrokes, allNotes)
            val json = Json.encodeToString(backup)
            
            context.contentResolver.openOutputStream(uri)?.use { 
                it.write(json.toByteArray())
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) {
                val backup = Json.decodeFromString<BackupData>(json)
                backup.strokes.forEach { repository.insertStroke(it) }
                backup.notes.forEach { repository.insertNote(it) }
            }
        }
    }
}
