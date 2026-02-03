package com.example.lowerscreentime.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialItemDao {
    @Query("SELECT * FROM special_items ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SpecialItem>>

    @Insert
    suspend fun insert(item: SpecialItem)

    @Delete
    suspend fun delete(item: SpecialItem)
}
