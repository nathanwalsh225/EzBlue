package com.example.ezblue.repositories

import com.example.ezblue.model.Configurations
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(
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

}