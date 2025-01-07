package com.example.ezblue.viewmodel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ezblue.navigation.NavGraph
import com.example.ezblue.ui.theme.EzBlueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EzBlueTheme {

                val navController = rememberNavController()
                NavGraph(navController)
            }
        }
    }
}

