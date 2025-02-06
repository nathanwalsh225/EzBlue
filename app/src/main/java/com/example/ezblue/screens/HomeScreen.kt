package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.viewmodel.BeaconViewModel
import com.example.ezblue.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    beaconViewModel: BeaconViewModel = hiltViewModel()
) {
    var connectedBeacons by remember { mutableStateOf<List<Beacon>>(emptyList()) }

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

    LaunchedEffect(connectedBeacons) {
        userViewModel.getConnectedBeacons(
            onBeaconsFetched = { beacons ->
                connectedBeacons = beacons
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
