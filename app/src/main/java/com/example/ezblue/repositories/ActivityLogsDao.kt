package com.example.ezblue.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.ezblue.model.ActivityLogs

@Dao
interface ActivityLogsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogs(activityLogs: ActivityLogs): Long
}