package com.example.ezblue.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Date

@Parcelize
data class Configuration(
    val configId: String,
    val userId: String,
    val beaconId: String,
    //Since the Map has a 'Any' value, I need to annotate it with a RawValue to avoid data loss as Parcelize does not support 'Any' types
    val parameters: Map<String, @RawValue Any>, //What the beacon will actually do
    val createdAt: Date,
    val visibility: Visibility? = Visibility.PRIVATE, //Made an Enum class for Visibility to ensure consistancy
) : Parcelable

