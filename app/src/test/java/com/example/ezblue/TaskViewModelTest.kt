package com.example.ezblue

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.ezblue.viewmodel.TaskViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class TaskViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var taskViewModel: TaskViewModel

    @Before
    fun setup() {
        taskViewModel = TaskViewModel()
    }
//
//    @Test
//    fun `sendMessage logs message success`() = runTest {
//        val mockBeacon = mockk<com.example.ezblue.model.Beacon>(relaxed = true) {
//            every { beaconId } returns "BEACON_123"
//            every { configuration!!.parameters } returns mapOf("message" to "Hello", "contactNumber" to "1234567890")
//        }
//        val mockContext = mockk<android.content.Context>(relaxed = true)
//
//        var successCalled = false
//        taskViewModel.sendMessage(mockBeacon, mockContext, onSuccess = { successCalled = true }, onError = {})
//
//        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
//
//        assertTrue(successCalled)
//    }


}