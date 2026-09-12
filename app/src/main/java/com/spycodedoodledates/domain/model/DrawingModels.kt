package com.spycodedoodledates.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DrawingPoint(
    val xFraction: Float,
    val yFraction: Float,
    val pressure: Float
)

enum class DrawingMode {
    PEN, ERASER, TEXT
}
