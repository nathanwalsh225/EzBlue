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

    fun updateBeacon(beacon: Beacon, onSuccess: () -> Unit, onError: (String) -> Unit) {
        //TODO attempt to improve efficiency


        /*
        * Was initially attempting to update the beacon by BeaconId as that was the only definite unique
        * variable in the Beacon table, however unbeknownst to me, Firebase does not read ':' which are contained
        * in the BeaconId as the beaconID is the beacons MacAddress, so I am unfortunately required to do a quick call
        * before hand to actually get the documentID
        */
        firestore.collection(BEACON_COLLECTION)
            .whereEqualTo("beaconId", beacon.beaconId) //Getting the document from the db where BeaconID is equal to the beaconId
            .get()
            .addOnSuccessListener { querySnapshot -> //If the query is successful
                if (!querySnapshot.isEmpty) { //just checking if the query is not empty to be safe
                    val docId = querySnapshot.documents[0].id //getting the id of the document to use it for updating the table
                    firestore.collection(BEACON_COLLECTION)
                        .document(docId) //document ID being used here as the key
                        .update(
                            mapOf(
                                "beaconName" to beacon.beaconName,
                                "beaconNote" to beacon.beaconNote,
                                "lastDetected" to beacon.lastDetected,
                                "major" to beacon.major,
                                "minor" to beacon.minor,
                                "role" to beacon.role,
                                "uuid" to beacon.uuid
                            )
                        )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError("Failed to update Beacon - ${it.message}") }
                } else {
                    onError("No beacon found with beaconId: ${beacon.beaconId}")
                }
            }
            .addOnFailureListener {
                onError("Failed to fetch Beacon by beaconId - ${it.message}")
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