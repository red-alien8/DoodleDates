package com.spycodedoodledates.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spycodedoodledates.data.local.entities.NoteEntity
import com.spycodedoodledates.data.local.entities.StrokeEntity

@Database(entities = [StrokeEntity::class, NoteEntity::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}
