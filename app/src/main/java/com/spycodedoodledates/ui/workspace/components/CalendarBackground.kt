package com.spycodedoodledates.ui.workspace.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spycodedoodledates.domain.model.getHolidaysForYear
import java.time.LocalDate

@Composable
fun CalendarBackground(monthKey: String) {
    if (monthKey.isEmpty()) return
    
    val year = monthKey.split("-")[0].toInt()
    val month = monthKey.split("-")[1].toInt()
    
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val holidays = getHolidaysForYear(year)
    val today = LocalDate.now()

    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle grid lines or "paper" texture could go here
        DrawingGrid()

        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Day headers
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            var currentDay = 1
            for (week in 0..5) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    for (dayOfWeek in 0..6) {
                        val dayIdx = week * 7 + dayOfWeek
                        if (dayIdx >= firstDayOfWeek && currentDay <= daysInMonth) {
                            val date = LocalDate.of(year, month, currentDay)
                            val isFriday = date.dayOfWeek.value == 5
                            val isSaturday = date.dayOfWeek.value == 6
                            val holiday = holidays.find { it.month == month && it.day == currentDay }
                            val isToday = date == today
                            
                            CalendarDayCell(
                                day = currentDay,
                                isToday = isToday,
                                isWeekend = isFriday || isSaturday,
                                holidayName = holiday?.name,
                                modifier = Modifier.weight(1f)
                            )
                            currentDay++
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (currentDay > daysInMonth) break
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isWeekend: Boolean,
    holidayName: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(2.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else if (holidayName != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFFEBEE), CircleShape)
            )
        } else if (isWeekend) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE0F2F1), CircleShape)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isToday || holidayName != null) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 20.sp
                ),
                color = when {
                    holidayName != null -> Color.Red
                    isToday -> MaterialTheme.colorScheme.primary
                    isWeekend -> Color(0xFF00796B)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                }
            )
            
            if (holidayName != null) {
                Text(
                    text = holidayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.Red.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 10.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            } else if (isWeekend) {
                Text(
                    text = "Weekend",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color(0xFF00796B).copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DrawingGrid() {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        
        // Draw horizontal dashed lines
        val rows = 6
        for (i in 1..rows) {
            val y = (size.height / rows) * i
            drawLine(
                color = outlineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth,
                pathEffect = dashPathEffect
            )
        }
        
        // Draw vertical dashed lines
        val cols = 7
        for (i in 1..cols) {
            val x = (size.width / cols) * i
            drawLine(
                color = outlineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth,
                pathEffect = dashPathEffect
            )
        }
    }
}
