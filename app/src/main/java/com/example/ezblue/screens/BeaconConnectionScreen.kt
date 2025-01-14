package com.example.ezblue.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.ui.theme.EzBlueTheme
import com.example.ezblue.viewmodel.ConnectionsViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BeaconConnectionScreen(
    navController: NavController,
    beacon: Beacon,
    connectionsViewModel: ConnectionsViewModel = hiltViewModel(),
    onBackClicked: () -> Unit,
    onNextClicked: (Beacon) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val beaconName = remember { mutableStateOf(beacon.beaconName) }
    val beaconTask = remember { mutableIntStateOf(0) }
    val beaconNote = remember { mutableStateOf("") }
    val beaconRole = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Beacon Set Up")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Spacer(Modifier.size(48.dp))
                },

            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.SpaceBetween, // Add consistent spacing between elements
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Beacon Name",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )


                Text(
                    text = "Max 20 characters",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                    ),
                )



                OutlinedTextField(
                    value = beaconName.value,
                    onValueChange = { beaconName.value = it },
                    placeholder = { Text("Give your new beacon a name...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.secondary,
                        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary
                    ),
                )


                Text(
                    text = "Task",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )



                Text(
                    text = "What will this beacon do?",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                    ),
                )

                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    //https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/FlowRowScope
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        maxItemsInEachRow = 2,
                        verticalArrangement = Arrangement.Center,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            1 to "Home Automation",
                            2 to "Automated Messaging",
                            3 to "Reminders",
                            4 to "Emergency Alerts / Motion Detection",
                            5 to "Open Links"
                        ).forEach { (value, label) ->
                            Button(
                                onClick = {
                                    beaconTask.value = value
                                    beaconRole.value = label
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (beaconTask.value == value)
                                        Color.DarkGray
                                    else
                                        MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(75.dp)
                                    .padding(4.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    label,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }


                Text(
                    text = "Beacon Note",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Max 100 characters",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                    ),
                )



                OutlinedTextField(
                    value = beaconNote.value, //TODO Implement automatic note generation based on task
                    onValueChange = { beaconNote.value = it },
                    placeholder = { Text("Leave a short note to distinguish your beacon...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.secondary,
                        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary
                    ),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        onClick = { onBackClicked() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "BACK",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val configuredBeacon = beacon.copy(
                                beaconName = beaconName.value,
                                note = beaconNote.value,
                                major = beaconTask.value,
                                role = beaconRole.value
                            )

                            //TODO Implement beacon setup
                            onNextClicked(configuredBeacon)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                }

            }
        }
    }
}


@Preview
@Composable
fun BeaconConnectionScreenPreview() {

    // Provide a dummy or default ViewModel instance
    val mockViewModel = ConnectionsViewModel()
    val navController = rememberNavController()
    EzBlueTheme {
        BeaconConnectionScreen(
            navController = navController,
            connectionsViewModel = mockViewModel,
            onBackClicked = {},
            beacon = Beacon(
                beaconId = "HSNN-1234-ABCD-2345",
                beaconName = "Nathans - BLE BEACON", //Alot of unknown values but they can be populated later when the user connects
                role = "Unknown",
                uuid = "Unknown",
                major = 0,
                minor = 0,
                createdAt = Date(),
                lastDetected = Date(),
                ownerId = "Unknown",
                signalStrength = 32,
                isConnected = false,
                note = null,
                status = BeaconStatus.AVAILABLE
            ),
            onNextClicked = {}
        )
    }
}