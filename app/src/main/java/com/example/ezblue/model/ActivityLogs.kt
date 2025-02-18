package com.example.ezblue.model

import androidx.room.Entity
import androidx.room.PrimaryKey

//Database entity for the logs in room
@Entity(tableName = "activity_logs")
data class ActivityLogs (
    @PrimaryKey(autoGenerate = true)
    val logId: Int,
    val beaconId: String,
    val action: String,
    val parameters: String,
    val timestamp: String,
    val status: LogResults
)
