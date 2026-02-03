package com.example.lowerscreentime.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY orderIndex ASC")
    fun getAll(): Flow<List<Quote>>

    @Insert
    suspend fun insert(quote: Quote)

    @Update
    suspend fun update(quote: Quote)

    @Delete
    suspend fun delete(quote: Quote)
}
