package com.example.ezblue

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.ezblue.repositories.BeaconRepository
import com.example.ezblue.repositories.ConfigurationRepository
import com.example.ezblue.viewmodel.BeaconViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class BeaconViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var beaconViewModel: BeaconViewModel
    private val mockBeaconRepository: BeaconRepository = mockk()
    private val mockConfigRepository: ConfigurationRepository = mockk()

    @Before
    fun setup() {
        beaconViewModel = BeaconViewModel(mockBeaconRepository, mockConfigRepository)
    }

    @Test
    fun `adding beacon updates scanned beacon list`() {
        val beaconId = "00:11:22:33:44:55"
        val rssi = -60
        val mockDevice = mockk<android.bluetooth.BluetoothDevice> {
            every { address } returns beaconId
            every { name } returns "Test Beacon"
        }

        beaconViewModel.addBeacon(mockDevice, rssi)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertTrue(beaconViewModel.scannedBeacons.value!!.any { it.beaconId == beaconId })
    }



}