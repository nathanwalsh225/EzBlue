package com.example.ezblue.worker

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.ezblue.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO
// 1. Implement the logic to handle the scan results and perform tasks on the beacons
// 2. Need to work on a solution for users to be considered logged in so I can get the correct tasks
class BeaconWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    //var connectedBeacons by userViewModel.connectedBeacons



    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = suspendCoroutine { cont ->
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (bluetoothLeScanner == null) {
            Log.e("EzBlueWorker", "BLE scanner is null")
            return@suspendCoroutine
        }

        val scanCallback = object : ScanCallback() {
            //Was having some big issues with the scan callback
            //It was scanning way too fast so beacon actions were being performed multiple times and logs were being duplicated as a result
            //I had to implement a debounce system to prevent the scan from happening too often
            //Inspiration from ChatGpt
            private val lastExecutionTimeMap =
                mutableMapOf<String, Long>() //map to keep track of the last time a beacon was scanned
            private val debounceTimeMillis = 5000L // 5 seconds

            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    val device = result.device
                    val rssi = result.rssi

//                    val smoothedRssi =
//                        smoothRssi(rssi) //attempting to round the RSSI every few seconds to reduce the sparatic RSSI jumps

//                    val currentTime = System.currentTimeMillis()
//                    val lastExecutionTime = lastExecutionTimeMap[device.address]
//                        ?: 0 //Gets the last time the beacon was scanned

//                    connectedBeacons = connectedBeacons.map { beacon ->
//                        if (beacon.beaconId == device.address) { //mapping the connected beacons to the scanned beacons to compare mac addresses
//
//                            if (currentTime - lastExecutionTime >= debounceTimeMillis) { //Every 5 seconds, allow the beacon to perform a task
//                                lastExecutionTimeMap[device.address] = currentTime
//
//                                if (beacon.configuration != null) { //Prevent null crashes, dont do any task until configurations have been loaded
//                                    taskViewModel.handleBeaconTask(
//                                        beacon = beacon,
//                                        context = context,
//                                        onSuccess = {
//
//                                        },
//                                        onError = {
//
//                                        }) //perform the task for the beacon
//                                }
//                            }
//                        }
//                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                cont.resume(Result.failure())
            }
        }

        bluetoothLeScanner.startScan(scanCallback)

        // Stop after 5s
        CoroutineScope(Dispatchers.IO).launch {
            delay(5000L)
            bluetoothLeScanner.stopScan(scanCallback)
            cont.resume(Result.success())
        }
    }

//    fun runBackgroundScan() {
//        // This method will be called to perform the background scan
//
//        HomeScreen.javaClass.getDeclaredMethod("startScanningForBeacons")
//            .invoke(HomeScreen())
//
//        // Your background scan logic here
//        // For example, you can call the method to start scanning for beacons
//    }
}