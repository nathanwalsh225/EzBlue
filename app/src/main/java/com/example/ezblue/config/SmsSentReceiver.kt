package com.example.ezblue.config

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d("TestingStuff", "SMS sent successfully.")
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                Log.d("TestingStuff", "2")
            }
            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                Log.d("TestingStuff", "3")
            }
            SmsManager.RESULT_ERROR_NULL_PDU -> {
                Log.d("TestingStuff", "4")
            }
            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                Log.d("TestingStuff", "5")
            } else -> {
            Log.d("TestingStuff", "Result code: $resultCode")
            }
        }
    }
}