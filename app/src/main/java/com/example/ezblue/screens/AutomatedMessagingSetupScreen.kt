package com.example.ezblue.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.viewmodel.ConnectionsViewModel
import java.util.Date
import kotlin.contracts.contract

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AutomatedMessagingSetupScreen(
    navController: NavController,
    beacon: Beacon,
    connectionsViewModel: ConnectionsViewModel = hiltViewModel(),
    onBackClicked: () -> Unit,
    onAutomatedMessagingSetupSuccess: () -> Unit
) {

    Log.d("AutomatedMessagingSetupScreen", "AutomatedMessagingSetupScreen: $beacon")
    val context = LocalContext.current
    val permissionGranted = remember { mutableStateOf(false) }
    val requestPermission = remember { mutableStateOf(false) }

    //https://developer.android.com/reference/androidx/core/app/ActivityCompat#requestPermissions
    //https://developer.android.com/training/permissions/requesting#request-permission
    //https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.RequestPermission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) { //This is handling what will happen when the user grants permission
            permissionGranted.value = true
        } else { //vise vera, when they deny permissions

        }
    }

    //Initial check to see if the permission is already granted
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED) {
        permissionGranted.value = true
    } else {
        requestPermission.value = true
    }

    //Was getting crashes because the launcher was being called before it was intitialized, so the work around is to just create another
    //requestPermission variable and use that will tell the launcher when to go
    LaunchedEffect(requestPermission.value) {
        if (requestPermission.value) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }


//    if (!permissionGranted.value) { //This is checking if the permission has been granted
//        // Permission has not been granted
//        //launch the permissions to ask
//        //requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
//    } else {
    if (permissionGranted.value) {
        // Permission have been granted yet
        //Proceed with the rest of the code
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

            Column(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


            }

        }
    }


}


@Preview
@Composable
fun AutomatedMessagingSetupScreenPreview() {
    val navController = rememberNavController()
    val mockViewModel = ConnectionsViewModel()
    AutomatedMessagingSetupScreen(
        navController = navController,
        beacon = Beacon(
            beaconId = "HSNN-1234-ABCD-2345",
            beaconName = "Nathans - BLE BEACON", //Alot of unknown values but they can be populated later when the user connects
            role = "Automated Messaging",
            uuid = "Unknown",
            major = 2,
            minor = 0,
            createdAt = Date(),
            lastDetected = Date(),
            ownerId = "Unknown",
            signalStrength = 32,
            isConnected = false,
            note = null,
            status = BeaconStatus.AVAILABLE
        ),
        connectionsViewModel = mockViewModel,
        onBackClicked = {},
        onAutomatedMessagingSetupSuccess = {}
    )
}