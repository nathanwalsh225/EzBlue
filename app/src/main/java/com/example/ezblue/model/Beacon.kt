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
    var lastDetected: Date?,
    val ownerId: String,
    val configurations: List<Configurations> = emptyList(), //Currently no plan to set up multiple configurations but leaving it as a list now allows me that option if I decide to add it later
    var signalStrength: Int = -100, // RunTime only, not stored in DB
    val isConnected: Boolean = false,// RunTime only, not stored in DB
    var status: BeaconStatus = BeaconStatus.OFFLINE // RunTime only, not stored in DB

)