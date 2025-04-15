package com.example.ezblue.viewmodel

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ezblue.config.SmsSentReceiver
import com.example.ezblue.model.ActivityLogs
import com.example.ezblue.model.Beacon
import com.example.ezblue.model.LogResults
import com.example.ezblue.roomdb.DatabaseProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

//Going to use this viewmodel for performing most tasks, probably better to keep them in one place
@HiltViewModel
class TaskViewModel @Inject constructor(
    //private val connectionsRepository: ConnectionsRepository
) : ViewModel() {


    private val smsManager =
        SmsManager.getDefault() ?: throw IllegalStateException("SmsManager not available")

    fun sendMessage(beacon: Beacon, context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        Log.d("HomeScreen", "Sending message")
        val database = DatabaseProvider.getRoomDatabase(context) //Getting the Room Database
        val activityLogsDao =
            database.activityLogsDao() //specifically getting the ActivityLogsDao table

        val sentIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(
                context,
                SmsSentReceiver::class.java
            ), //The SMS Sent Receiver class is a broadcast receiver that listens for the SMS being sent, really useful for testing as it lets me know the exact result of the SMS message
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        viewModelScope.launch(Dispatchers.IO) { //REVIEW MAKE SURE ITS WAITING FOR THE RESULT
            try {
                smsManager.sendTextMessage(
                    (beacon.configuration!!.parameters["contactNumber"]
                        ?: throw Error("No number found")).toString(), //Getting the contact number from the configuration
                    null,
                    (beacon.configuration!!.parameters["message"]
                        ?: throw Error("No message found")).toString(), //Getting the message from the configuration
                    sentIntent, //The send intent created above
                    null
                )


                activityLogsDao.insertLogs( //Once the message has been sent, I want to log it in the room database activity logs table
                    ActivityLogs(
                        0,
                        beacon.beaconId,
                        beacon.role,
                        beacon.configuration!!.parameters["message"] as String + " sent to " + beacon.configuration!!.parameters["contactNumber"] as String,
                        SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(System.currentTimeMillis())),
                        LogResults.SUCCESS
                    )
                )

                Log.d("HomeScreen", "It send")
                onSuccess() // This will only run if no exception is thrown
            } catch (e: Exception) {
                Log.d("HomeScreen", "Error sending message: ${e.message}")
                viewModelScope.launch(Dispatchers.IO) {
                    activityLogsDao.insertLogs( //Once the message has been sent, I want to log it in the room database activity logs table
                        ActivityLogs(
                            0,
                            beacon.beaconId,
                            beacon.role,
                            beacon.configuration!!.parameters["message"] as String + " sent to " + beacon.configuration!!.parameters["contactNumber"] as String,
                            SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(System.currentTimeMillis())),
                            LogResults.FAILURE
                        )
                    )
                }
                onError()

            }
        }
    }

    private fun sendReminder(beacon: Beacon, context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        val database = DatabaseProvider.getRoomDatabase(context) //Getting the Room Database
        val activityLogsDao =
            database.activityLogsDao()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("HomeScreen", "saving reminder log")

                val builder = NotificationCompat.Builder(context, "reminder")
                    .setSmallIcon(com.example.ezblue.R.drawable.ic_launcher_foreground)
                    .setContentTitle("Reminder")
                    .setContentText(beacon.configuration!!.parameters["reminderMsg"] as String)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL) // for sound/vibration
                    .setAutoCancel(true)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(0, builder.build())


                activityLogsDao.insertLogs(
                    ActivityLogs(
                        0,
                        beacon.beaconId,
                        beacon.role,
                        beacon.configuration!!.parameters["reminderMsg"] as String + " sent successfully",
                        SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(System.currentTimeMillis())),
                        LogResults.SUCCESS
                    )
                )

                Log.d("HomeScreen", "Log inserted")
                onSuccess()


            } catch (e: Exception) {
                Log.d("HomeScreen", "Error sending message: ${e.message}")
                viewModelScope.launch(Dispatchers.IO) {
                    activityLogsDao.insertLogs(
                        ActivityLogs(
                            0,
                            beacon.beaconId,
                            beacon.role,
                            beacon.configuration!!.parameters["reminderMsg"] as String + " failed to send",
                            SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(System.currentTimeMillis())),
                            LogResults.FAILURE
                        )
                    )
                }
                onError()
            }
        }
    }

    fun handleBeaconTask(
        beacon: Beacon,
        context: Context,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        when (beacon.major) {
            1 -> {}
            2 -> {
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d("HomeScreen", "Checking task")
                    if (performMsgTaskCheck(beacon.beaconId, context)) {
                        Log.d("HomeScreen", "Sending")
                        sendMessage(beacon, context, onSuccess, onError)
                    } else {
                        Log.d("HomeScreen", "Already sent")
                        onError()
                    }
                }
            }

            3 -> {
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d("HomeScreen", "Reminder Task")
                    if (performReminderTaskCheck(beacon, context)) {
//                    if (true) {
                        Log.d("TaskViewModel", "Sending Reminder")
                        sendReminder(beacon, context, onSuccess, onError)
                    } else {
                        Log.d("TaskViewModel", "Already sent")
                        onError()
                    }
                }

            }

            4 -> {}
            5 -> {}
            else -> {
                onError()
            }
        }

    }

    private suspend fun performReminderTaskCheck(beacon: Beacon, context: Context): Boolean {
        val database = DatabaseProvider.getRoomDatabase(context)
        val activityLogsDao = database.activityLogsDao()

        return withContext(Dispatchers.IO) { // Making this function a return with context suspend function because I need to wait for the result before continuing with the task
            val activityLogs = activityLogsDao.getLogsByBeaconId(beacon.beaconId)

            val lastLog = activityLogs.filter { it.status == LogResults.SUCCESS }.lastOrNull() //filtering to get the most recent log when it was a success
            if (lastLog == null) {
                Log.d("TaskViewModel", "No logs found for this beacon")
                return@withContext true
            }

            Log.d("TaskViewModel", "Raw timestamp: ${lastLog.timestamp}")

            //need to parse the extracted log date to a date object so I can compare it to the current time
            val extractedLogDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).parse(lastLog.timestamp) // Extracting the date from the logs
            val msgInterval = beacon.configuration?.parameters?.get("msgInterval")

            Log.d("TaskViewModel", "Parsed time millis: ${extractedLogDate.time}")


            Log.d("TaskViewModel", "msgInterval: $msgInterval")
            Log.d("TaskViewModel", "extracted log date: $extractedLogDate")

            if (msgInterval == null || extractedLogDate == null) {
                Log.d("TaskViewModel", "Invalid msgInterval or timestamp")
                return@withContext true
            }

            val intervalMillis = when (msgInterval) {
                "1" -> 15 * 60 * 1000      // 15 minutes
                "2" -> 30 * 60 * 1000      // 30 minutes
                "3" -> 60 * 60 * 1000      // 1 hour
                "4" -> 6 * 60 * 60 * 1000  // 6 hours
                "5" -> 24 * 60 * 60 * 1000 // 24 hours
                else -> 0
            }

            val now = Date()
            val nextAllowedTime = Date(extractedLogDate.time + intervalMillis)
            val shouldSend = now.after(nextAllowedTime)

            Log.d("TaskViewModel", "Last log time: $extractedLogDate, Next allowed time: $nextAllowedTime, Now: $now")
            Log.d("TaskViewModel", "Will reminder send now: $shouldSend")

            return@withContext shouldSend
        }

    }

    private suspend fun performMsgTaskCheck(beaconId: String, context: Context): Boolean {
        val database = DatabaseProvider.getRoomDatabase(context)
        val activityLogsDao = database.activityLogsDao()

        return withContext(Dispatchers.IO) { // Making this function a return with context suspend function because I need to wait for the result before continuing with the task
            val activityLogs = activityLogsDao.getLogsByBeaconId(beaconId)

            val todaysDate =
                SimpleDateFormat("dd-MM-yyyy").format(Date(System.currentTimeMillis())) //Getting the current date to make sure that no logs have been made today on that task

            val lastLog = activityLogs.lastOrNull() //getting the most recent log
            val extractedLogDate = lastLog?.timestamp?.substring(
                0,
                10
            ) // Extracting the values from 0 to 10 to get the date aka "dd-MM-yyyy"

            extractedLogDate != todaysDate //TODO its currently defaulting to one a day so maybe change
        }
    }

}