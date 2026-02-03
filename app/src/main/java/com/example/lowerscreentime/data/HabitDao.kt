package com.example.lowerscreentime.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Check if a habit is completed on a specific date (ignoring time)
    @Query("SELECT EXISTS(SELECT 1 FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1)")
    fun isHabitCompleted(habitId: Long, date: Long): Flow<Boolean>

    @Insert
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: Long)

    // Get all completions for a date range (for calendar view)
    @Query("SELECT * FROM habit_completions WHERE date >= :startDate AND date <= :endDate")
    fun getCompletionsInRange(startDate: Long, endDate: Long): Flow<List<HabitCompletion>>
    
    // Get count of habits to calculate percentage (if needed)
    @Query("SELECT COUNT(*) FROM habits")
    fun getHabitCount(): Flow<Int>
}
