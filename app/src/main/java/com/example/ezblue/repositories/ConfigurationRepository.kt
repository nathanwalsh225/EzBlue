package com.example.ezblue.repositories

import com.example.ezblue.model.Configuration
import com.example.ezblue.model.Visibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val CONFIGURATION_COLLECTION = "Configurations"
    }

    fun createConfiguration(
        userId: String,
        beaconId: String,
        parameters: Map<String, Any>,
        visibility: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseFirestore.getInstance().collection(CONFIGURATION_COLLECTION)
            .add(
                mapOf(
                    "configId" to firestore.collection(CONFIGURATION_COLLECTION).document().id,
                    "userId" to userId,
                    "beaconId" to beaconId,
                    "parameters" to parameters,
                    "createdAt" to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date()),
                    "visibility" to visibility
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to create configuration")
            }
    }

    fun updateConfiguration(
        userId: String,
        beaconId: String,
        parameters: Map<String, Any>,
        visibility: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(CONFIGURATION_COLLECTION)
            .whereEqualTo("beaconId", beaconId) //Getting the document from the db where BeaconID is equal to the beaconId
            .get()
            .addOnSuccessListener { querySnapshot -> //If the query is successful
                if (!querySnapshot.isEmpty) { //just checking if the query is not empty to be safe
                    val docId = querySnapshot.documents[0].id //getting the id of the document to use it for updating the table
                    firestore.collection(CONFIGURATION_COLLECTION)
                        .document(docId) //document ID being used here as the key
                        .update(
                            mapOf(
                                "parameters" to parameters,
                                "visibility" to visibility
                            )
                        )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError("Failed to update Configuration - ${it.message}") }
                } else {
                    onError("Error Updating beacon configuration")
                }
            }
            .addOnFailureListener {
                onError("Failed to fetch Beacon by beaconId - ${it.message}")
            }
    }

    fun deleteConfiguration(beaconId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit ) {
        FirebaseFirestore.getInstance().collection(CONFIGURATION_COLLECTION)
            .whereEqualTo("beaconId", beaconId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { configurations ->
                if (configurations != null) {
                    for (configuration in configurations) {
                        FirebaseFirestore.getInstance().collection(CONFIGURATION_COLLECTION)
                            .document(configuration.id)
                            .delete()
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onError(it.message ?: "Failed to delete configuration")
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to get configuration")
            }
    }

    //https://stackoverflow.com/questions/74934213/how-to-get-list-from-firestore-in-kotlin
    fun getConfiguration(userId: String, beaconId: String, onSuccess: (Configuration) -> Unit, onError: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection(CONFIGURATION_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("beaconId", beaconId)
            .get()
            .addOnSuccessListener { configurations ->
                if (configurations != null ) {
                    for (configuration in configurations) {
                        onSuccess(Configuration(
                            configId = configuration.id,
                            userId = configuration.data["userId"] as String,
                            beaconId = configuration.data["beaconId"] as String,
                            parameters = configuration.data["parameters"] as Map<String, Any>,
                            createdAt = (configuration.data["createdAt"] as String).let { SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(it) }, //Rare Co-pilot W for this line, had issues with parsing from db but co-pilot locked in so we good
                            visibility = Visibility.valueOf(configuration.data["visibility"] as String)
                        ))
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to get configuration")
            }
    }

}