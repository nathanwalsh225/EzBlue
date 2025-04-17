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
import androidx.core.app.NotificationCompat
import com.example.ezblue.R
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BeaconScanService : Service() {

    private lateinit var rxBleClient: RxBleClient
    private var scanDisposable: Disposable? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isScanning = true

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Beacon Scan",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("EzBlue Beacon Scanning")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun startScanLoop() {
        Log.d("BeaconWorker", "Starting scan loop...")

        serviceScope.launch {
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
                            // triggerAction(id)
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
