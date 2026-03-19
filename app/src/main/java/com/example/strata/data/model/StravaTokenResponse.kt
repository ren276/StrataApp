package com.example.strata.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StravaTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("athlete") val athlete: StravaAthlete? = null
)

@Serializable
data class StravaAthlete(
    val id: Long,
    val username: String?,
    @SerialName("firstname") val firstName: String?,
    @SerialName("lastname") val lastName: String?,
    @SerialName("profile") val profileUrl: String?,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val sex: String? = null,
    val weight: Float? = null,
    val age: Int? = null
)
