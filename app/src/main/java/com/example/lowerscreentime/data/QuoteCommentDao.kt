package com.example.lowerscreentime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteCommentDao {
    @Query("SELECT * FROM quote_comments WHERE quoteId = :quoteId ORDER BY timestamp DESC")
    fun getCommentsForQuote(quoteId: Long): Flow<List<QuoteComment>>

    @Insert
    suspend fun insert(comment: QuoteComment)
}
