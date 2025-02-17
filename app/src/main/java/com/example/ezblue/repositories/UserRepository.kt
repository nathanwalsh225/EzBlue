package com.example.ezblue.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
)  {

    companion object {
       private const val USERS_COLLECTION = "Users"
    }

    //TODO Make sure to implement no duplicate emails, if deleted from Auth on firebase, users can still make new accounts with old emails
    fun createUser(userId: String, email: String, hashedPassword: String, firstName: String, lastName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firestore.collection(USERS_COLLECTION)
            .add(
                mapOf(
                    "userId" to userId, //Rather then use a different Id for the user, I am going to use the same ID that firebase gives the user for authentication
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "password" to hashedPassword,
                    "createdAt" to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date()),
                    "updatedAt" to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to create user")
            }
    }


    //Turns out I am not an idiot! Yay, I was able to repurpose this code to check if the user email already exists in the database before registering
    //https://firebase.google.com/docs/firestore/query-data/get-data#kotlin
    //https://stackoverflow.com/questions/71904044/how-to-retrieve-data-from-firestore-and-store-it-to-array-kotlin-android-studi
    fun getUserIdByEmail(email: String, onSuccess: () -> Unit, onError: (Any) -> Unit) {
        val reference = FirebaseFirestore.getInstance().collection(USERS_COLLECTION)

        reference.whereEqualTo("email", email).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result.documents[0]
                if (document != null) {
                    onSuccess()
                } else {
                    onError("User not found")
                }
            } else {
                onError("Error contacting server: ${task.exception}")
            }
        }
    }

}