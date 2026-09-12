package com.spycodedoodledates.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "strokes")
@Serializable
data class StrokeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String, // e.g., "2026-01"
    val type: String, // "pen" | "erase"
    val colorHex: String,
    val widthDp: Float,
    val pointsJson: String, // List of {xFraction, yFraction, pressure} relative 0..1
    val createdAt: Long = System.currentTimeMillis()
)
