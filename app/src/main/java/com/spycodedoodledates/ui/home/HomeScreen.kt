package com.spycodedoodledates.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spycodedoodledates.domain.model.getHolidaysForYear
import com.spycodedoodledates.ui.workspace.components.DigitalClock
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWorkspace: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currentYear by viewModel.currentYear.collectAsState()
    val monthDataStatus by viewModel.monthDataStatus.collectAsState()
    var showYearMenu by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val currentMonth = remember { LocalDate.now().monthValue }

    LaunchedEffect(currentYear) {
        if (currentYear == LocalDate.now().year) {
            gridState.scrollToItem(currentMonth - 1)
        } else {
            gridState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showYearMenu = true }
                        ) {
                            Text(
                                text = "Diary $currentYear",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            
                            DropdownMenu(
                                expanded = showYearMenu,
                                onDismissRequest = { showYearMenu = false }
                            ) {
                                (2026..2030).forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = {
                                            viewModel.setYear(year)
                                            showYearMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        DigitalClock()
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Legend()
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                state = gridState,
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items((1..12).toList()) { month ->
                    val monthKey = "$currentYear-${month.toString().padStart(2, '0')}"
                    MonthCard(
                        year = currentYear,
                        month = month,
                        hasData = monthDataStatus[monthKey] ?: false,
                        onClick = { onNavigateToWorkspace(monthKey) }
                    )
                }
            }
        }
    }
}

@Composable
fun Legend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = Color(0xFFFF8A80).copy(alpha = 0.6f), label = "Holiday")
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(color = Color(0xFF80CBC4).copy(alpha = 0.6f), label = "Off Day")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MonthCard(year: Int, month: Int, hasData: Boolean, onClick: () -> Unit) {
    val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (hasData) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            MiniCalendar(year, month)
        }
    }
}

@Composable
fun MiniCalendar(year: Int, month: Int) {
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val holidays = getHolidaysForYear(year)

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        var currentDay = 1
        for (week in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    val dayIdx = week * 7 + dayOfWeek
                    if (dayIdx >= firstDayOfWeek && currentDay <= daysInMonth) {
                        val date = LocalDate.of(year, month, currentDay)
                        val isFriday = date.dayOfWeek.value == 5
                        val isSaturday = date.dayOfWeek.value == 6
                        val holiday = holidays.find { it.month == month && it.day == currentDay }
                        
                        val bgColor = when {
                            holiday != null -> Color(0xFFFF8A80).copy(alpha = 0.6f)
                            isFriday || isSaturday -> Color(0xFF80CBC4).copy(alpha = 0.6f)
                            else -> Color.Transparent
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bgColor != Color.Transparent) {
                                Box(modifier = Modifier.fillMaxSize().background(bgColor, CircleShape))
                            }
                            Text(
                                text = currentDay.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (holiday != null) Color.Red else Color.Unspecified
                            )
                        }
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
