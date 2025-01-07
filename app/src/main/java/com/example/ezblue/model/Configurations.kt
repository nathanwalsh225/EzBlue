package com.example.ezblue.model

import java.util.Date

data class Configurations(
    val configId: String,
    val userId: String,
    val beaconId: String,
    val actionId: String,
    val parameters: Map<String, Any>, //What the beacon will actually do
    val createdAt: Date,
    val visibility: Visibility, //Made an Enum class for Visibility to ensure consistancy
)

