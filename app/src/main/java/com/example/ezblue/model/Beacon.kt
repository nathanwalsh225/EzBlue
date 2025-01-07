package com.example.ezblue.model

import java.util.Date

data class Beacon(
    val beaconId: String,
    val beaconName: String,
    val role: String,
    val note: String?,
    val uuid: String,
    val major: Int,
    val minor: Int,
    val createdAt: Date,
    val lastDetected: Date?,
    val ownerId: String,
    val signalStrength: Int, // RunTime only, not stored in DB
    val isConnected: Boolean = false,// RunTime only, not stored in DB
    val configurations: List<Configurations> = emptyList() //list for now but I still dont know if a list is what I will go with
)