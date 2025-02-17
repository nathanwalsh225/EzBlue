package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.viewmodel.BeaconViewModel
import com.example.ezblue.viewmodel.TaskViewModel
import com.example.ezblue.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    beaconViewModel: BeaconViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var connectedBeacons by remember { mutableStateOf<List<Beacon>>(emptyList()) }
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    val rssiReadings = mutableListOf<Int>()
    var taskCounter = 0

    //Not working as expected
    fun smoothRssi(rssi: Int): Int {
        if (rssiReadings.size >= 5) rssiReadings.removeAt(0) //Keep only the last 5 readings not really a need for more then that for the average
        rssiReadings.add(rssi)
        return rssiReadings.average()
            .toInt() //returning the average RSSI reading to prevent sudden jumps in the RSSI value
    }

    //Also not working as expected :(
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

    //when performBeaconTask is called, much like in navGraph, the major of the beacon will be used to determine the task to be performed
    fun performBeaconTask(beacon: Beacon) {
        when (beacon.major) {
            1 -> {

            }

            2 -> { //Automated Messaging
                Log.d("TestingStuff", "Task Counter $taskCounter")

                //Task Counter is a placerholder at the moment so I dont spam messages until I get logs going
                if (taskCounter == 1) return //testing Only send one message
                Log.d("TestingStuff", "Task Counter $taskCounter")

                try {
                    Log.d("TestingStuff", "Performing task for beacon ${beacon.beaconName}")
                    Log.d("TestingStuff", "Configurations ${beacon.configuration}")
                    taskCounter++

                    taskViewModel.sendMessage(
                        number = beacon.configuration!!.parameters["contactNumber"] as String,
                        message = beacon.configuration!!.parameters["message"] as String,
                        context = context,
                        onSuccess = {
                            Log.d("HomeScreen", "Message sent successfully")
                        }
                    )
                } catch (e: Exception) {
                    Log.d("TestingStuff", "Error sending message: ${e.message}")
                }
            }

            3 -> {

            }

            4 -> {

            }

            5 -> {

            }

            else -> {
                Log.d("HomeScreen", "Beacon ${beacon.beaconName} is not a recognized beacon")
            }
        }
    }

    val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = result.device
                val rssi = result.rssi

                val smoothedRssi = smoothRssi(rssi) //attempting to round the RSSI every few seconds to reduce the sparatic RSSI jumps

                setScanFrequency(smoothedRssi, this) // attempting to set the scan frequency - helps with power consumption (nearer beacons cause scans more often)
                connectedBeacons = connectedBeacons.map { beacon ->
                    if (beacon.beaconId == device.address) { //mapping the connected beacons to the scanned beacons to compare mac addresses

                        if (beacon.configuration != null) { //Prevent null crashes, dont do any task until configurations have been loaded
                            performBeaconTask(beacon) //perform the task for the beacon
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

        override fun onBatchScanResults(results: MutableList<ScanResult>?) { //Review to see is this even needed
            results?.forEach { result ->
                val smoothedRssi = smoothRssi(result.rssi)
                setScanFrequency(smoothedRssi, this)

                connectedBeacons = connectedBeacons.map { beacon ->
                    if (beacon.bluetoothDevice?.address == result.device.address) {
                        beacon.copy(
                            signalStrength = smoothedRssi,
                            status = if (smoothedRssi > -90) BeaconStatus.ONLINE else BeaconStatus.OFFLINE
                        )
                    } else {
                        beacon
                    }
                }
            }
        }

        override fun onScanFailed(error: Int) {
            Log.d("ConnectionsViewModel", "Scan failed: $error")
        }
    }

    LaunchedEffect(Unit) {
        //TODO fix as it requires screen recomposition for the tasks to work
        userViewModel.getConnectedBeacons(
            onBeaconsFetched = { beacons ->
                connectedBeacons = beacons.toMutableList() //Temporary fix for the recomposition issue with the configuration

                for (beacon in connectedBeacons) { //for each beacon fetch the configuration for the tasks
                    beaconViewModel.fetchBeaconConfigurations(
                        beaconId = beacon.beaconId,
                        onSuccess = { configuration ->
                            beacon.configuration = configuration //assign the configuration to the beacon
                            connectedBeacons = connectedBeacons.toList() //then refresh the list to trigger recomposition (not working as expected)
                            Log.d("TestingStuff", "Connected Beacons: $connectedBeacons")
                        },
                        onFailure = { error ->
                            Log.d("HomeScreen", "Error: $error")
                        }
                    )
                }
            },
            onError = { error ->
                Log.d("HomeScreen", "Error: $error")
            }
        )

        Log.d("TestingStuff", "Connected Beacons: $connectedBeacons")


        while (true) {
            Log.d("HomeScreen", "Scanning")
            //start the scan in a balanced state to check for beacons
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()

            //begin the scan at home screen
            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
            Log.d("TestingStuff", "Scanning")
            delay(10000) //scan for 10 seconds

            bluetoothLeScanner?.stopScan(scanCallback) //stop the scan
            Log.d("TestingStuff", "Stopping Scan")

            delay(5000) //wait 5 seconds before starting the scan again
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


    //TODO get images
//    val signalIcon = when {
//        beacon.signalStrength > -40 -> R.drawable.signal_strong
//        beacon.signalStrength > -70 ->  R.drawable.signal_moderate
//        else ->  R.drawable.signal_weak
//    }

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
                        style = MaterialTheme.typography.headlineSmall.copy(
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
                            text = if (beacon.signalStrength >= -90) "Strength: ${beacon.signalStrength} dBm" else "Beacon Unavailable",
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
