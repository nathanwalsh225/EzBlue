package com.example.ezblue.repositories

import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
)  {

    companion object {
       private const val USERS_COLLECTION = "Users"
    }

    fun createUser(email: String, hashedPassword: String, firstName: String, lastName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firestore.collection(USERS_COLLECTION)
            .add(
                mapOf(
                    "userId" to firestore.collection(USERS_COLLECTION).document().id, //auto generated id
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "password" to hashedPassword,
                    "createdAt" to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date()),
                    "updatedAt" to  SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to create user")
            }
    }

}