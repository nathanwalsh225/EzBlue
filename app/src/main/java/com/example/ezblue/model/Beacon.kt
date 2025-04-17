package com.example.ezblue.model

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "beacons")
data class Beacon(

    @PrimaryKey(autoGenerate = false)
    var beaconId: String = "",

    var beaconName: String = "",
    var role: String = "",
    var beaconNote: String? = null,
    var uuid: String = "",
    var major: Int = 0,
    var minor: Int = 0,
    var createdAt: Date = Date(),
    var lastDetected: Date? = Date(),
    var ownerId: String = "",
    var configuration: Configuration? = null,

    @Ignore
    var signalStrength: Int = -100, // RunTime only, not stored in DB
    @Ignore
    var isConnected: Boolean = false,// RunTime only, not stored in DB
    @Ignore
    var status: BeaconStatus = BeaconStatus.OFFLINE, // RunTime only, not stored in DB
    @Ignore
    var bluetoothDevice: BluetoothDevice? = null // Runtime only, not stored in DB

) : Parcelable
//    // Room requires a no-arg constructor when using @Ignore fields
//    constructor(
//        beaconId: String,
//        beaconName: String,
//        role: String,
//        beaconNote: String?,
//        uuid: String,
//        major: Int,
//        minor: Int,
//        createdAt: Date,
//        lastDetected: Date?,
//        ownerId: String,
//        configuration: Configuration?
//    ) : this(
//        beaconId = beaconId,
//        beaconName = beaconName,
//        role = role,
//        beaconNote = beaconNote,
//        uuid = uuid,
//        major = major,
//        minor = minor,
//        createdAt = createdAt,
//        lastDetected = lastDetected,
//        ownerId = ownerId,
//        configuration = configuration
//    )
//}