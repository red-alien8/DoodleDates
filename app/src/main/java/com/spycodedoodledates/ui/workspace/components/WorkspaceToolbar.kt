package com.spycodedoodledates.ui.workspace.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spycodedoodledates.domain.model.DrawingMode
import kotlin.math.roundToInt

@Composable
fun WorkspaceToolbar(
    currentMode: DrawingMode,
    onModeChange: (DrawingMode) -> Unit,
    currentColor: Color,
    onColorChange: (Color) -> Unit,
    currentWidth: Float,
    onWidthChange: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearDrawing: () -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    var dragOffsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var dragOffsetY by rememberSaveable { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (expanded) 24.dp else 32.dp,
        label = "cornerRadius"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
                .widthIn(max = 600.dp)
                .wrapContentHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    }
                }
                .shadow(12.dp, RoundedCornerShape(cornerRadius)),
            shape = RoundedCornerShape(cornerRadius),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .animateContentSize()
            ) {
                if (expanded) {
                    FullToolbarContent(
                        currentMode = currentMode,
                        onModeChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onModeChange(it)
                        },
                        currentColor = currentColor,
                        onColorChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onColorChange(it)
                        },
                        currentWidth = currentWidth,
                        onWidthChange = onWidthChange,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        onClearDrawing = onClearDrawing,
                        onClearAll = onClearAll,
                        onBack = onBack,
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onCollapse = { expanded = false }
                    )
                } else {
                    CollapsedToolbarContent(
                        currentMode = currentMode,
                        onExpand = { expanded = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun FullToolbarContent(
    currentMode: DrawingMode,
    onModeChange: (DrawingMode) -> Unit,
    currentColor: Color,
    onColorChange: (Color) -> Unit,
    currentWidth: Float,
    onWidthChange: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearDrawing: () -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onCollapse: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Top Row: Navigation and Main Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // Mode Selector
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    ModeToggleButton(
                        selected = currentMode == DrawingMode.PEN,
                        onClick = { onModeChange(DrawingMode.PEN) },
                        icon = Icons.Default.Brush,
                        label = "Pen"
                    )
                    ModeToggleButton(
                        selected = currentMode == DrawingMode.ERASER,
                        onClick = { onModeChange(DrawingMode.ERASER) },
                        icon = Icons.Default.AutoFixHigh,
                        label = "Eraser"
                    )
                    ModeToggleButton(
                        selected = currentMode == DrawingMode.TEXT,
                        onClick = { onModeChange(DrawingMode.TEXT) },
                        icon = Icons.AutoMirrored.Filled.StickyNote2,
                        label = "Text"
                    )
                }
            }

            IconButton(onClick = onCollapse) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Middle Row: Width Slider and Undo/Redo
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LineWeight, contentDescription = null, modifier = Modifier.size(20.dp))
            Slider(
                value = currentWidth,
                onValueChange = onWidthChange,
                valueRange = 1f..50f,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            )
            
            VerticalDivider(modifier = Modifier.height(32.dp).padding(horizontal = 8.dp))

            Row {
                ActionIconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    icon = Icons.AutoMirrored.Filled.Undo,
                    tint = if (canUndo) MaterialTheme.colorScheme.primary else Color.Gray
                )
                ActionIconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    icon = Icons.AutoMirrored.Filled.Redo,
                    tint = if (canRedo) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }

        // Bottom Row: Color Palette and Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ColorPalette(
                    selectedColor = currentColor,
                    onColorChange = onColorChange
                )
            }
            
            Row(modifier = Modifier.padding(end = 8.dp)) {
                ActionIconButton(
                    onClick = onClearDrawing,
                    icon = Icons.Default.CleaningServices,
                    tint = MaterialTheme.colorScheme.error,
                    tooltip = "Clear Drawing"
                )
                ActionIconButton(
                    onClick = onClearAll,
                    icon = Icons.Default.DeleteSweep,
                    tint = MaterialTheme.colorScheme.error,
                    tooltip = "Clear All"
                )
            }
        }
    }
}

@Composable
private fun ModeToggleButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "modeColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
        label = "modeContentColor"
    )

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = contentColor)
            if (selected) {
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    tooltip: String = ""
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = tooltip, tint = tint)
    }
}

@Composable
private fun CollapsedToolbarContent(
    currentMode: DrawingMode,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when(currentMode) {
                DrawingMode.PEN -> Icons.Default.Brush
                DrawingMode.ERASER -> Icons.Default.AutoFixHigh
                DrawingMode.TEXT -> Icons.AutoMirrored.Filled.StickyNote2
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Expand Toolbar",
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ColorPalette(
    selectedColor: Color,
    onColorChange: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black, Color.White, Color.Red, Color.Blue, Color(0xFF006400),
        Color.Yellow, Color(0xFFFF8C00), Color(0xFF8B4513), Color(0xFF800080),
        Color(0xFF008080), Color(0xFFFFD1DC), Color(0xFFAEC6CF), Color(0xFF77DD77),
        Color(0xFFE6E6FA), Color(0xFFB2FBDA), Color.Magenta, Color(0xFF3F51B5),
        Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFFCDDC39)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(colors) { color ->
            val isSelected = selectedColor == color
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { onColorChange(color) }
            )
        }
    }
}
