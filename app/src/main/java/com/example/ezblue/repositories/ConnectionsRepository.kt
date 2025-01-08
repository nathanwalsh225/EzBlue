package com.example.ezblue.repositories

import javax.inject.Inject

class ConnectionsRepository @Inject constructor(
   // private val apiClient: ApiClient
) {

    fun fetchBeacons(): List<String> {
        //apiClient.fetchBeacons()
        return listOf("Beacon 1", "Beacon 2", "Beacon 3")
    }


}