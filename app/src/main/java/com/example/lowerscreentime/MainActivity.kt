package com.example.lowerscreentime

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lowerscreentime.domain.LockManager
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lowerscreentime.ui.HomeScreen
import com.example.lowerscreentime.ui.SettingsScreen
import com.example.lowerscreentime.ui.theme.LowerScreenTimeTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Edit
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.widget.Toast
import android.content.Intent
import android.content.ComponentName
import com.example.lowerscreentime.ui.QuoteDetailScreen
import com.example.lowerscreentime.ui.QuotesScreen
import com.example.lowerscreentime.data.QuoteCommentDao

class MainActivity : ComponentActivity() {
    private lateinit var lockManager: LockManager
    private var isLocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as LowerScreenTimeApp
        val database = app.database
        val habitDao = database.habitDao()
        val quoteDao = database.quoteDao()
        val quoteCommentDao = database.quoteCommentDao()
        
        lockManager = LockManager(this)

        setContent {
            LowerScreenTimeTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentDestination != "settings") {
                             NavigationBar {
                                NavigationBarItem(
                                    selected = currentDestination == "home",
                                    onClick = { 
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.startsWith("quotes") == true,
                                    onClick = { 
                                        navController.navigate("quotes") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Edit, contentDescription = "Quotes") },
                                    label = { Text("Quotes") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController, 
                        startDestination = "home",
                        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                habitDao = habitDao,
                                quoteDao = quoteDao,
                                quoteCommentDao = quoteCommentDao,
                                onLockClick = { startLock() },
                                onUnlockClick = { stopLock() },
                                isLocked = isLocked
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                habitDao = habitDao
                            )
                        }
                        composable("quotes") {
                            QuotesScreen(
                                navController = navController,
                                quoteDao = quoteDao
                            )
                        }
                        composable(
                            "quote_detail/{quoteId}",
                            arguments = listOf(navArgument("quoteId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val quoteId = backStackEntry.arguments?.getLong("quoteId") ?: 0L
                            QuoteDetailScreen(
                                quoteId = quoteId,
                                navController = navController,
                                quoteDao = quoteDao,
                                quoteCommentDao = quoteCommentDao
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startLock() {
        lockManager.startLockMode(this)
        isLocked = true
    }

    private fun stopLock() {
        lockManager.stopLockMode(this)
        isLocked = false
    }
    
    // Optional: Check status on resume to sync UI if system unpinned us
    override fun onResume() {
        super.onResume()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val lockTaskMode = activityManager.lockTaskModeState
        isLocked = lockTaskMode != android.app.ActivityManager.LOCK_TASK_MODE_NONE
    }
}