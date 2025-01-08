package com.example.ezblue.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.navigation.MainScreenWithSideBar
import com.example.ezblue.viewmodel.ConnectionsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConnectionsScreen(
    navController: NavController,
    connectionsViewModel: ConnectionsViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    //Making this a LiveData so I can update it in the ViewModel easier
    val scannedBeacons by connectionsViewModel.scannedBeacons.observeAsState(emptyList())


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
                text = "Connect to a Beacon",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { connectionsViewModel.getBeacons() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        // List of Beacons
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(scannedBeacons) { beacon ->
                BeaconRow(beacon = beacon, onConnectClick = { connectionsViewModel.connectToBeacon(beacon) })
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
            enabled = beacon.status != BeaconStatus.CONNECTED && beacon.status != BeaconStatus.UNAVAILABLE
        ) {
            Text("Connect")
        }
    }
}


//@Preview
//@Composable
//fun ConnectionsScreenPreview() {
//    ConnectionsScreen(
//        navController = rememberNavController(),
//        connectionsViewModel = ConnectionsViewModel()
//    )
//}