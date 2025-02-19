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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var isExpanded by remember { mutableStateOf(false) }
    val beaconLogs = beaconViewModel.beaconLogs
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
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("${beacon.beaconName}'s Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClicked()
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween, // Add consistent spacing between elements
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Status: ${beacon.status}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

            }

            item {

                Row(modifier = Modifier.padding(8.dp)) {
                    ConnectivityGraph(beaconRssi)
                }

                Row(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column {

                        Text(
                            text = "Major: ${beacon.major}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column {
                        Text(
                            text = "Minor: ${beacon.minor}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Activity Log",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                AnimatedVisibility(visible = isExpanded) {
                    ActivityLogsTable(beaconLogs = beaconLogs)
                }

                Button(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = if (isExpanded) "Hide Logs" else "Show Logs",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = { onBackClicked() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
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
        Text("No RSSI Data Available", color = Color.Red)
        return
    }

    // Converting the RSSI values to Points for the graph
    val pointsData = rssiValues.mapIndexed { index, value ->
        Point(index.toFloat(), value)
    }

    // X-Axis configuration
    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp) //distance between the x-axis labels
        .steps(pointsData.size - 1)
        .labelData { i -> "${i}s" } //This is where the {x}s are labeled indicating seconds
        .labelAndAxisLinePadding(15.dp)
        .build()

    // Y-Axis configuration
    val yAxisData = AxisData.Builder()
        .steps(5) //distance between points
        .backgroundColor(MaterialTheme.colorScheme.background)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val yMin = pointsData.minOf { -100f } //minimum value of the y-axis
            val yMax = pointsData.maxOf { -30f } //maximum value of the y-axis
            val yScale = (yMax - yMin) / 5
            ((i * yScale) + yMin).formatToSinglePrecision()
        }
        .build()

    // Line chart data configuration
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    lineStyle = LineStyle(color = MaterialTheme.colorScheme.primary),
                    intersectionPoint = IntersectionPoint(),
                    selectionHighlightPoint = SelectionHighlightPoint(),
                    shadowUnderLine = ShadowUnderLine(),
                    selectionHighlightPopUp = SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = MaterialTheme.colorScheme.background
    )

    // Render the LineChart
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartData = lineChartData
    )
}


@Composable
fun ActivityLogsTable(beaconLogs: MutableState<List<ActivityLogs>>) {

    //
    //val logs = beaconLogs.value

    //Dummy data for testing
    val logs = listOf(
        ActivityLogs(
            logId = 1,
            beaconId = "HSNN-1234-ABCD-2345",
            action = "Home Automation",
            parameters = "Device: Lights, Action: On",
            timestamp = "2024-12-30 14:35",
            status = LogResults.SUCCESS
        ),
        ActivityLogs(
            logId = 2,
            beaconId = "HSNN-1234-ABCD-2345",
            action = "Home Automation",
            parameters = "Device: Heater, Action: Off",
            timestamp = "2024-12-29 17:44",
            status = LogResults.SUCCESS
        ),
        ActivityLogs(
            logId = 3,
            beaconId = "HSNN-1234-ABCD-2345",
            action = "Home Automation",
            parameters = "Device: Lights, Action: On",
            timestamp = "2024-12-28 14:11",
            status = LogResults.SUCCESS
        ),
        ActivityLogs(
            logId = 4,
            beaconId = "HSNN-1234-ABCD-2345",
            action = "Home Automation",
            parameters = "Device: Lights, Action: On",
            timestamp = "2024-12-28 14:10",
            status = LogResults.FAILURE
        ),
        ActivityLogs(
            logId = 5,
            beaconId = "HSNN-1234-ABCD-2345",
            action = "Send Message",
            parameters = "Recipient: Dad",
            timestamp = "2024-12-28 14:10",
            status = LogResults.SUCCESS
        )
    )



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .border(1.dp, Color.White)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeaderCell("Timestamp")
            TableHeaderCell("Action")
            TableHeaderCell("Parameters")
            TableHeaderCell("Status")
        }

        logs.forEach { log ->
            TableRow(log = log)
        }
    }

}

@Composable
fun TableHeaderCell(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .padding(8.dp)
    )
}

@Composable
fun TableRow(log: ActivityLogs) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = log.timestamp,
            fontSize = 12.sp,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
        Text(
            text = log.action,
            fontSize = 12.sp,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
        Text(
            text = log.parameters,
            fontSize = 12.sp,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
        Text(
            text = log.status.name,
            fontSize = 12.sp,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            color = if (log.status.name == LogResults.SUCCESS.name) Color.Green else Color.Red
        )
    }
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