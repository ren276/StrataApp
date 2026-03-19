package com.example.strata.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StravaActivity(
    val id: Long,
    val name: String,
    val distance: Float, // Meters
    @SerialName("moving_time") val movingTime: Int, // Seconds
    @SerialName("elapsed_time") val elapsedTime: Int,
    @SerialName("total_elevation_gain") val totalElevationGain: Float,
    val type: String,
    @SerialName("start_date") val startDate: String, // ISO 8601
    @SerialName("start_date_local") val startDateLocal: String,
    @SerialName("average_speed") val averageSpeed: Float,
    @SerialName("max_speed") val maxSpeed: Float,
    val map: StravaMap? = null,
    @SerialName("total_photo_count") val totalPhotoCount: Int = 0,
    val photos: StravaPhotos? = null
)

@Serializable
data class StravaMap(
    val id: String,
    @SerialName("summary_polyline") val summaryPolyline: String?,
    @SerialName("resource_state") val resourceState: Int
)

@Serializable
data class StravaPhotos(
    val primary: StravaPrimaryPhoto? = null,
    val count: Int = 0
)

@Serializable
data class StravaPrimaryPhoto(
    val id: Long? = null,
    val urls: Map<String, String>? = null
)
