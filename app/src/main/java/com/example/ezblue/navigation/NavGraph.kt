package com.example.ezblue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ezblue.screens.ConnectionsScreen
import com.example.ezblue.screens.HomeScreen
import com.example.ezblue.screens.LoginScreen
import com.example.ezblue.screens.RegisterScreen
import com.example.ezblue.viewmodel.ConnectionsViewModel
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
                     }
                 }) //No need to pass viewmodel here since Hilt is dealing with it
        }
    }

}