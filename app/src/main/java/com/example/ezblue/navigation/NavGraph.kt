package com.example.ezblue.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ezblue.model.Beacon
import com.example.ezblue.roomdb.DatabaseProvider
import com.example.ezblue.screens.AutomatedMessagingSetupScreen
import com.example.ezblue.screens.BeaconConnectionScreen
import com.example.ezblue.screens.BeaconInfoScreen
import com.example.ezblue.screens.ConnectionsScreen
import com.example.ezblue.screens.ContactUsScreen
import com.example.ezblue.screens.HomeScreen
import com.example.ezblue.screens.LoginScreen
import com.example.ezblue.screens.RegisterScreen
import com.example.ezblue.screens.SetRemindersSetupScreen
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("NewApi")
@Composable
fun NavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current
    val database = DatabaseProvider.getRoomDatabase(context)

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
                },
                onNavigateToBeaconInfoScreen = { beacon ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("beacon", beacon)
                    navController.navigate("beaconInfo")
                },
                onConfigureBeacon = { beacon ->
                    Log.d("NavGraph", "Configure Beacon $beacon")
                    navController.currentBackStackEntry?.savedStateHandle?.set("beacon", beacon)
                    navController.currentBackStackEntry?.savedStateHandle?.set("update", true)
                    navController.navigate("beaconConnectionScreen")
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
                    navController.currentBackStackEntry?.savedStateHandle?.set("beacon", beacon)
                    navController.navigate("beaconConnectionScreen")
                }
            )
        }

        composable("contactUs") {
            ContactUsScreen(
                navController = navController,
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable("beaconConnectionScreen") {

            val beacon =
                navController.previousBackStackEntry?.savedStateHandle?.get<Beacon>("beacon")
//            val update =
//                navController.previousBackStackEntry?.savedStateHandle?.get<Boolean>("update")

            Log.d("NavGraph", "Beacon Connection Screen $beacon")

            LaunchedEffect(Unit) {
                if (beacon == null) {
                    navController.navigate("connections") {
                        popUpTo("beaconConnectionScreen") { inclusive = true }
                    }
                }
            }

            Log.d("NavGraph", "Tryuna connect")
            BeaconConnectionScreen(
                navController = navController,
                onBackClicked = {
                    navController.navigate("connections") {
                        popUpTo("beaconConnectionScreen") { inclusive = true }
                    }
                },
                beacon = beacon!!,
                onNextClicked = { configuredBeacon ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "beacon",
                        configuredBeacon
                    )
//                    if (update == true) {
//                        navController.currentBackStackEntry?.savedStateHandle?.set(
//                            "update",
//                            true
//                        )
//                    }

                    when (configuredBeacon.major) {
                        2 -> {
                            Log.d("NavGraph", "Beacon Major 2")
                            //Automated Messaging Setup Screen
                            navController.navigate("AutomatedMessagingSetupScreen")
                        }

                        3 -> {
                            navController.navigate("SetRemindersSetupScreen")
                        }
                    }
                },
            )
        }

        composable("beaconInfo") {
            val beacon =
                navController.previousBackStackEntry?.savedStateHandle?.get<Beacon>("beacon")

            LaunchedEffect(Unit) {
                if (beacon == null) {
                    navController.navigate("home") {
                        popUpTo("beaconInfo") { inclusive = true }
                    }
                }
            }

            BeaconInfoScreen(
                beacon = beacon!!,
                activityLogsDao = database.activityLogsDao(),
                onBackClicked = {
                    navController.navigate("home") {
                        popUpTo("beaconInfo") { inclusive = true }
                    }
                }
            )

        }

        composable("AutomatedMessagingSetupScreen") {
            val beacon =
                navController.previousBackStackEntry?.savedStateHandle?.get<Beacon>("beacon")

            AutomatedMessagingSetupScreen(
                navController = navController,
                onBackClicked = {
                    navController.popBackStack()
                },
                beacon = beacon!!,
                onAutomatedMessagingSetupSuccess = {
                    navController.navigate("home") {
                        popUpTo("AutomatedMessagingSetupScreen") { inclusive = true }
                    }
                }
            )
        }

        composable("SetRemindersSetupScreen") {
            val beacon =
                navController.previousBackStackEntry?.savedStateHandle?.get<Beacon>("beacon")

            SetRemindersSetupScreen (
                navController = navController,
                onBackClicked = {
                    navController.popBackStack()
                },
                beacon = beacon!!,
                onSetRemindersComplete = {
                    navController.navigate("home") {
                        popUpTo("SetRemindersSetupScreen") { inclusive = true }
                    }
                }
            )
        }

    }

}