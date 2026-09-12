package com.spycodedoodledates.ui.workspace.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spycodedoodledates.data.local.entities.NoteEntity
import kotlin.math.roundToInt

@Composable
fun NoteLayer(
    notes: List<NoteEntity>,
    onNoteUpdate: (NoteEntity) -> Unit,
    onNoteDelete: (NoteEntity) -> Unit,
    onSetAlarm: (NoteEntity) -> Unit,
    onFullScreen: (NoteEntity) -> Unit,
    enabled: Boolean
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()

        notes.forEach { note ->
            StickyNote(
                note = note,
                onUpdate = onNoteUpdate,
                onDelete = { onNoteDelete(note) },
                onAlarmClick = { onSetAlarm(note) },
                onFullScreen = { onFullScreen(note) },
                maxWidthPx = maxWidthPx,
                maxHeightPx = maxHeightPx
            )
        }
    }
}

@Composable
fun StickyNote(
    note: NoteEntity,
    onUpdate: (NoteEntity) -> Unit,
    onDelete: () -> Unit,
    onAlarmClick: () -> Unit,
    onFullScreen: () -> Unit,
    maxWidthPx: Float,
    maxHeightPx: Float,
    isFullscreenMode: Boolean = false
) {
    var xOffset by remember { mutableStateOf(note.xFraction * maxWidthPx) }
    var yOffset by remember { mutableStateOf(note.yFraction * maxHeightPx) }
    var width by remember { mutableStateOf(note.widthDp) }
    var height by remember { mutableStateOf(note.heightDp) }
    var text by remember { mutableStateOf(note.contentHtml) }
    var isBold by remember { mutableStateOf(note.isBold) }
    var isItalic by remember { mutableStateOf(note.isItalic) }
    var textColor by remember { mutableStateOf(Color(android.graphics.Color.parseColor(note.textColorHex))) }
    var showToolbar by remember { mutableStateOf(false) }

    Box(
        modifier = if (isFullscreenMode) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .offset { IntOffset(xOffset.roundToInt(), yOffset.roundToInt()) }
                .size(width.dp, height.dp)
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isFullscreenMode) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    onUpdate(
                                        note.copy(
                                            xFraction = xOffset / maxWidthPx,
                                            yFraction = yOffset / maxHeightPx
                                        )
                                    )
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    xOffset += dragAmount.x
                                    yOffset += dragAmount.y
                                }
                            )
                        }
                    } else Modifier
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(android.graphics.Color.parseColor(note.paperColorHex))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFullscreenMode) 0.dp else 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(if (isFullscreenMode) 32.dp else 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = { showToolbar = !showToolbar }, modifier = Modifier.size(if (isFullscreenMode) 48.dp else 24.dp)) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(if (isFullscreenMode) 32.dp else 16.dp))
                        }
                        IconButton(onClick = onAlarmClick, modifier = Modifier.size(if (isFullscreenMode) 48.dp else 24.dp)) {
                            Text(if (note.alarmEpochMillis != null) "⏰" else "🔔", fontSize = if (isFullscreenMode) 24.sp else 14.sp)
                        }
                        if (!isFullscreenMode) {
                            IconButton(onClick = onFullScreen, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(if (isFullscreenMode) 48.dp else 24.dp)) {
                        Text("×", fontSize = if (isFullscreenMode) 36.sp else 20.sp)
                    }
                }

                AnimatedVisibility(
                    visible = showToolbar || isFullscreenMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallFormatButton(
                            text = "B",
                            active = isBold,
                            onClick = { 
                                isBold = !isBold
                                onUpdate(note.copy(isBold = isBold))
                            },
                            size = if (isFullscreenMode) 40.dp else 24.dp
                        )
                        SmallFormatButton(
                            text = "I",
                            active = isItalic,
                            onClick = { 
                                isItalic = !isItalic
                                onUpdate(note.copy(isItalic = isItalic))
                            },
                            size = if (isFullscreenMode) 40.dp else 24.dp
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val noteColors = listOf(
                                Color.Black, Color.Red, Color.Blue, Color(0xFF006400), 
                                Color(0xFF8B4513), Color.Magenta, Color.DarkGray,
                                Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5),
                                Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFFFF9800)
                            )
                            items(noteColors) { color ->
                                Box(
                                    modifier = Modifier
                                        .size(if (isFullscreenMode) 36.dp else 20.dp)
                                        .background(color, CircleShape)
                                        .clickable { 
                                            textColor = color
                                            onUpdate(note.copy(textColorHex = String.format("#%06X", (0xFFFFFF and color.toArgb()))))
                                        }
                                        .border(
                                            width = if (textColor == color) 2.dp else 0.dp,
                                            color = if (textColor == color) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                BasicTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        onUpdate(note.copy(contentHtml = it)) 
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (if (isFullscreenMode) note.fontSizeSp * 1.5f else note.fontSizeSp).sp,
                        color = textColor,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                    )
                )
            }
        }
        
        // Resize handle
        if (!isFullscreenMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                onUpdate(note.copy(widthDp = width, heightDp = height))
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                width = (width + dragAmount.x / density).coerceIn(120f, 400f)
                                height = (height + dragAmount.y / density).coerceIn(50f, 400f)
                            }
                        )
                    }
            )
        }
    }
}

@Composable
fun SmallFormatButton(text: String, active: Boolean, onClick: () -> Unit, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(if (active) Color.LightGray else Color.Transparent, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = (size.value * 0.6).sp, fontWeight = FontWeight.Bold)
    }
}
