package com.example.ezblue.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ezblue.model.ActivityLogs
import com.example.ezblue.repositories.ActivityLogsDao

//Initialization of the Dao classes
@Database(entities = [ActivityLogs::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogsDao(): ActivityLogsDao
}