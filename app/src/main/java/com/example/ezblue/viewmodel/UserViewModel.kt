package com.example.ezblue.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ezblue.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    fun fetchCurrentUser(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            return userId
        } else {
            return "No user found"
        }
    }

}