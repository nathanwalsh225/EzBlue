package com.example.ezblue

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Application-level initialization can be added here if needed
        // Hilt will automatically inject dependencies into the Application using this as its setup class
    }
}