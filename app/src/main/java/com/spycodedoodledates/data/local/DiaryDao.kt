package com.spycodedoodledates.data.local

import androidx.room.*
import com.spycodedoodledates.data.local.entities.NoteEntity
import com.spycodedoodledates.data.local.entities.StrokeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM strokes WHERE monthKey = :monthKey ORDER BY createdAt ASC")
    fun getStrokesForMonth(monthKey: String): Flow<List<StrokeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStroke(stroke: StrokeEntity): Long

    @Delete
    suspend fun deleteStroke(stroke: StrokeEntity): Int

    @Query("DELETE FROM strokes WHERE id = :id")
    suspend fun deleteStrokeById(id: Long): Int

    @Query("DELETE FROM strokes WHERE monthKey = :monthKey")
    suspend fun clearStrokesForMonth(monthKey: String): Int

    @Query("SELECT * FROM notes WHERE monthKey = :monthKey ORDER BY createdAt ASC")
    fun getNotesForMonth(monthKey: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Delete
    suspend fun deleteNote(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long): Int

    @Query("DELETE FROM notes WHERE monthKey = :monthKey")
    suspend fun clearNotesForMonth(monthKey: String): Int

    @Query("SELECT COUNT(*) FROM strokes WHERE monthKey = :monthKey")
    suspend fun getStrokeCountForMonth(monthKey: String): Int

    @Query("SELECT COUNT(*) FROM notes WHERE monthKey = :monthKey")
    suspend fun getNoteCountForMonth(monthKey: String): Int
}
