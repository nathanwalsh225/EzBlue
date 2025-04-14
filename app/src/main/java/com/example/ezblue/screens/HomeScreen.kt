package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.roomdb.DatabaseProvider
import com.example.ezblue.viewmodel.BeaconViewModel
import com.example.ezblue.viewmodel.TaskViewModel
import com.example.ezblue.viewmodel.UserViewModel
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    onNavigateToBeaconInfoScreen: (Beacon) -> Unit,
    onConfigureBeacon: (Beacon) -> Unit
) {
    val context = LocalContext.current
    var connectedBeacons by userViewModel.connectedBeacons //Beacons connected to the user are being gathered in the userViewModel
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    val filterList = mutableListOf<ScanFilter>()
    val scanRecords = mutableListOf<Int>()


    //An attempt to smooth the RSSI reading so its not as sporadic, its works (kind of) but its the best I have for now
    fun smoothRssi(rssi: Int): Int {
        scanRecords.add(rssi)

        if (scanRecords.size > 10) { //if the scan record has more then 10 values
            val beaconRssi = scanRecords.average().toFloat() //average and return the values
            scanRecords.clear() //then clear the record for the next set of values
            return beaconRssi.toInt()
        } else {
            return rssi
        }
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

                Log.d("ConnectionsViewModel", "Scan result: ${device.address} - RSSI: $rssi")

                val smoothedRssi =
                    smoothRssi(rssi) //attempting to round the RSSI every few seconds to reduce the sparatic RSSI jumps

                val currentTime = System.currentTimeMillis()
                val lastExecutionTime = lastExecutionTimeMap[device.address]
                    ?: 0 //Gets the last time the beacon was scanned

                connectedBeacons = connectedBeacons.map { beacon ->
                    if (beacon.beaconId == device.address) { //mapping the connected beacons to the scanned beacons to compare mac addresses

                        if (currentTime - lastExecutionTime >= debounceTimeMillis) { //Every 5 seconds, allow the beacon to perform a task
                            lastExecutionTimeMap[device.address] = currentTime

                            if (beacon.configuration != null) { //Prevent null crashes, dont do any task until configurations have been loaded

                                taskViewModel.handleBeaconTask(
                                    beacon = beacon,
                                    context = context,
                                    onSuccess = {
                                        //TODO maybe toss in a confirmation message like a toast
                                    },
                                    onError = {

                                    }) //perform the task for the beacon
                            }
                        } else {
                            Log.d("HomeScreen", "Skipping scan for $device.address")
                        }

                        beacon.copy( //Update the Rssi and status of the beacon per scan
                            signalStrength = smoothedRssi,
                            status = if (smoothedRssi > -90) BeaconStatus.ONLINE else BeaconStatus.OFFLINE
                        )
                    } else {
                        beacon //placeholder, dont think Ill need anything here
                    }
                }
            }
        }

        override fun onScanFailed(error: Int) {
            Log.d("ConnectionsViewModel", "Scan failed: $error")
        }
    }

    LaunchedEffect(Unit) {
        //Previous version was causing issues with getting the configurations so I moved the logic to the viewmodel, honestly I dont know why I was trying
        //to do it here in the first place, it was just one of those days
        //fetching the users beacons and configurations Mostly code taken from old project StudyPath
        userViewModel.fetchBeaconsAndConfigurations()

        while (true) {
            //start the scan in a balanced state to check for beacons
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()

            connectedBeacons.map {
                val scanFilter = ScanFilter.Builder()
                    .setDeviceAddress(it.beaconId)
                    .build()

                filterList.add(scanFilter)
            }

            //begin the scan at home screen
            bluetoothLeScanner?.startScan(filterList, scanSettings, scanCallback)

            delay(10000) //scan for 10 seconds

            bluetoothLeScanner?.stopScan(scanCallback) //stop the scan


            delay(3500) //wait 3.5 seconds before starting the scan again (random numbers that suited)
        }
    }

    DisposableEffect(Unit) { //Will stop the scan if the user leaves the screen, just to ensure scanning doesnt continue in the background
        onDispose {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    MainScreenWithSideBar(
        navController = navController,
        currentRoute = "home",
        onContactUsClick = { navController.navigate("contactUs") },
        onAccountSettingsClick = {},
        onSettingsClick = {},
        onLogoutClick = onLogoutClick
    ) {

        if (connectedBeacons.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Search, //TODO replace with bluetooth icon
                    contentDescription = "No Beacons",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No Beacons Connected",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Navigate to beacon scan */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Scan for Beacons", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(connectedBeacons) { beacon ->
                    BeaconCard(
                        beacon = beacon,
                        onNavigateToBeaconInfoScreen = onNavigateToBeaconInfoScreen,
                        onConfigureBeacon = onConfigureBeacon
                    )
                }
            }
        }
    }
}

@Composable
fun BeaconCard(
    beacon: Beacon,
    onNavigateToBeaconInfoScreen: (Beacon) -> Unit,
    onConfigureBeacon: (Beacon) -> Unit
) {

    var isExpanded by remember { mutableStateOf(false) }


    //TODO get images
//    val signalIcon = when {
//        beacon.signalStrength > -40 -> R.drawable.signal_strong
//        beacon.signalStrength > -70 ->  R.drawable.signal_moderate
//        else ->  R.drawable.signal_weak
//    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            //TASK TITLE AND EXPAND/COLLAPSE BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = beacon.beaconName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (beacon.signalStrength >= -90) "${beacon.signalStrength} dBm" else "Beacon Unavailable",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            LinearProgressIndicator(
                                progress = {
                                    (beacon.signalStrength + 100) / 60f // Normalize RSSI to a 0-1 scale
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (beacon.signalStrength > -70) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            )
                        }

                        Text(
                            text = "Role: ${beacon.role}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }

                Box( //TODO why is this invisible?
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Red)
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.padding(8.dp)
                            .size(24.dp),
//                    colors = IconButtonDefaults.iconButtonColors(
//                        containerColor = MaterialTheme.colorScheme.secondary,
//                        contentColor = MaterialTheme.colorScheme.secondary,
//                        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
//                        disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.38f)
//                    )
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
//                            modifier = Modifier
//                                .rotate(if (isExpanded) 180f else 0f)
//                                .animateContentSize(),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 1f) // Force full visibility,

                        )
                    }
                }
            }


            AnimatedVisibility(visible = isExpanded) {
                Box(
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Column {
                        Text(
                            text = "Note: ${beacon.beaconNote}",
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
                                onClick = {
                                    Log.d("NavGraph", "Configure Beacon $beacon")
                                    onConfigureBeacon(beacon) },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(8.dp), // Softer edges
                                elevation = ButtonDefaults.elevatedButtonElevation()
                            ) {
                                Text("Configure")
                            }

                            Button(
                                onClick = {
                                    Log.d("NavGraph", "Murder")
                                    onNavigateToBeaconInfoScreen(beacon) },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(8.dp), // Softer edges
                                elevation = ButtonDefaults.elevatedButtonElevation()
                            ) {
                                Text("More Info")
                            }

                        }
                    }


                }
            }
        }


    }
}

