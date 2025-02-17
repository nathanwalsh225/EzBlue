package com.example.ezblue.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ezblue.model.Beacon
import com.example.ezblue.screens.AutomatedMessagingSetupScreen
import com.example.ezblue.screens.BeaconConnectionScreen
import com.example.ezblue.screens.ConnectionsScreen
import com.example.ezblue.screens.HomeScreen
import com.example.ezblue.screens.LoginScreen
import com.example.ezblue.screens.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = "login",
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("connections") {
            ConnectionsScreen(
                navController = navController,
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("connections") { inclusive = true }
                    } //No need to pass viewmodel here since Hilt is dealing with it
                },
                onConnectClick = { beacon ->
                    //Parcelable is a better option than JSON here
                    Log.d("NavGraph", "0 beacon: $beacon")
                    navController.currentBackStackEntry?.savedStateHandle?.set("beacon", beacon)
                    navController.navigate("beaconConnectionScreen")
                }
            )
        }

        composable("beaconConnectionScreen") {

            val beacon =
                navController.previousBackStackEntry?.savedStateHandle?.get<Beacon>("beacon")

            Log.d("NavGraph", "1 beacon: $beacon")

            LaunchedEffect(Unit) {
                if (beacon == null) {
                    navController.navigate("connections") {
                        popUpTo("beaconConnectionScreen") { inclusive = true }
                    }
                }
            }

            BeaconConnectionScreen(
                navController = navController,
                onBackClicked = {
                    navController.navigate("connections") {
                        popUpTo("beaconConnectionScreen") { inclusive = true }
                    }
                },
                beacon = beacon!!,
                onNextClicked = { configuredBeacon ->
                    //Automated Messaging Setup Screen
                    if (configuredBeacon.major == 2) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("beacon", configuredBeacon)
                        navController.navigate("AutomatedMessagingSetupScreen")
                    }

                }
            )
        }

        composable("AutomatedMessagingSetupScreen") {
            val beacon =
                navController.previousBackStackEntry?.savedStateHandle?.get<Beacon>("beacon")

            Log.d("NavGraph", "2 beacon: $beacon")

            AutomatedMessagingSetupScreen(
                navController = navController,
                onBackClicked = {
                    navController.popBackStack()
                },
                beacon = beacon!!,
                onAutomatedMessagingSetupSuccess = {
                    //navController.popBackStack()
                }
            )
        }

    }

}