package com.example.lowerscreentime.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Habit::class, HabitCompletion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
