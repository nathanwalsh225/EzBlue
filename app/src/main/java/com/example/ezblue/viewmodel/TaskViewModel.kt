package com.example.ezblue.viewmodel

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ezblue.config.SmsSentReceiver
import com.example.ezblue.model.ActivityLogs
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.LogResults
import com.example.ezblue.roomdb.DatabaseProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

//Going to use this viewmodel for performing most tasks, probably better to keep them in one place
@HiltViewModel
class TaskViewModel @Inject constructor(
    //private val connectionsRepository: ConnectionsRepository
) : ViewModel() {

    private val smsManager = SmsManager.getDefault() ?: throw IllegalStateException("SmsManager not available")

    fun sendMessage(beacon: Beacon, context: Context, onSuccess : () -> Unit, onError: () -> Unit) {
        val database = DatabaseProvider.getRoomDatabase(context) //Getting the Room Database
        val activityLogsDao = database.activityLogsDao() //specifically getting the ActivityLogsDao table

        val sentIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, SmsSentReceiver::class.java), //The SMS Sent Receiver class is a broadcast receiver that listens for the SMS being sent, really useful for testing as it lets me know the exact result of the SMS message
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("TestingStuff", "Send Intent 1: $sentIntent")

        try {
            smsManager.sendTextMessage(
                (beacon.configuration!!.parameters["contactNumber"] ?: throw Error("No number found")).toString(), //Getting the contact number from the configuration
                null,
                (beacon.configuration!!.parameters["message"] ?: throw Error("No message found")).toString(), //Getting the message from the configuration
                sentIntent, //The send intent created above
                null
            )

            viewModelScope.launch(Dispatchers.IO) {
                val id = activityLogsDao.insertLogs( //Once the message has been sent, I want to log it in the room database activity logs table
                    ActivityLogs(
                        0,
                        beacon.beaconId,
                        beacon.role,
                        beacon.configuration!!.parameters["message"] as String + " sent to " + beacon.configuration!!.parameters["contactNumber"] as String,
                        SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(System.currentTimeMillis())),
                        LogResults.SUCCESS
                    )
                )

                if (id > 0) { //TODO find something to do with the return type
                    Log.d("HomeScreen", "Log inserted successfully")
                } else {
                    Log.d("HomeScreen", "Log not inserted")
                }
            }

            Log.d("TestingStuff", "It send")
            onSuccess() // This will only run if no exception is thrown
        } catch (e: Exception) {
           Log.d("TestingStuff", "Error sending message: ${e.message}")
            onError()
        }
    }

}