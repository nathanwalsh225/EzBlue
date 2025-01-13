package com.example.ezblue.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.MutableState
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
    val contactName = remember { mutableStateOf("") }
    val contactNumber = remember { mutableStateOf("") }

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

                if (permissionGranted.value) {
                    ContactPickerPopup(context, contactName, contactNumber)
                } else {
                    //this is where the code for just entering a number will go
                }

            }


    }


}
/*
    Had to get some assistance from chatgpt on this one, couldn't find any good documentation to help but this has been modified -
    to fit the needs of the application and quite a bit from what he suggested.
    one change was that he said to put this code in my main Activity but im the human here not him so I call the shots
    Really it was because it seemed unnecessary to put it in the main activity when i could run it here and avoid jumping through hoops using sharedViewModels or anything
 */
@SuppressLint("Range")
@Composable
fun ContactPickerPopup(context: Context, contactName: MutableState<String>, contactNumber: MutableState<String>) {
//The context aswell as the contactName and contactNumber variables are passed to set them when the number is selected
    val contactPickerLauncher = rememberLauncherForActivityResult ( //Initializing the launcher to tell it to expect a result from the activity
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> //when the result has been got we can continue with the code
        if (result.resultCode == RESULT_OK && result.data != null) { //Making sure the result was okay and that there is data populated
            val contactUri = result.data!!.data

            //this contentResolver.query is used to query to the users device contact list using the contactURI that was returned from the activity
            val cursor = context.contentResolver.query(contactUri!!, null, null, null, null)
            cursor?.use { //cursor?.use is to make sure the contact picker has closed
                if (it.moveToFirst()) { //get the first index from the results
                    val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) //assinging the name and number from the CommonDataKinds Library
                    val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) //gpt recommended using "ContactsContract.Contacts" but this worked better for me

                    Log.d("IMTESTING", "Other number : $number, Other name: $name")
                    contactName.value = name //assiging the name and the number to the mutable state variables that i passed so they can be used when the user selects a contact
                    contactNumber.value = number
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val contactPickerIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI) //creating the Intent launcher to inform the device im looking to open the contacts
                contactPickerLauncher.launch(contactPickerIntent) //launch the contact picker method from above in the code
            }
        ) {
            Text(
                text = "Select Contact"
            )
        }

        Text("Please select a contact to send messages to")
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