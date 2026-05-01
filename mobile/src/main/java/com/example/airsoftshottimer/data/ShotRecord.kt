package com.example.airsoftshottimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shot_records")
data class ShotRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val time: Float
)
