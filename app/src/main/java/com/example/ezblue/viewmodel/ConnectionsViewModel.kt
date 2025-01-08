package com.example.ezblue.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.repositories.ConnectionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

//First time using Hilt so this should be fun (Based on the setup alone, this will not be fun)
//https://developer.android.com/training/dependency-injection/hilt-android#kts
@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository
): ViewModel() {

    private val _scannedBeacons = MutableLiveData<List<Beacon>>() //customizable beacon list for use only by the view model
    val scannedBeacons: LiveData<List<Beacon>> = _scannedBeacons //now copying the list to a public "read-only" list so I can send that back
                                                                //This way helps alot with editing beacons without overcomplicating who gets the list of said beacons

    init {
        // Simulate fetching data
        _scannedBeacons.value = listOf(Beacon(
            beaconId = "beacon1",
            beaconName = "Beacon #1",
            role = "Home Automation",
            uuid = "12345678-1234-1234-1234-123456789abc",
            major = 1,
            minor = 101,
            createdAt = Date(),
            lastDetected = Date(),
            ownerId = "user1",
            signalStrength = -30,
            isConnected = false,
            note = null,
            status = BeaconStatus.AVAILABLE
        ),
            Beacon(
                beaconId = "beacon2",
                beaconName = "Beacon #2",
                role = "Motion Detection",
                uuid = "87654321-4321-4321-4321-987654321def",
                major = 1,
                minor = 102,
                createdAt = Date(),
                lastDetected = Date(),
                ownerId = "user1",
                signalStrength = -75,
                isConnected = false,
                note = null,
                status = BeaconStatus.UNAVAILABLE
            ),
            Beacon(
                beaconId = "beacon3",
                beaconName = "Beacon #3",
                role = "Asset Tracking",
                uuid = "11223344-5566-7788-99aa-bbccddeeff00",
                major = 1,
                minor = 103,
                createdAt = Date(),
                lastDetected = Date(),
                ownerId = "user2",
                signalStrength = -50,
                isConnected = false,
                note = null,
                status = BeaconStatus.AVAILABLE
            ))
    }

    fun getBeacons() : List<String> {
       return connectionsRepository.fetchBeacons()
    }

    //Beacon scanning function
    //dummy data atm cause I dont have scanning implemented yet
//    fun startScan() {
//        //clear the list at the start
//        _scannedBeacons.clear()
//        _scannedBeacons.addAll(
//            listOf(
//                Beacon(
//                    beaconId = "beacon1",
//                    beaconName = "Beacon #5",
//                    role = "Home Automation",
//                    uuid = "12345678-1234-1234-1234-123456789abc",
//                    major = 1,
//                    minor = 101,
//                    createdAt = Date(),
//                    lastDetected = Date(),
//                    ownerId = "user1",
//                    signalStrength = -30,
//                    isConnected = false,
//                    note = null,
//                    status = BeaconStatus.AVAILABLE
//                ),
//                Beacon(
//                    beaconId = "beacon2",
//                    beaconName = "Beacon #8",
//                    role = "Motion Detection",
//                    uuid = "87654321-4321-4321-4321-987654321def",
//                    major = 1,
//                    minor = 102,
//                    createdAt = Date(),
//                    lastDetected = Date(),
//                    ownerId = "user1",
//                    signalStrength = -75,
//                    isConnected = false,
//                    note = null,
//                    status = BeaconStatus.UNAVAILABLE
//                )
//            )
//        )
//    }
//
//
    fun connectToBeacon(beacon: Beacon) {
        //TODO Implement beacon connection logic
//        _scannedBeacons.replaceAll {
//            if (it.beaconId == beacon.beaconId) it.copy(status = BeaconStatus.CONNECTED)
//            else it
//        }
    }

}