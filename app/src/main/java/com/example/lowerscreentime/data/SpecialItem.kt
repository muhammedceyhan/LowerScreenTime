package com.example.lowerscreentime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "special_items")
data class SpecialItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
