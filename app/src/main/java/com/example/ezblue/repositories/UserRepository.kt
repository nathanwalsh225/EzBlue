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

    //Turns out I am an idiot and didn't need to do all of this because I could have just been using the firebase auth to get the user id
    //so I refactored the User table in the DB to instead of generating a new Id when the user is generated, I just use the firebase auth id
    //that firebase generates already for when a user is authenticated - I will keep this here for reference for getting data from the DB for now
    //https://firebase.google.com/docs/firestore/query-data/get-data#kotlin
    //https://stackoverflow.com/questions/71904044/how-to-retrieve-data-from-firestore-and-store-it-to-array-kotlin-android-studi
    fun getUserIdByEmail(email: String, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        val reference = FirebaseFirestore.getInstance().collection(USERS_COLLECTION)

        //Refactored to be on complete listener rather then success listener so the application waits for the data to be retrieved
        reference.whereEqualTo("email", email).get().addOnCompleteListener { task ->
            val document = task.result.documents[0]
            val userId = document.get("userId")
            Log.d("TestingStuff", "DocumentSnapshot data: ${userId}")
            if (userId != null) {
                onSuccess(userId)
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