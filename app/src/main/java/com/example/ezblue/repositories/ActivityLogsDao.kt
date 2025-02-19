package com.example.ezblue.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ezblue.model.ActivityLogs

@Dao
interface ActivityLogsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogs(activityLogs: ActivityLogs): Long

    @Query("SELECT * FROM activity_logs WHERE beaconID = :beaconID")
    fun getLogsByBeaconId(beaconID: String): List<ActivityLogs>
}