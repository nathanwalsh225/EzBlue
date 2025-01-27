package com.example.ezblue.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ezblue.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.lifecycle.HiltViewModel
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        //Again, firebase automatically protects the passwords here so no need to unhash it, but I will later for account updates if the user needs it
        firebaseAuth.signInWithEmailAndPassword(email.trim().lowercase(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Login Failed")
                }
            }
    }


    //found an open source hashing library that I can use to hash the password
    //https://github.com/jeremyh/jBCrypt
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        checkEmail(email.trim().lowercase()) { emailInUse -> //Check to make sure email isnt in use (got this from my StudyPath code)
            Log.d("TaskScreen", "Email in use $emailInUse")

            if (emailInUse) {
                Log.d("TaskScreen", "Email already in use")
                onError("Email already in use")
            } else {
                //firebase automatically protects the passwords here, I only need it for my DB
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Getting the users name just to save it here so I can use it in the app
                        val user = firebaseAuth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("$firstName $lastName")
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d("TaskScreen", "User profile updated.")
                                }
                            }

                        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                        userRepository.createUser(user!!.uid ,email, hashedPassword, firstName, lastName, onSuccess, onError) //TODO implement failure listener

                    } else {
                        // Error for any issues in registration
                        println("Registration error: ${task.exception?.message}")
                    }
                }
            }
        }
    }

    fun fetchCurrentUser(): String {
        return firebaseAuth.currentUser!!.uid
    }

    private fun checkEmail(email: String, onResult: (Boolean) -> Unit) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    val emailExists = !signInMethods.isNullOrEmpty()
                    onResult(emailExists)
                } else {
                    onResult(false)
                }
            }
    }
}