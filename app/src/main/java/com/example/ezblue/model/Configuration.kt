package com.example.ezblue.model

import java.util.Date

data class Configuration(
    val configId: String,
    val userId: String,
    val beaconId: String,
    val parameters: Map<String, Any>, //What the beacon will actually do
    val createdAt: Date,
    val visibility: Visibility? = Visibility.PRIVATE, //Made an Enum class for Visibility to ensure consistancy
)

