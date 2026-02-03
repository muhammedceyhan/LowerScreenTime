package com.example.lowerscreentime

import android.app.Application
import androidx.room.Room
import com.example.lowerscreentime.data.AppDatabase

class LowerScreenTimeApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "lower_screen_time_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Focus Timer"
            val descriptionText = "Notifications for focus timer completion"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("focus_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
