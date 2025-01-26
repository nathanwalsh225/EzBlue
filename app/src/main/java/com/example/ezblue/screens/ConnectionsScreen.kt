package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.viewmodel.ConnectionsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat

@SuppressLint("MissingPermission")
@Composable
fun ConnectionsScreen(
    navController: NavController,
    connectionsViewModel: ConnectionsViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onConnectClick: (Beacon) -> Unit
) {
    //Making this a LiveData so I can update it in the ViewModel easier
    val scannedBeacons by connectionsViewModel.scannedBeacons.observeAsState(emptyList())
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    var scanning = remember { mutableStateOf(true) }

    //Keeping scan call back up here in the composable rather then making a seperate function below
    //because I needed to keep the same instance of the scan callback to be able to stop the scan, but I also
    //needed to be able to use my connections view model so Its up here for ease of use
    //I dont see any issue with it being here but what do I know
    val scanCallback = remember {
        object : ScanCallback() {
            @RequiresApi(Build.VERSION_CODES.R)
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device = result?.device
                val rssi = result?.rssi

                connectionsViewModel.addBeacon(device!!, rssi!!)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { result ->
                    val device = result.device
                    val rssi = result.rssi
                    connectionsViewModel.addBeacon(device, rssi)
                }
            }

            override fun onScanFailed(error: Int) {
                Log.d("ConnectionsViewModel", "Scan failed: $error")
            }
        }
    }

    val testBeacon = Beacon(
            beaconId = "beacon2",
            beaconName = "Car Beacon",
            role = "Automated Messaging",
            uuid = "12345678-1234-5678-1234-123456789def",
            major = 1,
            minor = 2,
            signalStrength = -70,
            isConnected = true,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-09-11T12:00:00Z"),
            ownerId = "user1",
            lastDetected = null,
            note = null
        )




    MainScreenWithSideBar(
        navController = navController,
        currentRoute = "connections",
        onContactUsClick = {},
        onLogoutClick = onLogoutClick,
        onAccountSettingsClick = {},
        onSettingsClick = {}
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (scanning.value) "Scanning..." else "Connect to a Beacon",
                style = MaterialTheme.typography.headlineMedium
            )

            if (!scanning.value) {
                IconButton(onClick = {
                    if (bluetoothAdapter?.isEnabled == true) {
                        //starting the bluetooth scan
                        bluetoothLeScanner?.startScan(scanCallback)
                        Log.d("ConnectionsViewModel", "Scan started")
                        scanning.value = true;
                    } else {
                        Log.d("ConnectionsViewModel", "Bluetooth Scanning not enabled")
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            } else {
                IconButton(onClick = {
                    //stopping the scan
                    bluetoothLeScanner?.stopScan(scanCallback)
                    Log.d("ConnectionsViewModel", "Scan stopped")
                    scanning.value = false;
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Stop")
                }
            }
        }

        if (scannedBeacons.isEmpty() && !scanning.value) {
            //some text to just inform the user to start scanning
            Text(
                text = "No devices found. Try scanning again.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
            BeaconRow( //Test beacon used only for checking functionally while using the emulator, will be removed eventually
                beacon = testBeacon,
                onConnectClick = {
                    onConnectClick(testBeacon)
                }
            )

        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(scannedBeacons) { beacon ->
                    BeaconRow(
                        beacon = beacon,
                        onConnectClick = {
                            onConnectClick(beacon)
                        }
                    )
                }

            }
        }
    }
}

@Composable
fun BeaconRow(beacon: Beacon, onConnectClick: () -> Unit) {
    val signalDescription = when {
        beacon.signalStrength > -40 -> "Strong Signal"
        beacon.signalStrength > -70 -> "Moderate Signal"
        else -> "Weak Signal"
    }
    val signalColor = when {
        beacon.signalStrength > -40 -> Color.Green
        beacon.signalStrength > -70 -> Color.Yellow
        else -> Color.Red
    }

    //Beacon status will be unavailable if the signal strength is less than -85 or if the beacon is already connected
    beacon.status = when {
        beacon.signalStrength < -85 -> BeaconStatus.UNAVAILABLE
        beacon.status == BeaconStatus.CONNECTED -> BeaconStatus.CONNECTED
        else -> BeaconStatus.AVAILABLE
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = beacon.beaconName, style = MaterialTheme.typography.bodyLarge)
            Text(text = beacon.status.name, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Strength: ${beacon.signalStrength} dBm ($signalDescription)",
                style = MaterialTheme.typography.bodySmall,
                color = signalColor
            )
        }
        Button(
            onClick = onConnectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            enabled = beacon.status != BeaconStatus.CONNECTED && beacon.status != BeaconStatus.UNAVAILABLE && beacon.signalStrength > -85
        ) {
            Text("Connect")
        }
    }
}