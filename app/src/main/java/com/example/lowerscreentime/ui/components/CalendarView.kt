package com.example.lowerscreentime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    completedDates: Set<LocalDate>,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = remember(currentMonth) {
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonthCount = currentMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) - 7 (Sun)
        
        // Adjust for Sunday start? Or Monday? Let's assume Monday start for now or standard ISO
        // If we want standard calendar grid, we usually need placeholders for start
        (1..daysInMonthCount).map { currentMonth.atDay(it) }
    }
    
    // Simple placeholder calc for grid offset
    val startOffset = currentMonth.atDay(1).dayOfWeek.value % 7 // If 7 (Sun) -> 0? Or 1(Mon)->0?
    // Let's use standard Java Time DayOfWeek: 1=Mon, ..., 7=Sun.
    // Compose Grid: typically want to start row correctly.
    // Let's just list days.

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, "Previous Month")
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, "Next Month")
            }
        }

        // Days of Week Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        // We need to pad the start
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1=Mon
        val padCount = firstDayOfWeek - 1

        val totalSlots = padCount + daysInMonth.size
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp), // Fixed height for now
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Padding
            items(padCount) {
                Box(modifier = Modifier.aspectRatio(1f))
            }
            
            // Days
            items(daysInMonth) { date ->
                val isCompleted = completedDates.contains(date)
                val isFuture = date.isAfter(LocalDate.now())
                
                // Color Logic:
                // Green if completed.
                // Black (or Dark Gray) if missed (past and not completed).
                // Default/Transparent if future? Or Gray?
                
                val backgroundColor = when {
                    isCompleted -> Color.Green
                    !isFuture && !isCompleted -> Color.Black
                    else -> Color.Transparent // Future
                }
                
                val textColor = when {
                    isCompleted -> Color.Black
                    !isFuture && !isCompleted -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(backgroundColor, shape = MaterialTheme.shapes.small)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
