package com.example.ezblue.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ezblue.model.ActivityLogs
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.model.Configuration
import com.example.ezblue.model.Visibility
import com.example.ezblue.repositories.ActivityLogsDao
import com.example.ezblue.repositories.ConfigurationRepository
import com.example.ezblue.repositories.BeaconRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.Error
import java.util.Date
import javax.inject.Inject

//First time using Hilt so this should be fun (Based on the setup alone, this will not be fun)
//https://developer.android.com/training/dependency-injection/hilt-android#kts
@HiltViewModel
class BeaconViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val configurationsRepository: ConfigurationRepository,
) : ViewModel() {

    private val _scannedBeacons =
        MutableLiveData<List<Beacon>>() //customizable beacon list for use only by the view model
    val scannedBeacons: LiveData<List<Beacon>> =
        _scannedBeacons //now copying the list to a public "read-only" list so I can send that back
    //This way helps alot with editing beacons without overcomplicating who gets the list of said beacons

    private val _beaconLogs = mutableStateOf<List<ActivityLogs>>(emptyList())
    val beaconLogs = _beaconLogs


    private val connectedGatts = mutableMapOf<String, BluetoothGatt>()
    private val beaconList = mutableMapOf<String, Beacon>()
    private var bluetoothResetAttempts = 0

    fun fetchBeaconConfigurations(
        beaconId: String,
        onSuccess: (Configuration) -> Unit,
        onFailure: (String) -> Unit
    ) {
        return configurationsRepository.getConfiguration(
            userId = FirebaseAuth.getInstance().currentUser!!.uid,
            beaconId = beaconId,
            onSuccess = { configuration ->
                onSuccess(configuration)
            },
            onError = { error ->
                Log.e("BLE_TEST", "Failed to fetch configuration: $error")
                onFailure("Failed to fetch configuration: $error")
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun addBeacon(device: BluetoothDevice, rssi: Int) {
        //device.address is the MAC address of the device
        val beaconId = device.address
        //val Uuid = device.uuids
        val existingBeacon = beaconList[beaconId] //check if the beacon is already in the list

        if (existingBeacon != null) { //if beacon is in the list just update its signal Strength and last detected time (maybe done even need the time detected)
            existingBeacon.lastDetected = Date()
            existingBeacon.signalStrength = rssi
        } else {
            // Beacon is not already in the list so add it
            val newBeacon = Beacon(
                beaconId = device.address,
                beaconName = device.name
                    ?: "Unknown", //Alot of unknown values but they can be populated later when the user connects
                role = "Unknown",
                uuid = "Unknown",
                major = 0,
                minor = 0,
                createdAt = Date(),
                lastDetected = Date(),
                ownerId = "Unknown",
                signalStrength = rssi,
                isConnected = false,
                beaconNote = null,
                status = BeaconStatus.AVAILABLE,
                bluetoothDevice = device
            )
            beaconList[beaconId] =
                newBeacon //add the new beacon to the list under its ID / Mac Address
        }
        //update the list with the new or updated beacon values
        _scannedBeacons.postValue(beaconList.values.toList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun isBeaconConnected(beaconId: String): Boolean {
        var connectedBeacons: List<Beacon> = emptyList()

        withContext(Dispatchers.IO) {
            val result = suspendCancellableCoroutine<List<Beacon>> { cont ->
                beaconRepository.getConnectedBeacons(
                    userId = FirebaseAuth.getInstance().currentUser!!.uid,
                    onSuccess = { beaconList ->
                        cont.resume(beaconList) {}
                    },
                    onError = { error ->
                        Log.e("BLE_TEST", "Failed to fetch connected beacons: $error")
                        cont.resume(emptyList()) {}
                    }
                )
            }

            connectedBeacons = result
        }

        return connectedBeacons.any { it.beaconId == beaconId }
    }

    fun updateBeacon(
        beacon: Beacon,
        parameters: Map<String, String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("TestingStuff", "Updating Beacon: $beacon")

        try {
            beaconRepository.updateBeacon(
                beacon = beacon,
                onSuccess = {
                    updateConfigurations(
                        beacon,
                        parameters,
                        onSuccess = {
                            Log.d("TestingStuff", "Beacon Updated")
                            onSuccess()
                        },
                        onFailure = { e ->
                            onFailure("Failed to update beacon - Error: $e")
                        }
                    )
                },
                onError = { e ->
                    onFailure("Failed to update beacon - Error: $e")
                }
            )


        } catch (e: Error) {
            Log.e("TestingStuff", "There was an error Updating the beacon: ${e.message}")
            onFailure("Failed to update beacon - Error: $e")
        }
    }

    private fun updateConfigurations(
        beacon: Beacon,
        parameters: Map<String, String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        configurationsRepository.updateConfiguration(
            userId = FirebaseAuth.getInstance().currentUser!!.uid,
            beaconId = beacon.beaconId,
            parameters = parameters,
            visibility = if (beacon.role == "Open Links") Visibility.PUBLIC.name else Visibility.PRIVATE.name,
            onSuccess = {
                Log.d("BigTest", "Configuration Updated")
                onSuccess()
            },
            onError = { e ->
                Log.e("BigTest", "Failed to update configuration - Error: $e")
                onFailure("Failed to update configuration - Error: $e")
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun connectToBeacon(
        beacon: Beacon,
        context: Context,
        parameters: Map<String, String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        Log.e("BeaconSetup", "Entered Connection")

        //Having to reasign the bluetoothDevice since its not being saved to the db and its being passed through the navGraph
        //as a JSON object so the bluetoothDevice is being lost as it is not a serializable object
        //this is fine for the other items that arent saved to the db because they will be updated live anyway, so for the
        //bluetoothDevice I will just have to reassign it here (no big deal really 😎)
        //TODO review as Parcelable may have solved the issue of the bluetoothDevice being lost
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(beacon.beaconId)

        if (bluetoothDevice == null) {
            Log.e("BLE_TEST", "Failed to get BluetoothDevice from MAC address")
            onFailure("Failed to get BluetoothDevice")
            return
        }

        try {

            Log.e("BeaconSetup", "Entered Try")
            val bluetoothGatt = bluetoothDevice.connectGatt(
                context,
                false,
                bluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE
            ) // connect to the device, also inform the device that we are using BLE

            if (bluetoothGatt == null) {
                //Had an issue myself where the bluetoothGattCallback method was not being called, after ALOT of time debugging,
                //It turns out it was a problem with my devices bluetooth cache and a simple reset of the bluetooth fixed it
                //so in order to avoid that ive got a simple reset of the bluetooth adapter here which will reset the users bluetooth
                //its obviously not ideal so I will only try to do it once but if this a more common issue then just my device, I will
                //have to find an alternative solution as this is not a good one
                if (bluetoothResetAttempts == 0) {
                    bluetoothResetAttempts++
                    bluetoothAdapter.disable()
                    Thread.sleep(2000) // give bluetooth the time to turn off
                    bluetoothAdapter.enable()
                    Thread.sleep(2000) // give bluetooth the time to turn on
                    connectToBeacon(
                        beacon,
                        context,
                        parameters,
                        onSuccess,
                        onFailure
                    ) // call the function again to try reconnecting
                } else {
                    Log.e("BLE_TEST", "Failed to connect after Bluetooth reset")
                    onFailure("Failed to connect to beacon - We recommend Bluetooth Services and restarting the app")
                }
                return
            }

            Log.e("BeaconSetup", "Bluetooth Okay")
            bluetoothResetAttempts = 0 // on success we will reset the attempts
            connectedGatts[beacon.beaconId] =
                bluetoothGatt // save the gatt to the list of connected gatts

            val ownerId = FirebaseAuth.getInstance().currentUser!!.uid
            val beaconSetup = beacon.copy(
                isConnected = true,
                lastDetected = Date(),
                ownerId = ownerId,
                bluetoothDevice = bluetoothDevice
            )

            //create the configuration for the beacon and save it to the db
            configurationsRepository.createConfiguration(
                userId = ownerId,
                beaconId = beacon.beaconId,
                parameters = parameters,
                visibility = if (beacon.role == "Open Links") Visibility.PUBLIC.name else Visibility.PRIVATE.name,
                onSuccess = {
                    Log.d("BLE_TEST", "Configuration created successfully")
                },
                onError = {
                    onFailure("Failed to create configuration - Error: $it")
                }
            )

            Log.d("TestingStuff", "Connected to beacon: $beaconSetup")
            //after all is successful, save the beacon to the DB
            beaconRepository.connectToBeacon(beaconSetup, onSuccess, onFailure)
        } catch (e: Error) {
            Log.e("BeaconSetup", "Error connecting: ${e.message}")
            onFailure("Failing to connect to beacon - Error: $e")
        }

    }

    fun deleteBeacon(beacon: Beacon) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.e("TestingStuff", "User not logged in")
            return
        }
            //TODO is there some @transactional annotation I could use
        try {
            beaconRepository.deleteBeacon(
                beacon = beacon,
                userId = user.uid,
                onSuccess = {
                    Log.d("TestingStuff", "Beacon Deleted")
                },
                onError = { e ->
                    Log.e("TestingStuff", "Failed to delete beacon - Error: $e")
                }
            )

            configurationsRepository.deleteConfiguration(
                beaconId = beacon.beaconId,
                userId = user.uid,
                onSuccess = {
                    Log.d("TestingStuff", "Configuration Deleted")
                },
                onError = { e ->
                    Log.e("TestingStuff", "Failed to delete configuration - Error: $e")
                }
            )

        } catch (e: Error) {
            Log.e("TestingStuff", "There was an error Deleting the beacon: ${e.message}")
        }
    }


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BLE_TEST", "Connected to device: ${gatt.device.address}")
                    gatt.discoverServices() // start the discovery for the services
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BLE_TEST", "Disconnected from device: ${gatt.device.address}")
                    gatt.close()

                    //TODO work on reconnect if disconnected
//                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//                    val bluetoothDevice = bluetoothAdapter.getRemoteDevice(gatt.device.address)
//
//                    if (bluetoothDevice != null) {
//                        Log.d("BLE_TEST", "Attempting to reconnect...")
//                        bluetoothDevice.connectGatt(gatt.device.context, false, bluetoothGattCallback)
//                    }
                }

                BluetoothProfile.STATE_CONNECTING -> {
                    Log.d("BLE_TEST", "Connecting to device: ${gatt.device.address}")
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.d("BLE_TEST", "Disconnecting from device: ${gatt.device.address}")
                }

                else -> {
                    Log.d("BLE_TEST", "Unknown GATT state: $newState")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE_TEST", "Services Discovered: ${gatt.services}")
                //readCharacteristic(gatt)
                for (service in gatt.services) {
                    for (characteristic in service.characteristics) {
                        Log.d(
                            "BLE_TEST",
                            "Characteristic ${characteristic.uuid} Properties ${characteristic.properties}"
                        )
                    }
                }
            }
        }
    }

    fun fetchBeaconLogs(beaconId: String, activityLogsDao: ActivityLogsDao) {
        viewModelScope.launch(Dispatchers.IO) {
            val beaconLogs = activityLogsDao.getLogsByBeaconId(beaconId)
            Log.d("TestingStuff", "Beacon Logs: $beaconLogs")
            _beaconLogs.value = beaconLogs
        }
    }

}

