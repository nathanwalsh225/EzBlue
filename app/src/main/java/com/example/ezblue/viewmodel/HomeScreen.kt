package com.example.ezblue.viewmodel

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.model.Configurations
import com.example.ezblue.model.Visibility
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.ui.theme.EzBlueTheme
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun HomeScreen(
    navController: NavController,
//    userViewModel: UserViewModel,
//    beaconViewModel: BeaconViewModel
) {

    val beacon1Configurations = listOf(
        Configurations(
            configId = "config1",
            beaconId = "beacon1",
            userId = "user1",
            actionId = "action1",
            parameters = mapOf("message" to "Office alert", "priority" to "High"),
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-09-01T12:00:00Z"),
            visibility = Visibility.PUBLIC
        )
    )

    val beacon2Configurations = listOf(
        Configurations(
            configId = "config2",
            beaconId = "beacon2",
            userId = "user1",
            actionId = "action2",
            parameters = mapOf("message" to "Car Alert", "speedLimit" to "50"),
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-09-11T12:00:00Z"),
            visibility = Visibility.PRIVATE
        )
    )

    val beacon3Configurations = listOf(
        Configurations(
            configId = "config3",
            beaconId = "beacon3",
            userId = "user1",
            actionId = "action3",
            parameters = mapOf("message" to "Car Alert", "speedLimit" to "50"),
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-09-11T12:00:00Z"),
            visibility = Visibility.PRIVATE
        )
    )

    val dummyBeaconList = listOf(
        Beacon(
            beaconId = "beacon1",
            beaconName = "Office Beacon",
            role = "Motion Detection",
            uuid = "12345678-1234-1234-1234-123456789abc",
            major = 1,
            minor = 1,
            signalStrength = -65,
            isConnected = false,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-09-01T12:00:00Z"),
            ownerId = "user1",
            lastDetected = null,
            note = "This is a note",
            configurations = beacon1Configurations
        ),
        Beacon(
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
            note = null,
            configurations = beacon2Configurations
        ),
        Beacon(
            beaconId = "beacon3",
            beaconName = "Home Beacon",
            role = "Home Automation",
            uuid = "87654321-4321-4321-4321-987654321abc",
            major = 2,
            minor = 3,
            signalStrength = -80,
            isConnected = false,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-07-01T12:00:00Z"),
            ownerId = "user1",
            lastDetected = null,
            note = "This is a note",
            configurations = beacon3Configurations
        )
    )


    MainScreenWithSideBar(
        userName = "John Doe",
        userEmail = "test@email.com",
        navController = navController,
        currentRoute = "home",
        onContactUsClick = {}
    ) {
        //HomeScreenContent(userViewModel = userViewModel, beaconViewModel = beaconViewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(dummyBeaconList) { beacon ->
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

                    beacon.configurations.forEach { configuration ->
                        Text(
                            text = configuration.visibility.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

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


@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()

    EzBlueTheme {
        HomeScreen(
            navController = navController,
        )
    }
}