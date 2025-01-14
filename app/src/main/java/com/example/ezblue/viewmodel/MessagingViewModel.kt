package com.example.ezblue.viewmodel

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ezblue.config.SmsSentReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessagingViewModel @Inject constructor(
    //private val connectionsRepository: ConnectionsRepository
) : ViewModel() {

    private val smsManager = SmsManager.getDefault() ?: throw IllegalStateException("SmsManager not available")

    fun sendMessage(number: String, message: String, context: Context, onSuccess : () -> Unit) {
        val sentIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, SmsSentReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            smsManager.sendTextMessage(number, null, message, sentIntent, null)
            Log.d("TestingStuff", "It send")
            onSuccess() // This will only run if no exception is thrown
        } catch (e: Exception) {
           Log.d("TestingStuff", "Error sending message: ${e.message}")

        }
    }
}