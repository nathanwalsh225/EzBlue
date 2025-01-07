package com.example.ezblue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ezblue.viewmodel.ConnectionsScreen
import com.example.ezblue.viewmodel.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("connections") {
             ConnectionsScreen(navController = navController)
        }
    }

}