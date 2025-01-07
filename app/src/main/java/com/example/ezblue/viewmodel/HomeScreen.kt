package com.example.ezblue.viewmodel

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.ui.theme.EzBlueTheme
import java.text.SimpleDateFormat

@Composable
fun HomeScreen(
    navController: NavController,
//    userViewModel: UserViewModel,
//    beaconViewModel: BeaconViewModel
) {

    val dummyBeaconList = listOf(
        Beacon(
            id = "beacon1",
            name = "Office Beacon",
            role = "Motion Detection",
            uuid = "12345678-1234-1234-1234-123456789abc",
            major = 1,
            minor = 1,
            signalStrength = -65,
            isConnected = false,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-09-01T12:00:00Z"),
            ownerId = "user1",
            lastDetected = null,
            note = "This is a note"
        ),
        Beacon(
            id = "beacon2",
            name = "Car Beacon",
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
        ),
        Beacon(
            id = "beacon3",
            name = "Home Beacon",
            role = "Home Automation",
            uuid = "87654321-4321-4321-4321-987654321abc",
            major = 2,
            minor = 3,
            signalStrength = -80,
            isConnected = false,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2021-07-01T12:00:00Z"),
            ownerId = "user1",
            lastDetected = null,
            note = "This is a note"
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
                        text = beacon.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )

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

            }
        }
    }
}


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