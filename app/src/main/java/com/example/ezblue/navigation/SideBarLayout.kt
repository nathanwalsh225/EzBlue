package com.example.ezblue.navigation

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.data.ScanPreferences
import com.example.ezblue.service.BeaconScanService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithSideBar(
    navController: NavController,
    onContactUsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    ModalNavigationDrawer(
        drawerContent = {
            SideBarContent(
                onContactUsClick = onContactUsClick,
                onCloseDrawer = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onLogoutClick = onLogoutClick,
                onAccountSettingsClick = onAccountSettingsClick,
                onSettingsClick = onSettingsClick,
                context = context
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row (
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("EzBlue")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Spacer(Modifier.size(48.dp))
                    }
                )
            },

            bottomBar = {
                BottomNavigationBar(navController, currentRoute)
            },
        ) { paddingValues ->

            Column(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        }

    }
}

@Composable
fun SideBarContent(
    onContactUsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    context: Context,
    onCloseDrawer: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isScanningEnabledFlow = remember { ScanPreferences.isScanningEnabled(context) }
    val isScanningEnabled by isScanningEnabledFlow.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp)
    ) {


        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedButton(
            onClick = { onSettingsClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings Icon",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("SETTINGS")
        }

        OutlinedButton(
            onClick = { onAccountSettingsClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Account Icon",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("ACCOUNT")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    val newValue = !isScanningEnabled
                    scope.launch {
                        ScanPreferences.setScanningEnabled(context, newValue)
                    }

                    val intent = Intent(context, BeaconScanService::class.java)
                    if (newValue) {
                        context.startService(intent)
                    } else {
                        context.stopService(intent)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isScanningEnabled) "Disable Background Scanning" else "Enable Background Scanning",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isScanningEnabled,
                onCheckedChange = { newValue ->
                    scope.launch {
                        ScanPreferences.setScanningEnabled(context, newValue)
                    }

                    val intent = Intent(context, BeaconScanService::class.java)
                    if (newValue) {
                        context.startService(intent)
                    } else {
                        context.stopService(intent)
                    }
                }
            )
        }


        Spacer(modifier = Modifier.weight(1f)) //Push the logout button to the bottom

        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Button (
                onClick = {
                    onContactUsClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text( text = "Contact Us")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button (
                onClick = {
                    onLogoutClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text( text = "LOGOUT")
            }
        }
    }

}
