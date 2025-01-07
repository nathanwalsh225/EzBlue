package com.example.ezblue.viewmodel

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.ezblue.navigation.MainScreenWithSideBar

@Composable
fun ConnectionsScreen(
    navController: NavController
) {
    MainScreenWithSideBar(
        userName = "John Doe",
        userEmail = "test@email.com",
        navController = navController,
        currentRoute = "connections",
        onContactUsClick = {}
    ) {
        //HomeScreenContent(userViewModel = userViewModel, beaconViewModel = beaconViewModel)
        Text("Connections Screen")

    }

}