package com.example.ezblue.model

import java.util.Date

data class Configurations(
    val configId: String,
    val userId: String,
    val beaconId: String,
    val actionId: String?, //Im at half a mind on if this is even needed so maybe the action table might leave us in the near future
    val parameters: Map<String, Any>, //What the beacon will actually do
    val createdAt: Date,
    val visibility: Visibility, //Made an Enum class for Visibility to ensure consistancy
)

