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
        ).build()
    }
}
