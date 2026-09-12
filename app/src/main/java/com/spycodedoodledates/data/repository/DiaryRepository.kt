package com.spycodedoodledates.data.repository

import com.spycodedoodledates.data.local.DiaryDao
import com.spycodedoodledates.data.local.entities.NoteEntity
import com.spycodedoodledates.data.local.entities.StrokeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao
) {
    fun getStrokesForMonth(monthKey: String): Flow<List<StrokeEntity>> =
        diaryDao.getStrokesForMonth(monthKey)

    suspend fun insertStroke(stroke: StrokeEntity): Long = diaryDao.insertStroke(stroke)

    suspend fun deleteStroke(stroke: StrokeEntity) {
        diaryDao.deleteStroke(stroke)
    }
    
    suspend fun deleteStrokeById(id: Long) {
        diaryDao.deleteStrokeById(id)
    }

    suspend fun clearStrokesForMonth(monthKey: String) {
        diaryDao.clearStrokesForMonth(monthKey)
    }

    suspend fun clearNotesForMonth(monthKey: String) {
        diaryDao.clearNotesForMonth(monthKey)
    }

    fun getNotesForMonth(monthKey: String): Flow<List<NoteEntity>> =
        diaryDao.getNotesForMonth(monthKey)

    suspend fun insertNote(note: NoteEntity): Long = diaryDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) {
        diaryDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        diaryDao.deleteNote(note)
    }
    
    suspend fun deleteNoteById(id: Long) {
        diaryDao.deleteNoteById(id)
    }

    suspend fun hasDataForMonth(monthKey: String): Boolean {
        val strokeCount = diaryDao.getStrokeCountForMonth(monthKey)
        val noteCount = diaryDao.getNoteCountForMonth(monthKey)
        return strokeCount > 0 || noteCount > 0
    }
}
