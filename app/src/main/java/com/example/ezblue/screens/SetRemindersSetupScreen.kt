package com.example.ezblue.screens

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ezblue.model.Beacon
import com.example.ezblue.viewmodel.BeaconViewModel
import com.example.ezblue.viewmodel.TaskViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SetRemindersSetupScreen(
    navController: NavController,
    beacon: Beacon,
    taskViewModel: TaskViewModel = hiltViewModel(),
    beaconViewModel: BeaconViewModel = hiltViewModel(),
    onBackClicked: () -> Unit,
    onSetRemindersComplete: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val permissionGranted = remember { mutableStateOf(false) }
    val requestPermission = remember { mutableStateOf(false) }
    val showCustomTimePicker = remember { mutableStateOf(false) }
    val reminderMsg = remember { mutableStateOf("") }
    var reminderInterval = remember { mutableIntStateOf(0) }
    var customStartTime = remember { mutableStateOf<LocalTime?>(null) }
    var customEndTime = remember { mutableStateOf<LocalTime?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (notificationGranted) { //This is handling what will happen when the user grants permission
            permissionGranted.value = true
        } else { //This is handling what will happen when the user denies permission
            permissionGranted.value = false
        }
    }

    //Checking two permissions so need a check to see which ones are allowed and which ones are not
    val notificationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PermissionChecker.PERMISSION_GRANTED


    //Initial check to see if the permissions are already granted
    if (notificationGranted) {
        permissionGranted.value = true
    } else {
        requestPermission.value = true
    }

    //Was getting crashes because the launcher was being called before it was intitialized, so the work around is to just create another
    //requestPermission variable and use that will tell the launcher when to go
    LaunchedEffect(requestPermission.value) {
        if (requestPermission.value) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }



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
                        text = "Lets set up your reminders",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxSize()
                            .padding(top = 8.dp)
                    )

                    Text(
                        text = "This will allow you to set up reminders for your beacon",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxSize()
                            .padding(top = 8.dp)
                    )
                }

                item {
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
                        value = reminderMsg.value,
                        onValueChange = { reminderMsg.value = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                            .height(50.dp),
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
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Select how often you want this reminder to take place",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxSize()
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                        //https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/FlowRowScope
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            maxItemsInEachRow = 4,
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                1 to "15 Minutes",
                                2 to "30 Minutes",
                                3 to "Hourly",
                                4 to "Once a Day",
                                5 to "Custom *WIP*"
                            ).forEach { (value, label) ->
                                Button(
                                    onClick = {
                                        reminderInterval.intValue = value

                                        if (reminderInterval.intValue == 5) {
                                            showCustomTimePicker.value = true
                                        } else {
                                            showCustomTimePicker.value = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (reminderInterval.intValue == value)
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
                }

                item {
                    if (showCustomTimePicker.value) {
                        CustomTimePicker(
                            onStartTimeSelected = { selectedTime ->
                                Log.d("Reminders", "Start Time: $selectedTime")
                                customStartTime.value = selectedTime
                            },
                            onEndTimeSelected = { selectedTime ->
                                Log.d("Reminders", "End Time: $selectedTime")
                                customEndTime.value = selectedTime
                            }
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
//                                onNextClicked.value = !onNextClicked.value
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePicker(
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit
) {
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }

    Column {
        TimePickerButton("Start", startTime) { selectedTime -> startTime = selectedTime }
        Spacer(modifier = Modifier.height(8.dp))
        TimePickerButton("End", endTime) { selectedTime -> endTime = selectedTime }
    }

}

@Composable
fun TimePickerButton(
    label: String,
    time: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    val timePicker = remember { mutableStateOf<MaterialTimePicker?>(null) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Button(onClick = {
        Log.d("Reminders", "Button clicked for $label")
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(time?.hour ?: 12)
            .setMinute(time?.minute ?: 0)
            .setTitleText("Select $label Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val selected = LocalTime.of(picker.hour, picker.minute)
            onTimeSelected(selected)
        }

        val activity = context as? FragmentActivity
        Log.d("Reminders", "Context is: ${context::class.java.name}")
        activity?.supportFragmentManager?.let {
            picker.show(it, "TIME_PICKER_$label")
        }

        timePicker.value = picker
    }) {
        Text("$label: ${time?.format(timeFormatter) ?: "--:--"}")
    }
}
