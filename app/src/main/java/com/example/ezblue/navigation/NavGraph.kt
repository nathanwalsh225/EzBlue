package com.example.ezblue.navigation

import androidx.compose.runtime.Composable
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
import com.google.gson.Gson

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

        //TODO switch the JSON to a Parcelable for better performance
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
                    //converting the beacon to json string and passing it as an argument for ease of passing through pages
                    navController.navigate("beaconConnectionScreen/${Gson().toJson(beacon)}") {
                        popUpTo("beaconConnectionScreen/${Gson().toJson(beacon)}") {
                            inclusive = true
                        }
                    }
                }
            )

        }

        composable("beaconConnectionScreen/{beacon}") { backStackEntry ->
            //unJsoning the beacon from the json string passed as an argument
            val beacon =
                Gson().fromJson(backStackEntry.arguments?.getString("beacon"), Beacon::class.java)

            BeaconConnectionScreen(
                navController = navController,
                onBackClicked = {
                    navController.popBackStack()
                },
                beacon = beacon,
                onNextClicked = { configuredBeacon ->
                    //Automated Messaging Setup Screen
                    if(configuredBeacon.major == 2) {
                        navController.navigate("AutomatedMessagingSetupScreen/${Gson().toJson(configuredBeacon)}") {
                            popUpTo("AutomatedMessagingSetupScreen/${Gson().toJson(configuredBeacon)}") {
                                inclusive = true
                            }
                        }
                    }

                }
            )
        }

        composable("AutomatedMessagingSetupScreen/{beacon}") { backStackEntry ->
            val beacon =
                Gson().fromJson(backStackEntry.arguments?.getString("beacon"), Beacon::class.java)

            AutomatedMessagingSetupScreen(
                navController = navController,
                onBackClicked = {
                    navController.popBackStack()
                },
                beacon = beacon,
                onAutomatedMessagingSetupSuccess = {
                    //navController.popBackStack()
                }
            )
        }

    }

}