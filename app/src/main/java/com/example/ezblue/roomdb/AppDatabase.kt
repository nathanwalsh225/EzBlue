package com.example.ezblue.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ezblue.model.ActivityLogs
import com.example.ezblue.model.Beacon
import com.example.ezblue.repositories.ActivityLogsDao
import com.example.ezblue.repositories.BeaconDao

//Initialization of the Dao classes
@Database(entities = [ActivityLogs::class, Beacon::class], version = 2)
@TypeConverters(BeaconConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogsDao(): ActivityLogsDao
    abstract fun beaconDao(): BeaconDao
}