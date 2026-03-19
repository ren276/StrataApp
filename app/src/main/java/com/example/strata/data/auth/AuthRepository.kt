package com.example.strata.data.auth

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val stravaId: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profileUrl: String? = null,
    val username: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val sex: String? = null,
    val weight: Float? = null
)

interface AuthRepository {
    val isLoggedIn: StateFlow<Boolean>
    val isGuestMode: StateFlow<Boolean>
    val darkMode: StateFlow<Boolean?>
    val useMetricUnits: StateFlow<Boolean>
    
    fun getStravaLoginUrl(): String
    
    suspend fun handleStravaCallback(uri: Uri): Result<Unit>
    
    suspend fun getValidAccessToken(): Result<String>

    fun getUserProfile(): UserProfile
    
    fun setDarkMode(isDark: Boolean?)
    
    fun setUseMetricUnits(useMetric: Boolean)

    fun enterGuestMode()

    fun exitGuestMode()

    suspend fun logout()
}
