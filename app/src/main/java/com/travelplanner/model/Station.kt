package com.travelplanner.model

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("stationName") val name: String,
    @SerializedName("crsCode") val crs: String,
    val lat: Double? = null,
    val long: Double? = null
)
