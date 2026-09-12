package com.spycodedoodledates.ui.workspace.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.spycodedoodledates.data.local.entities.StrokeEntity
import com.spycodedoodledates.domain.model.DrawingPoint
import kotlinx.serialization.json.Json
import kotlin.math.*

@Composable
fun DrawingLayer(
    strokes: List<StrokeEntity>,
    onDrawEnd: (List<DrawingPoint>) -> Unit,
    enabled: Boolean,
    currentStrokeColor: Color,
    currentStrokeWidth: Float
) {
    val currentPoints = remember { mutableStateListOf<DrawingPoint>() }
    
    // Zoom/Pan State
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformModifier = Modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 5f)
                offset += pan
            }
        }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(if (!enabled) transformModifier else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .pointerInput(enabled, scale, offset) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { startOffset ->
                        currentPoints.clear()
                        // Adjust points by current scale and offset
                        val adjustedX = (startOffset.x - offset.x) / (size.width * scale)
                        val adjustedY = (startOffset.y - offset.y) / (size.height * scale)
                        currentPoints.add(DrawingPoint(adjustedX, adjustedY, 1f))
                    },
                    onDrag = { change, _ ->
                        val adjustedX = (change.position.x - offset.x) / (size.width * scale)
                        val adjustedY = (change.position.y - offset.y) / (size.height * scale)
                        currentPoints.add(DrawingPoint(adjustedX, adjustedY, change.pressure))
                    },
                    onDragEnd = {
                        if (currentPoints.isNotEmpty()) {
                            val finalPoints = detectAndApplySmartShape(currentPoints.toList())
                            onDrawEnd(finalPoints)
                            currentPoints.clear()
                        }
                    }
                )
            }
    ) {
        // Draw existing strokes
        strokes.forEach { strokeEntity ->
            val points: List<DrawingPoint> = try {
                Json.decodeFromString(strokeEntity.pointsJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            if (points.size > 1) {
                val path = createSmoothPath(points, size.width, size.height)
                val isEraser = strokeEntity.type == "erase"
                drawPath(
                    path = path,
                    color = if (isEraser) Color.Transparent else Color(android.graphics.Color.parseColor(strokeEntity.colorHex)),
                    style = Stroke(
                        width = (strokeEntity.widthDp * density) / scale, // Compensate for scale
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver
                )
            }
        }

        // Draw current active stroke
        if (currentPoints.size > 1) {
            val path = createSmoothPath(currentPoints, size.width, size.height)
            drawPath(
                path = path,
                color = currentStrokeColor,
                style = Stroke(
                    width = (currentStrokeWidth * density) / scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun detectAndApplySmartShape(points: List<DrawingPoint>): List<DrawingPoint> {
    if (points.size < 10) return points
    
    // Simplistic shape detection: Check if start and end are close (potential circle/rect)
    val start = points.first()
    val end = points.last()
    val dist = sqrt((start.xFraction - end.xFraction).pow(2) + (start.yFraction - end.yFraction).pow(2))
    
    if (dist < 0.05) { // Closed shape
        // For now, let's just smooth it significantly or return as is.
        // True smart shapes require more complex convex hull / template matching.
        return points 
    }
    
    return points
}

private fun createSmoothPath(points: List<DrawingPoint>, width: Float, height: Float): Path {
    val path = Path()
    if (points.isEmpty()) return path
    
    val first = points[0]
    path.moveTo(first.xFraction * width, first.yFraction * height)
    
    if (points.size < 3) {
        for (i in 1 until points.size) {
            path.lineTo(points[i].xFraction * width, points[i].yFraction * height)
        }
    } else {
        for (i in 1 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.xFraction + p1.xFraction) / 2 * width
            val midY = (p0.yFraction + p1.yFraction) / 2 * height
            path.quadraticTo(
                p0.xFraction * width,
                p0.yFraction * height,
                midX,
                midY
            )
        }
        val last = points.last()
        path.lineTo(last.xFraction * width, last.yFraction * height)
    }
    return path
}
