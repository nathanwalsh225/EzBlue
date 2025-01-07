package com.example.ezblue.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithSideBar(
    userName: String,
    userEmail: String,
    navController: NavController,
    onContactUsClick: () -> Unit,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            SideBarContent(
                userName = userName,
                userEmail = userEmail,
                onContactUsClick = onContactUsClick,
                onCloseDrawer = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
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
    userName: String,
    userEmail: String,
    onContactUsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {


}

@Preview
@Composable
fun PreviewSideBar() {
    val navController = rememberNavController()

    MainScreenWithSideBar(
        userName = "John Doe",
        userEmail = "Test@email.com",
        navController = navController,
        onContactUsClick = {},
        currentRoute = "home",
        content = {}
    )
}