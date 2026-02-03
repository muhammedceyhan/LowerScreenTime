package com.example.lowerscreentime.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quote_comments",
    foreignKeys = [
        ForeignKey(
            entity = Quote::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuoteComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quoteId: Long,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
