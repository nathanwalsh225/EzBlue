package com.example.ezblue.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ezblue.model.Beacon
import com.example.ezblue.repositories.BeaconRepository
import com.example.ezblue.repositories.ConfigurationRepository
import com.example.ezblue.roomdb.DatabaseProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
    private val beaconRepository: BeaconRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _connectedBeacons = mutableStateOf<List<Beacon>>(emptyList())
    val connectedBeacons = _connectedBeacons

    fun fetchBeaconsAndConfigurations() {
        viewModelScope.launch {
            Log.d("TestingStuff", "fetchBeaconsAndConfigurations")
            getConnectedBeacons( //Getting the beacons that the user is connected to
                onBeaconsFetched = { beacons ->
                    viewModelScope.launch { //with each beacon, get the configuration
                        val updatedBeacons =
                            beacons.toMutableList() //temp list to hold the updated beacons
                        updatedBeacons.forEach { beacon ->
                            configurationRepository.getConfiguration( //get the configuration for the beacon
                                beaconId = beacon.beaconId,
                                userId = FirebaseAuth.getInstance().currentUser!!.uid,
                                onSuccess = { configuration ->
                                    Log.d("TestingStuff", "Configuration: $configuration")
                                    beacon.configuration =
                                        configuration //for each configuration, add it to the corresponding beacon
                                    _connectedBeacons.value =
                                        updatedBeacons.toList() //update the list of connected beacons to populate the configuration

                                    Log.d("TestingStuff", "Connected Beacons: $_connectedBeacons")
                                },
                                onError = {
                                    Log.d("UserViewModel", it)
                                }
                            )
                        }
                    }
                },
                onError = {
                    Log.d("UserViewModel", it)
                }
            )
        }

    }

    private fun getConnectedBeacons(
        onBeaconsFetched: (List<Beacon>) -> Unit,
        onError: (String) -> Unit
    ) =
        beaconRepository.getConnectedBeacons(
            userId = firebaseAuth.currentUser!!.uid,
            onSuccess = { beacons ->
                onBeaconsFetched(beacons)
            },
            onError = { error ->
                onError(error)
            }
        )

    fun syncBeaconsFromFirebase(context: Context) {
        val database = DatabaseProvider.getRoomDatabase(context)
        val beaconDao = database.beaconDao()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        if (userId.isEmpty()) {
            Log.d("UserViewModel", "User ID is empty")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("beacons")
            .get()
            .addOnSuccessListener { result ->
                val beacons = result.documents.mapNotNull { it.toObject(Beacon::class.java) }

                CoroutineScope(Dispatchers.IO).launch {
                    beaconDao.clear()
                    beaconDao.insertAll(beacons)
                }
            }
    }

}