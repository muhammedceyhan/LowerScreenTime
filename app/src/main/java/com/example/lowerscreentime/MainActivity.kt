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
import com.example.lowerscreentime.ui.HomeScreen
import com.example.lowerscreentime.ui.SettingsScreen
import com.example.lowerscreentime.ui.theme.LowerScreenTimeTheme

class MainActivity : ComponentActivity() {
    private lateinit var lockManager: LockManager
    private var isLocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as LowerScreenTimeApp
        val habitDao = app.database.habitDao()
        lockManager = LockManager(this)

        setContent {
            LowerScreenTimeTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            habitDao = habitDao,
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