package com.example.lowerscreentime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lowerscreentime.data.Habit
import com.example.lowerscreentime.data.HabitDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    habitDao: HabitDao,
    onLockClick: () -> Unit,
    onUnlockClick: () -> Unit,
    isLocked: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // UI State
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    
    val habits by habitDao.getAllHabits().collectAsState(initial = emptyList())
    
    // Track completion state map: habitId -> isCompleted
    val completionState = remember { mutableStateMapOf<Long, Boolean>() }
    
    // Load completion status for today
    LaunchedEffect(habits) {
        habits.forEach { habit ->
            launch {
                habitDao.isHabitCompleted(habit.id, todayMillis).collectLatest { completed ->
                    completionState[habit.id] = completed
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lower Screen Time") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOCK SECTION
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock State",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { if (isLocked) onUnlockClick() else onLockClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isLocked) "UNLOCK" else "LOCK MODE")
                    }
                    if (isLocked) {
                        Text("DND Active", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CALENDAR SUMMARY
            Text("Last 7 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 6 downTo 0) {
                    val date = today.minusDays(i.toLong())
                    val dayMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    
                    // Logic to check if ALL habits were completed on this day would be complex to query directly efficiently in one go for all days
                    // For now, let's just assume we query for "Habit Success" separately or visualize it simply
                    // Simplified: We'll show day name. To make it GREEN, we need to know if all habits were done.
                    // This requires a more complex query or pre-loading.
                    // For this MVP, let's just make TODAY green if all checked.
                    
                    val isToday = i == 0
                    var isAllCompleted by remember { mutableStateOf(false) }
                    
                    // Real logic for "Green Day": Get all habits, check completion for each.
                    // This is expensive to do inside a loop for 7 days in UI.
                    // Better approach: DAO returns a list of "Fully Completed Dates".
                    // Optimization: Skip querying for past days for now to save complexity, or just load completions.
                    
                    DayCircle(date = date, isGreen = if (isToday) habits.isNotEmpty() && habits.all { completionState[it.id] == true } else false) 
                    // Note: Past days logic is mocked/simplified for now as "False" (Grey) except today
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HABITS LIST
            Text("Today's Habits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(habits) { habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = completionState[habit.id] == true,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    if (checked) {
                                        habitDao.insertCompletion(
                                            com.example.lowerscreentime.data.HabitCompletion(
                                                habitId = habit.id,
                                                date = todayMillis
                                            )
                                        )
                                    } else {
                                        habitDao.deleteCompletion(habit.id, todayMillis)
                                    }
                                }
                            }
                        )
                        Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (habits.isEmpty()) {
                    item {
                        Text("No habits added. Go to settings to add one!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DayCircle(date: LocalDate, isGreen: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isGreen) Color.Green else Color.LightGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isGreen) Color.Black else Color.White,
                fontSize = 12.sp
            )
        }
        Text(date.format(DateTimeFormatter.ofPattern("EEE")), style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
    }
}
