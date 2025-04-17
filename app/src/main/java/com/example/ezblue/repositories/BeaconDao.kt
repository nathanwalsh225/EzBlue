package com.example.ezblue.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ezblue.model.Beacon

@Dao
interface BeaconDao {
    @Query("SELECT * FROM beacons")
    suspend fun getAllBeacons(): List<Beacon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beacons: List<Beacon>)

    @Query("DELETE FROM beacons")
    suspend fun clear()
}
