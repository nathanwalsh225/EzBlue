package com.example.ezblue.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.navigation.NavGraph
import com.example.ezblue.ui.theme.EzBlueTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EzBlueTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    GatherPermissions {
                        NavGraph(navController)
                    }
                }
            }
        }
    }

    //Most of this is from StudyPath (One of my previous projects which already has alot of the boiler plate code I need)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun GatherPermissions(content: @Composable () -> Unit) {
        val permissionState = rememberMultiplePermissionsState(
            permissions = buildList { //This is a list of permissions that we need to ask for
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  //ANDROID 12+ if the current SDK version is greater than S (31) then we need to ask for these bluetooth permissions
                    add(Manifest.permission.BLUETOOTH_SCAN)
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                    add(Manifest.permission.BLUETOOTH_ADVERTISE)
                } else {
                    add(Manifest.permission.BLUETOOTH_ADMIN) // For older Android versions
                    add(Manifest.permission.BLUETOOTH)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS) //if the current SDK version is greater than TIRAMISU (33) then we need to ask for this permission
                }
            }
        )

        val requestingPermissions = remember { mutableStateOf(true) }

        // Request permissions when the app is first launched
        LaunchedEffect(permissionState) {
            requestingPermissions.value = true // Start requesting

            // Launch permission request
            permissionState.launchMultiplePermissionRequest()


            //TODO either use this or scrap it

            // Continuously monitor until the user completes permission interaction
            //https://proandroiddev.com/mastering-side-effects-in-jetpack-compose-b7ee46162c01
            //https://medium.com/@tangkegaga/snapshotflow-3895096e6fd9
            //mix of the documentation and chatGPT helped me get this going to create a loop that will keep checking the permissions
            //until the user has either granted or denied them just for a more dynamic experience
//            snapshotFlow { permissionState.allPermissionsGranted }
//                .collect { allGranted ->
//                    if (allGranted) {
//                        requestingPermissions.value = false // Permissions granted
//                    } else {
//                        val shouldShowRationale = permissionState.permissions.any { permission ->
//                            permission.status.shouldShowRationale
//                        }
//                        if (!shouldShowRationale) {
//                            requestingPermissions.value = false // Permissions denied
//                        }
//                    }
//                }
        }

        when {
            permissionState.allPermissionsGranted -> {
                content() // permissions granted so the application can continue
            }
            else -> {
                FallbackUI() // Permissions were denied so inform the user they need to approve them
            }
        }
    }


    @Composable
    fun FallbackUI() { //TODO make this look better

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(100.dp)
            )
            Text(
                text = "Permissions are required to proceed. Please enable them in settings.",
                modifier = Modifier
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

