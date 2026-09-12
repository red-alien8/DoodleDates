package com.spycodedoodledates.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notes")
@Serializable
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val xFraction: Float,
    val yFraction: Float,
    val widthDp: Float,
    val heightDp: Float,
    val contentHtml: String,
    val paperColorHex: String,
    val fontSizeSp: Float,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val textColorHex: String = "#000000",
    val alarmEpochMillis: Long? = null,
    val alarmLabel: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
