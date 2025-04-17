package com.example.ezblue.roomdb

import androidx.room.TypeConverter
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.model.Configuration
import com.google.gson.Gson
import java.util.Date

class BeaconConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun beaconStatusToString(status: BeaconStatus?): String? = status?.name

    @TypeConverter
    fun stringToBeaconStatus(name: String?): BeaconStatus? = name?.let { BeaconStatus.valueOf(it) }

    @TypeConverter
    fun fromConfiguration(value: String?): Configuration? =
        value?.let { Gson().fromJson(it, Configuration::class.java) }

    @TypeConverter
    fun configurationToString(config: Configuration?): String? =
        Gson().toJson(config)
}
