package com.example.ezblue.repositories

import android.util.Log
import com.example.ezblue.model.Beacon
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class BeaconRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    companion object {
        private const val BEACON_COLLECTION = "Beacons"
    }

    fun connectToBeacon(beacon: Beacon, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firestore.collection(BEACON_COLLECTION)
            .add(
                mapOf(
                    "beaconId" to beacon.beaconId,
                    "beaconName" to beacon.beaconName,
                    "beaconNote" to beacon.beaconNote,
                    "createdAt" to beacon.createdAt,
                    "lastDetected" to beacon.lastDetected,
                    "major" to beacon.major,
                    "minor" to beacon.minor,
                    "ownerId" to beacon.ownerId,
                    "role" to beacon.role,
                    "uuid" to beacon.uuid //TODO: Sort UID
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError("Failed to connect to Beacon - ${it.message}")
            }
    }


    fun getConnectedBeacons(
        userId: String,
        onSuccess: (List<Beacon>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(BEACON_COLLECTION)
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { result ->
                val beacons = result.toObjects(Beacon::class.java)
                onSuccess(beacons)
            }
            .addOnFailureListener {
                onError("Failed to fetch beacons - try a restart :D")
            }
    }

}