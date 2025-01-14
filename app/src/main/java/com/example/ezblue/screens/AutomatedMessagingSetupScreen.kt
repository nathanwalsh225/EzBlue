package com.example.ezblue.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.ui.theme.EzBlueTheme
import com.example.ezblue.viewmodel.ConnectionsViewModel
import com.example.ezblue.viewmodel.MessagingViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AutomatedMessagingSetupScreen(
    navController: NavController,
    beacon: Beacon,
    messagingViewModel: MessagingViewModel = hiltViewModel(),
    onBackClicked: () -> Unit,
    onAutomatedMessagingSetupSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val permissionGranted = remember { mutableStateOf(false) }
    val requestPermission = remember { mutableStateOf(false) }
    val onNextClicked = remember { mutableStateOf(false) }
    val contactName = remember { mutableStateOf("") }
    val contactNumber = remember { mutableStateOf("") }
    var contactMsg by remember { mutableStateOf("") }

    //https://developer.android.com/reference/androidx/core/app/ActivityCompat#requestPermissions
    //https://developer.android.com/training/permissions/requesting#request-permission
    //https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.RequestPermission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val contactsGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false

        if (smsGranted && contactsGranted) { //This is handling what will happen when the user grants permission
            permissionGranted.value = true
        } else { //vise vera, when they deny permissions

        }
    }

    //Checking two permissions so need a check to see which ones are allowed and which ones are not
    val contactsGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PermissionChecker.PERMISSION_GRANTED

    val smsGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PermissionChecker.PERMISSION_GRANTED

    //Initial check to see if the permissions are already granted
    if (smsGranted && contactsGranted) {
        permissionGranted.value = true
    } else {
        requestPermission.value = true
    }

    //Was getting crashes because the launcher was being called before it was intitialized, so the work around is to just create another
    //requestPermission variable and use that will tell the launcher when to go
    LaunchedEffect(requestPermission.value) {
        if (requestPermission.value) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS))
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

        if (!onNextClicked.value) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    item {
                        Text(
                            text = "Select a Contact",
                            style = MaterialTheme.typography.titleLarge
                        )

                        if (permissionGranted.value) {
                            ContactPickerPopup(context, contactName, contactNumber)
                        } else {
                            //this is where the code for just entering a number will go
                            //TODO allow users to enter numbers
                        }
                    }

                    item {
                        Text(
                            text = "What do you want this message to say?",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.size(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        ) {
                            Text(
                                text = "Message",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        OutlinedTextField(
                            value = contactMsg,
                            onValueChange = { contactMsg = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                                .height(350.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.secondary,
                                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.secondary
                            )
                        )

                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
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
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = "Back",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    onNextClicked.value = !onNextClicked.value
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.secondary
                                ),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(text = "Selected Contact:")

                    Text(text = "Name: ${contactName.value}")
                    Text(text = "Number: ${contactNumber.value}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Message Content:")
                    Text(text = contactMsg)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            messagingViewModel.sendMessage(
                                contactNumber.value,
                                contactMsg,
                                onSuccess = {
                                    Toast.makeText(context, "Message Sent", Toast.LENGTH_LONG).show()
                                    //onAutomatedMessagingSetupSuccess()
                                },
                                context = context
                            )
                        },
                        modifier = Modifier
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "TEST",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedButton(
                            onClick = { onNextClicked.value = !onNextClicked.value },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onNextClicked.value = !onNextClicked.value
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
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
}

/*
    Had to get some assistance from chatgpt on this one, couldn't find any good documentation to help but this has been modified -
    to fit the needs of the application and quite a bit from what he suggested.
    one change was that he said to put this code in my main Activity but im the human here not him so I call the shots
    Really it was because it seemed unnecessary to put it in the main activity when i could run it here and avoid jumping through hoops using sharedViewModels or anything
 */
@SuppressLint("Range")
@Composable
fun ContactPickerPopup(
    context: Context,
    contactName: MutableState<String>,
    contactNumber: MutableState<String>
) {
//The context aswell as the contactName and contactNumber variables are passed to set them when the number is selected
    val contactPickerLauncher =
        rememberLauncherForActivityResult( //Initializing the launcher to tell it to expect a result from the activity
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result -> //when the result has been got we can continue with the code
            if (result.resultCode == RESULT_OK && result.data != null) { //Making sure the result was okay and that there is data populated
                val contactUri = result.data!!.data

                //this contentResolver.query is used to query to the users device contact list using the contactURI that was returned from the activity
                val cursor = context.contentResolver.query(contactUri!!, null, null, null, null)
                cursor?.use { //cursor?.use is to make sure the contact picker has closed
                    if (it.moveToFirst()) { //get the first index from the results
                        val number =
                            it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) //assinging the name and number from the CommonDataKinds Library
                        val name =
                            it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) //gpt recommended using "ContactsContract.Contacts" but this worked better for me

                        Log.d("TestingStuff", "Other number : $number, Other name: $name")
                        contactName.value =
                            name //assiging the name and the number to the mutable state variables that i passed so they can be used when the user selects a contact
                        contactNumber.value = cleanNumber(number)

                        Log.d(
                            "TestingStuff",
                            "AFTER: Other number : ${contactNumber}, Other name: $name"
                        )

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
                val contactPickerIntent = Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                ) //creating the Intent launcher to inform the device im looking to open the contacts
                contactPickerLauncher.launch(contactPickerIntent) //launch the contact picker method from above in the code
            }
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Select Contact",
                tint = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = if (contactName.value != "") contactName.value else "Select Contact",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        if (contactName.value != "" && contactNumber.value != "") {

            Button(
                onClick = {
                    contactName.value = ""
                    contactNumber.value = ""
                }
            ) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}

//very basic function to just clean the number so something like 087 653 8986 will be +353876538986
fun cleanNumber(number: String): String {
    return number.replaceFirst("0", "+353").replace(" ", "")
}

fun checkPermissions(): Boolean {
    return false
}


@Preview
@Composable
fun AutomatedMessagingSetupScreenPreview() {
    val navController = rememberNavController()
    val mockViewModel = MessagingViewModel()

    EzBlueTheme {
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
            messagingViewModel = mockViewModel,
            onBackClicked = {},
            onAutomatedMessagingSetupSuccess = {}
        )
    }
}