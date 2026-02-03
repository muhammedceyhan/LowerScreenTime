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
import com.example.lowerscreentime.data.HabitCompletion
import com.example.lowerscreentime.data.HabitDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    habitDao: HabitDao,
    quoteDao: com.example.lowerscreentime.data.QuoteDao,
    quoteCommentDao: com.example.lowerscreentime.data.QuoteCommentDao,
    onLockClick: () -> Unit,
    onUnlockClick: () -> Unit,
    isLocked: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Permission granted or denied logic if needed
        }
    )
    
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                 notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
             }
        }
    }
    
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

    var showCalendarForHabit by remember { mutableStateOf<Habit?>(null) }

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
                        contentDescription = if (isLocked) "Locked" else "Unlocked",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = if (isLocked) onUnlockClick else onLockClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isLocked) "Unlock (Emergency)" else "Lock Phone")
                    }
                    if (isLocked) {
                        Text("DND Active", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FOCUS TIMER
            var timerDuration by remember { mutableStateOf(25f) }
            var timeLeft by remember { mutableStateOf(0L) }
            var isTimerRunning by remember { mutableStateOf(false) }
            var showCompletionDialog by remember { mutableStateOf(false) }
            var randomQuote by remember { mutableStateOf<com.example.lowerscreentime.data.Quote?>(null) }
            
            // Effect to handle timer countdown
            LaunchedEffect(isTimerRunning, timeLeft) {
                if (isTimerRunning && timeLeft > 0) {
                    kotlinx.coroutines.delay(1000L)
                    timeLeft -= 1000
                } else if (isTimerRunning && timeLeft <= 0) {
                    isTimerRunning = false
                    onUnlockClick()
                    
                    // Send Notification
                    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    val intent = android.content.Intent(context, com.example.lowerscreentime.MainActivity::class.java).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: android.app.PendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE)

                    val builder = androidx.core.app.NotificationCompat.Builder(context, "focus_channel")
                        .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon if available, using system default for now to be safe
                        .setContentTitle("Focus Session Complete!")
                        .setContentText("Well done! Review your session.")
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    notificationManager.notify(1001, builder.build())

                    // Timer Finished!
                    // Fetch random quote
                    scope.launch {
                        val allQuotes = quoteDao.getAll().first()
                        if (allQuotes.isNotEmpty()) {
                            randomQuote = allQuotes.random()
                            showCompletionDialog = true
                        }
                    }
                }
            }
            
            // Request Notification Permission
            LaunchedEffect(Unit) {
               if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                   if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                       // We can't request directly from Composable easily without Accompanist or ActivityResultLauncher within the Activity.
                       // For simplicity in this existing structure, let's assume MainActivity might handle it or we just try-catch/ignore if we can't.
                       // Ideally, we should use rememberLauncherForActivityResult.
                   }
               }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Focus Timer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                if (isTimerRunning) {
                        Text(
                            text = String.format("%02d:%02d", (timeLeft / 1000) / 60, (timeLeft / 1000) % 60),
                            style = MaterialTheme.typography.displayMedium
                        )
                        Button(
                            onClick = { 
                                isTimerRunning = false
                                onUnlockClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Stop")
                        }
                    } else {
                        Text("${timerDuration.toInt()} minutes")
                        Slider(
                            value = timerDuration,
                            onValueChange = { timerDuration = it },
                            valueRange = 1f..120f,
                            steps = 119
                        )
                        Button(onClick = {
                            timeLeft = (timerDuration.toInt() * 60 * 1000).toLong()
                            isTimerRunning = true
                            onLockClick()
                        }) {
                            Text("Start Focus")
                        }
                    }
                }
            }
            
            // Completion Dialog
            if (showCompletionDialog && randomQuote != null) {
                var commentText by remember { mutableStateOf("") }
                
                AlertDialog(
                    onDismissRequest = { showCompletionDialog = false },
                    title = { Text("Session Complete!") },
                    text = {
                        Column {
                            Text("Well done! Here is a quote for you:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${randomQuote!!.content}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                label = { Text("Reflection (Optional)") },
                                placeholder = { Text("How was your focus?") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (commentText.isNotBlank()) {
                                scope.launch {
                                    quoteCommentDao.insert(
                                        com.example.lowerscreentime.data.QuoteComment(
                                            quoteId = randomQuote!!.id,
                                            text = commentText
                                        )
                                    )
                                    showCompletionDialog = false
                                }
                            } else {
                                showCompletionDialog = false
                            }
                        }) {
                            Text("Save & Close")
                        }
                    }
                )
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
                    val date = LocalDate.now().minusDays(i.toLong())
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
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits) { habit ->
                    val isCompleted = completionState[habit.id] == true
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                            .clickable { showCalendarForHabit = habit }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                        Checkbox(
                            checked = isCompleted,
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
                    }
                }
                if (habits.isEmpty()) {
                    item {
                        Text("No habits added. Go to settings to add one!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        }
        
        // Calendar Dialog
        if (showCalendarForHabit != null) {
            val habit = showCalendarForHabit!!
            val completions by habitDao.getCompletionsForHabit(habit.id).collectAsState(initial = emptyList())
            val completedDates = remember(completions) {
                completions.map { 
                    java.time.Instant.ofEpochMilli(it.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate() 
                }.toSet()
            }

            AlertDialog(
                onDismissRequest = { showCalendarForHabit = null },
                title = { Text("History: ${habit.name}") },
                text = {
                    com.example.lowerscreentime.ui.components.CalendarView(
                        completedDates = completedDates
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showCalendarForHabit = null }) {
                        Text("Close")
                    }
                }
            )
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
