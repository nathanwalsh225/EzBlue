package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.viewmodel.BeaconViewModel
import com.example.ezblue.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    beaconViewModel: BeaconViewModel = hiltViewModel()
) {
    var connectedBeacons by remember { mutableStateOf<List<Beacon>>(emptyList()) }
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    val rssiReadings = mutableListOf<Int>()


    fun smoothRssi(rssi: Int): Int {
        if (rssiReadings.size >= 5) rssiReadings.removeAt(0) //Keep only the last 5 readings not really a need for more then that for the average
        rssiReadings.add(rssi)
        return rssiReadings.average()
            .toInt() //returning the average RSSI reading to prevent sudden jumps in the RSSI value
    }


    @SuppressLint("MissingPermission")
    fun setScanFrequency(rssi: Int, scanCallback: ScanCallback) {
        val scanSettings = when {
            rssi > -60 -> { //closer in range, scan more often
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            }

            rssi in -80..-60 -> { //bit further away so scan less
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()
            }

            else -> { //scan even less for reduced power consumption
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
            }
        }
        //TODO implement filter, only scan for regonized devices *REVIEW*
        bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
    }

    val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = result.device
                val rssi = result.rssi

                Log.d("HomeScreen", "Device: $device, RSSI: $rssi")

                val smoothedRssi = smoothRssi(rssi)

                setScanFrequency(smoothedRssi, this)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result ->
                val smoothedRssi = smoothRssi(result.rssi)
                setScanFrequency(smoothedRssi, this)
            }
        }

        override fun onScanFailed(error: Int) {
            Log.d("ConnectionsViewModel", "Scan failed: $error")
        }
    }


    LaunchedEffect(Unit) {
        while (true) {
            Log.d("HomeScreen", "Scanning")

            //start the scan in a balanced state to check for beacons
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()

            //begin the scan at homescreen
            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)

            delay(10000) //scan for 10 seconds

            bluetoothLeScanner?.stopScan(scanCallback) //stop the scan

            delay(5000) //wait 5 seconds before starting the scan again
        }
    }


    LaunchedEffect(connectedBeacons) {
        //TODO review for performance, ensure multiple scans are not running at the same time
        userViewModel.getConnectedBeacons( //#1 CHECK WHAT THIS RETURNS, COMPARE IT AGAINST THE MAC ADDRESS?
            onBeaconsFetched = { beacons ->
                connectedBeacons = beacons
                Log.d("HomeScreen", "Connected Beacons: $beacons")
            },
            onError = { error ->
                Log.d("HomeScreen", "Error: $error")
            }
        )

        for (beacon in connectedBeacons) {
            Log.d("HomeScreen", "Beacon: $beacon")
            beaconViewModel.fetchBeaconConfigurations(
                beaconId = beacon.beaconId,
                onSuccess = { configuration ->
                    //#3 MAYBE HERE IDK BUT UPDATE THE BEACON LIVE
                    Log.d("HomeScreen", "Configurations: $configuration")
                    beacon.configuration = configuration
                },
                onFailure = { error ->
                    Log.d("HomeScreen", "Error: $error")
                    //TODO Handle error
                }
            )
        }
    }



    MainScreenWithSideBar(
        navController = navController,
        currentRoute = "home",
        onContactUsClick = {},
        onAccountSettingsClick = {},
        onSettingsClick = {},
        onLogoutClick = onLogoutClick
    ) {
        //HomeScreenContent(userViewModel = userViewModel, beaconViewModel = beaconViewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(connectedBeacons) { beacon ->
                BeaconCard(beacon = beacon)
            }
        }

    }
}

@Composable
fun BeaconCard(beacon: Beacon) {

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            //TASK TITLE AND EXPAND/COLLAPSE BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = beacon.beaconName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )

//                    Text(
//                        text = beacon.configuration?.visibility!!.name,
//                        style = MaterialTheme.typography.bodyMedium.copy(
//                                color = MaterialTheme.colorScheme.secondary
//                            )
//                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Text(
                            text = "Strength: ${beacon.signalStrength} dBm",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )

                        Text(
                            text = "Role: ${beacon.role}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }



            AnimatedVisibility(visible = isExpanded) {
                Box(
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Column {
                        Text(
                            text = "Note: ${beacon.note}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Status:")

                        Text(
                            text = "${beacon.status}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                if (beacon.status == BeaconStatus.ONLINE) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Button(
                                onClick = { /*TODO*/ },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                Text(
                                    text = "Configure",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Button(
                                onClick = { /*TODO*/ },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                Text(
                                    text = "More Info",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                        }
                    }


                }
            }
        }


    }
}

//fun updateBeaconStatus(beacon: Beacon, currentSignalStrength: Int?): Beacon {
//    val isOnline = currentSignalStrength != null && currentSignalStrength > -90 //Anything less then 90dBm is considered offline
//    val currentTime = Date()
//
//    return beacon.copy(
//        status = if (isOnline) BeaconStatus.ONLINE else BeaconStatus.OFFLINE,
//        lastDetected = if (isOnline) currentTime else beacon.lastDetected
//    )
//}
