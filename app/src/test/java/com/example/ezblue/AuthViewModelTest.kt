package com.example.ezblue

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.ezblue.repositories.UserRepository
import com.example.ezblue.viewmodel.AuthViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class AuthViewModelTest {

    //Ensures live updates are run synchronously for testing as opposed to waiting for the android main thread
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var authViewModel: AuthViewModel

    private val mockUserRepository: UserRepository = mockk()
    private val mockFirebaseAuth: FirebaseAuth = mockk()

    @Before
    fun setup() {
        authViewModel = AuthViewModel(mockUserRepository, mockFirebaseAuth)
    }

    @Test
    fun `login success triggers onSuccess`() {
        val email = "test@example.com"
        val password = "password123"
        val mockUser: FirebaseUser = mockk(relaxed = true)

        val mockTask: Task<AuthResult> = Tasks.forResult(mockk())

        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask

        var successCalled = false
        authViewModel.login(email, password, onSuccess = { successCalled = true }, onError = {})

        //This will force the execution of the tasks on the main thread so we can test the results, otherwise they just fail because its waiting
        //on something else from the main thread, in this case the login response
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(successCalled)
    }

    @Test
    fun `login failure triggers onError`() {
        val email = "fail@example.com"
        val password = "wrongpass"

        val mockSignIn: Task<AuthResult> = Tasks.forException(Exception("Login Failed"))

        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns mockSignIn

        var errorMessage = ""
        authViewModel.login(email, password, onSuccess = {}, onError = { errorMessage = it })

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertEquals("Login Failed", errorMessage)
    }

}