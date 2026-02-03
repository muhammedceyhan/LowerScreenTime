package com.example.lowerscreentime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val orderIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
