package com.example.ezblue.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.ezblue.model.ActivityLogs
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.BeaconStatus
import com.example.ezblue.model.LogResults
import com.example.ezblue.repositories.ActivityLogsDao
import com.example.ezblue.roomdb.DatabaseProvider
import com.example.ezblue.ui.theme.EzBlueTheme
import com.example.ezblue.viewmodel.BeaconViewModel
import kotlinx.coroutines.delay
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MissingPermission")
@Composable
fun BeaconInfoScreen(
    beacon: Beacon,
    activityLogsDao: ActivityLogsDao,
    onBackClicked: () -> Unit,
    beaconViewModel: BeaconViewModel = hiltViewModel()
) {
    val openDeleteDialog = remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val beaconLogs = beaconViewModel.beaconLogs
//    val beaconLogs = listOf(
//        ActivityLogs(
//            logId = 1,
//            beaconId = "HSNN-1234-ABCD-2345",
//            action = "Device Activation",
//            parameters = "Device: Lights, Action: On",
//            timestamp = "2025-03-26 09:15:23",
//            status = LogResults.SUCCESS
//        ),
//        ActivityLogs(
//            logId = 2,
//            beaconId = "HSNN-1234-ABCD-2345",
//            action = "Signal Check",
//            parameters = "RSSI: -45 dBm",
//            timestamp = "2025-03-26 09:10:12",
//            status = LogResults.SUCCESS
//        ),
//        ActivityLogs(
//            logId = 3,
//            beaconId = "HSNN-1234-ABCD-2345",
//            action = "Device Activation",
//            parameters = "Device: Thermostat, Action: Set 72°F",
//            timestamp = "2025-03-26 09:05:47",
//            status = LogResults.SUCCESS
//        ),
//        ActivityLogs(
//            logId = 4,
//            beaconId = "HSNN-1234-ABCD-2345",
//            action = "Connection Attempt",
//            parameters = "Target: Mobile App",
//            timestamp = "2025-03-26 09:00:19",
//            status = LogResults.FAILURE
//        ),
//        ActivityLogs(
//            logId = 5,
//            beaconId = "HSNN-1234-ABCD-2345",
//            action = "Message Sent",
//            parameters = "Recipient: Admin, Msg: Status Update",
//            timestamp = "2025-03-26 08:55:33",
//            status = LogResults.SUCCESS
//        )
//    )
    val beaconRssi = remember { mutableStateListOf<Float>() }
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    val scanRecords = mutableListOf<Int>()

    val scanCallback = object : ScanCallback() {
        @RequiresApi(35)
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = result.device
                val rssi = result.rssi

                scanRecords.add(rssi)

                if (scanRecords.size > 5) {
                    beaconRssi.add(scanRecords.average().toFloat())
                    scanRecords.clear()

                }
            }
        }


        override fun onScanFailed(error: Int) {
            Log.d("BeaconInfoScreen", "Scan failed: $error")
        }
    }

    LaunchedEffect(Unit) {
        beaconViewModel.fetchBeaconLogs(beacon.beaconId, activityLogsDao)


        while (true) {
            Log.d("BeaconInfoScreen", "Scanning")
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

            val targetAddress = beacon.beaconId

            val scanFilter = ScanFilter.Builder()
                .setDeviceAddress(targetAddress)
                .build()

            val scanFilters = mutableListOf(scanFilter)

            Log.d("BeaconInfoScreen", "Filters: $scanFilters")

            bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
            delay(10000) //scan for 10 seconds

            bluetoothLeScanner?.stopScan(scanCallback)
            Log.d("BeaconInfoScreen", "Stopping Scan")
            Log.d("BeaconInfoScreen", "RSSI List: $beaconRssi")

            delay(1500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${beacon.beaconName} Info",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.secondary
                        )
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween, // Add consistent spacing between elements
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Status: ${beacon.status}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Major: ${beacon.major}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Minor: ${beacon.minor}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (beaconRssi.isNotEmpty()) {
                            ConnectivityGraph(beaconRssi)
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                            Text(
                                text = "No signal strength data available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }


            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    AnimatedVisibility(visible = isExpanded) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 300.dp)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Time",
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Action",
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Status",
                                        modifier = Modifier.weight(0.8f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            items(beaconLogs.value.take(10)) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.timestamp.replace(" ", "\n"), //will split the date and the time so the time can be on a line below the date for visibility
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = log.action,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = log.status.name,
                                        modifier = Modifier.weight(0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (log.status == LogResults.SUCCESS) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }

//
                    Button(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(if (isExpanded) "Hide Logs" else "Show Logs")
                    }
                }


            }

            item{
                Button(
                    onClick = { openDeleteDialog.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Delete Beacon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                when {
                    openDeleteDialog.value -> {
                        DeleteAlert(
                            confirmDelete = {
                                beaconViewModel.deleteBeacon(beacon)
                                onBackClicked()
                            },
                            closeDialog = { openDeleteDialog.value = false }
                        )
                    }
                }
            }


            item {
                Button(
                    onClick = { onBackClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Back",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

//Got the code for this from the github library -> https://github.com/codeandtheory/YCharts
@Composable
fun ConnectivityGraph(rssiValues: List<Float>) {
    if (rssiValues.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Search, //TODO replace with a no signal icon
                contentDescription = "No Signal",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "No RSSI data available",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        return
    }

    // Converting the RSSI values to Points for the graph
    val pointsData = rssiValues.mapIndexed { index, value ->
        Point(index.toFloat(), value)
    }

    //limiting to 10 data points to show on the graph as it was getting too crowded otherwise
    val maxPointsToShow = 10
    val visiblePoints = if (pointsData.size > maxPointsToShow) {
        pointsData.takeLast(maxPointsToShow)
    } else {
        pointsData
    }


    // X-Axis configuration
    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp) //distance between the x-axis labels
        .steps(visiblePoints.size)
        //.labelData { i -> "${i}s" } //This is where the {x}s are labeled indicating seconds
        .labelAndAxisLinePadding(8.dp)
        .build()

    // Y-Axis configuration
    val yAxisData = AxisData.Builder()
        .steps(4) //distance between points
        .backgroundColor(MaterialTheme.colorScheme.background)
        .labelAndAxisLinePadding(8.dp)
        .labelData { i ->
            val yMin =  -100f  //minimum value of the y-axis
            val yMax =  -30f  //maximum value of the y-axis
            val yScale = (yMax - yMin) / 4
            ((i * yScale) + yMin).toInt().toString()
        }
        .build()

    // Line chart data configuration
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = visiblePoints,
                    lineStyle = LineStyle(color = MaterialTheme.colorScheme.primary),
                    intersectionPoint = IntersectionPoint(),
                    //selectionHighlightPoint = SelectionHighlightPoint(),
                    shadowUnderLine = ShadowUnderLine(),
                    //selectionHighlightPopUp = SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = MaterialTheme.colorScheme.background
    )

    // Render the LineChart
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(12.dp)
    ) {
        Text(
            text = "Signal Strength Over Time",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Latest RSSI: ${rssiValues.lastOrNull()?.toInt()} dBm",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .height(180.dp)
                .horizontalScroll(rememberScrollState())
                .width((visiblePoints.size * 40).dp.coerceAtLeast(300.dp))
        ) {
            LineChart(
                lineChartData = lineChartData,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}





@Composable
fun DeleteAlert(confirmDelete: () -> Unit, closeDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /*TODO*/ },
        title = {
            Text(
                text = "Delete Beacon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this beacon?\n\nNote: Deleting a beacon will remove all logs related to the beacon.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        confirmButton = {
            Button(
                onClick = { confirmDelete() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        dismissButton = {
            Button(
                onClick = { closeDialog() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}

@Preview
@Composable
fun BeaconInfoScreenPreview() {

    val context = LocalContext.current
    val database = DatabaseProvider.getRoomDatabase(context)
    val navController = rememberNavController()

    EzBlueTheme {
        BeaconInfoScreen(
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
                beaconNote = null,
                status = BeaconStatus.AVAILABLE
            ),
            activityLogsDao = database.activityLogsDao(),
            onBackClicked = {}
        )
    }
}