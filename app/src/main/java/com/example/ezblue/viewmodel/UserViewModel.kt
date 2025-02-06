package com.example.ezblue.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ezblue.model.Beacon
import com.example.ezblue.repositories.BeaconRepository
import com.example.ezblue.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val beaconRepository: BeaconRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    fun getConnectedBeacons(onBeaconsFetched: (List<Beacon>) -> Unit, onError: (String) -> Unit) = beaconRepository.getConnectedBeacons(
        userId = firebaseAuth.currentUser!!.uid,
        onSuccess = { beacons ->
            onBeaconsFetched(beacons)
        },
        onError = { error ->
            onError(error)
        }
    )

}