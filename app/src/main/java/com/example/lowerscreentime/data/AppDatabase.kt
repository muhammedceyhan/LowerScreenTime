package com.example.lowerscreentime.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Habit::class, HabitCompletion::class, Quote::class, QuoteComment::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun quoteDao(): QuoteDao
    abstract fun quoteCommentDao(): QuoteCommentDao
}
