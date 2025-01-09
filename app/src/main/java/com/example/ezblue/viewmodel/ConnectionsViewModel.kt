package com.example.ezblue.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.repositories.ConnectionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Error
import java.util.Date
import javax.inject.Inject

//First time using Hilt so this should be fun (Based on the setup alone, this will not be fun)
//https://developer.android.com/training/dependency-injection/hilt-android#kts
@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository
) : ViewModel() {

    private val _scannedBeacons =
        MutableLiveData<List<Beacon>>() //customizable beacon list for use only by the view model
    val scannedBeacons: LiveData<List<Beacon>> =
        _scannedBeacons //now copying the list to a public "read-only" list so I can send that back
    //This way helps alot with editing beacons without overcomplicating who gets the list of said beacons

    private val beaconList = mutableMapOf<String, Beacon>()

    @SuppressLint("MissingPermission")
    fun addBeacon(device: BluetoothDevice, rssi: Int) {
        //device.address is the MAC address of the device
        val beaconId = device.address
        val existingBeacon = beaconList[beaconId] //check if the beacon is already in the list

        if (existingBeacon != null) { //if beacon is in the list just update its signal Strength and last detected time (maybe done even need the time detected)
            existingBeacon.lastDetected = Date()
            existingBeacon.signalStrength = rssi
        } else {
            // Beacon is not already in the list so add it
            val newBeacon = Beacon(
                beaconId = device.address,
                beaconName = device.name ?: "Unknown", //Alot of unknown values but they can be populated later when the user connects
                role = "Unknown",
                uuid = "Unknown",
                major = 0,
                minor = 0,
                createdAt = Date(),
                lastDetected = Date(),
                ownerId = "Unknown",
                signalStrength = rssi,
                isConnected = false,
                note = null,
                status = BeaconStatus.AVAILABLE
            )
            beaconList[beaconId] = newBeacon //add the new beacon to the list under its ID / Mac Address
        }
        //update the list with the new or updated beacon values
        _scannedBeacons.postValue(beaconList.values.toList())
    }

}

//    init {
//        // Simulate fetching data
//        _scannedBeacons.value = listOf(Beacon(
//            beaconId = "beacon1",
//            beaconName = "Beacon #1",
//            role = "Home Automation",
//            uuid = "12345678-1234-1234-1234-123456789abc",
//            major = 1,
//            minor = 101,
//            createdAt = Date(),
//            lastDetected = Date(),
//            ownerId = "user1",
//            signalStrength = -30,
//            isConnected = false,
//            note = null,
//            status = BeaconStatus.AVAILABLE
//        ),
//            Beacon(
//                beaconId = "beacon2",
//                beaconName = "Beacon #2",
//                role = "Motion Detection",
//                uuid = "87654321-4321-4321-4321-987654321def",
//                major = 1,
//                minor = 102,
//                createdAt = Date(),
//                lastDetected = Date(),
//                ownerId = "user1",
//                signalStrength = -75,
//                isConnected = false,
//                note = null,
//                status = BeaconStatus.UNAVAILABLE
//            ),
//            Beacon(
//                beaconId = "beacon3",
//                beaconName = "Beacon #3",
//                role = "Asset Tracking",
//                uuid = "11223344-5566-7788-99aa-bbccddeeff00",
//                major = 1,
//                minor = 103,
//                createdAt = Date(),
//                lastDetected = Date(),
//                ownerId = "user2",
//                signalStrength = -50,
//                isConnected = false,
//                note = null,
//                status = BeaconStatus.AVAILABLE
//            ))
//    }



