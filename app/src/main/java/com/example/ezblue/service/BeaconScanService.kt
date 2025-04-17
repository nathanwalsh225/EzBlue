package com.example.ezblue.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ezblue.R
import com.example.ezblue.config.BeaconTaskHandler
import com.example.ezblue.model.Beacon
import com.example.ezblue.roomdb.DatabaseProvider
import com.example.ezblue.viewmodel.TaskViewModel
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BeaconScanService : Service() {

    private lateinit var handler: BeaconTaskHandler
    //var connectedBeacons by userViewModel.connectedBeacons
    private lateinit var rxBleClient: RxBleClient
    private var scanDisposable: Disposable? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isScanning = true


    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(applicationContext)
        handler = BeaconTaskHandler(applicationContext)
        startForeground(1, createNotification())
        startScanLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanDisposable?.dispose()
        isScanning = false
    }

    private fun createNotification(): Notification {
        val channelId = "beacon_scan_channel"
        val channel = NotificationChannel(
            channelId,
            "Beacon Scan",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("EzBlue Beacon Scanning")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun startScanLoop() {
        Log.d("BeaconWorker", "Starting scan loop...")

        serviceScope.launch {

            val database = DatabaseProvider.getRoomDatabase(applicationContext)
            val connectedBeacons = withContext(Dispatchers.IO) {
                database.beaconDao().getAllBeacons()
            }

            while (isScanning) {
                Log.d("BeaconScanService", "Starting scan...")
                scanDisposable = rxBleClient.scanBleDevices(
                    ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                        { result ->
                            val id = result.bleDevice.macAddress
                            val rssi = result.rssi
                            Log.d("BeaconScanService", "Beacon: $id, RSSI: $rssi")

                            connectedBeacons.forEach { beacon ->
                                if (beacon.beaconId == id) {
                                    if (beacon.configuration != null) {
                                        serviceScope.launch {
                                            handler.handleBeaconTask(
                                                beacon = beacon,
                                                context = applicationContext,
                                                onSuccess = {
                                                    Log.d("ScanService", "Task succeeded")
                                                },
                                                onError = {
                                                    Log.d("ScanService", "Task failed or not needed")
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Log.d("HomeScreen", "Skipping scan for $id")
                                }


                            }


                        },
                        { error ->
                            Log.e("BeaconScanService", "Scan error: ${error.message}")
                        }
                    )

                delay(5000) // Scan for 5 seconds
                scanDisposable?.dispose()
                Log.d("BeaconWorker", "Scan stopped.")

                delay(10000) // wait 10 seconds before restarting
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
