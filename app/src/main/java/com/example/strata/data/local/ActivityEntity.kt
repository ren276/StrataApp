package com.example.strata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: Long, // Strava ID
    val title: String,
    val type: String,
    val date: Instant, // Start date/time
    val distance: Float, // Meters
    val movingTime: Int, // Seconds
    val totalElevationGain: Float, // Meters
    val averageSpeed: Float, // Meters/second (for Pace calculation)
    val mapPolyline: String? = null,
    val totalPhotos: Int = 0,
    val photoUrl: String? = null,
    val lastSynced: Instant? = null // When this record was last fetched from Strava
)
