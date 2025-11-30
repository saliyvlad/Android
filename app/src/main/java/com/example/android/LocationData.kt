package com.example.android

import java.util.Date

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val timestamp: Long = Date().time
) {
    fun toJson(): String {
        return """
            {
                "latitude": $latitude,
                "longitude": $longitude,
                "altitude": $altitude,
                "accuracy": $accuracy,
                "timestamp": $timestamp,
                "time": "${Date(timestamp)}"
            }
        """.trimIndent()
    }
}