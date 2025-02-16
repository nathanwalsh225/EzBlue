package com.example.ezblue.model

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Beacon(
    val beaconId: String = "",
    var beaconName: String = "",
    val role: String = "",
    val note: String? = null,
    val uuid: String = "",
    val major: Int = 0,
    val minor: Int = 0,
    val createdAt: Date = Date(),
    var lastDetected: Date? = Date(),
    val ownerId: String = "",
    var configuration: Configuration? = null, //Currently no plan to set up multiple configurations but leaving it as a list now allows me that option if I decide to add it later
    var signalStrength: Int = -100, // RunTime only, not stored in DB
    val isConnected: Boolean = false,// RunTime only, not stored in DB
    var status: BeaconStatus = BeaconStatus.OFFLINE, // RunTime only, not stored in DB
    var bluetoothDevice: BluetoothDevice? = null // Runtime only, not stored in DB

) : Parcelable