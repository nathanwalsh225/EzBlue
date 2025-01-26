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

    //https://firebase.google.com/docs/firestore/query-data/get-data#kotlin
    //https://stackoverflow.com/questions/71904044/how-to-retrieve-data-from-firestore-and-store-it-to-array-kotlin-android-studi
    fun getUserIdByEmail(email: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val reference = FirebaseFirestore.getInstance().collection(USERS_COLLECTION)

        reference.whereEqualTo("email", email).get().addOnSuccessListener { result ->
            if (result != null) {
                val document = result.documents[0]
                val userId = document.get("userId")
                Log.d("TestingStuff", "User ID: $userId")
                onSuccess(userId.toString())
            } else {
                Log.d("TestingStuff", "No such document")
                onError("No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("TestingStuff", "get failed with ", exception)
            onError(exception.message ?: "Failed to get user")
        }
    }
}