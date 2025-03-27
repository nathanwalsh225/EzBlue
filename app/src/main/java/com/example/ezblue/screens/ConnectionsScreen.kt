package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.ezblue.viewmodel.BeaconViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat

@SuppressLint("MissingPermission")
@Composable
fun ConnectionsScreen(
    navController: NavController,
    beaconViewModel: BeaconViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onConnectClick: (Beacon) -> Unit
) {
    //Making this a LiveData so I can update it in the ViewModel easier
    val scannedBeacons by beaconViewModel.scannedBeacons.observeAsState(emptyList())
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    val scanning = remember { mutableStateOf(false) }

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

                beaconViewModel.addBeacon(device!!, rssi!!)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { result ->
                    val device = result.device
                    val rssi = result.rssi

                    beaconViewModel.addBeacon(device, rssi)
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
        beaconNote = null
    )

    DisposableEffect(Unit) {
        onDispose {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }


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
                text = if (scanning.value) "Scanning for Beacons..." else "Connect to a Beacon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    if (scanning.value) {
                        bluetoothLeScanner?.stopScan(scanCallback)
                        scanning.value = false
                    } else {
                        bluetoothLeScanner?.startScan(scanCallback)
                        scanning.value = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = if (scanning.value) Icons.Default.Clear else Icons.Default.Refresh,
                    contentDescription = if (scanning.value) "Stop Scan" else "Start Scan",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (scanning.value) "Stop Scan" else "Start Scan")
            }
        }


        if (scannedBeacons.isEmpty() && !scanning.value) {
            //Informing the user to begin a scan
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search, //TODO REPLACE WITH BLUETOOTH ICON
                    contentDescription = "No Beacons",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No beacons detected.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            BeaconRow( //Test beacon used only for checking functionally while using the emulator, will be removed eventually
                beacon = testBeacon,
                onConnectClick = {
                    onConnectClick(testBeacon)
                }
            )

        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
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
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    text = beacon.beaconName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Status: ${beacon.status.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Signal: ${beacon.signalStrength} dBm ($signalDescription)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = signalColor
                )

                Button(
                    onClick = onConnectClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
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
    }
}