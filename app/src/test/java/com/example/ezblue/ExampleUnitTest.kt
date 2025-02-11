package com.example.ezblue

import com.example.ezblue.repositories.UserRepository
import com.example.ezblue.screens.cleanNumber
import com.example.ezblue.viewmodel.AuthViewModel
import org.junit.Test

import org.junit.Assert.*
import org.junit.Assert.*
import org.mockito.Mockito.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun mobileNumber_isCleaned() {
        cleanNumber("087 337 5036")?.let {
            assertEquals("+353873375036", it)
        }
    }

//    @Test
//    fun email_isAvailable() {
//        val firebaseAuth = mock(FirebaseAuth::class.java)
//        val userRepository = mock(UserRepository::class.java)
//        val authViewModel = AuthViewModel(userRepository, firebaseAuth)
//
//        //Creating hte behaviour for the inner functions of the unit test
//        `when`(userRepository.getUserIdByEmail(
//            email = "test@example.com",
//            onSuccess = any(),
//            onError = any()
//        )).thenReturn(Unit)
//
//        // Call the method and assert the result
//        assertTrue(authViewModel.isEmailAvailable("test@example.com"))
//    }
}