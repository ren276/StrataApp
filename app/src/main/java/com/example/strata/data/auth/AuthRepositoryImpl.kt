package com.example.strata.data.auth

import android.net.Uri
import com.example.strata.data.Secrets
import com.example.strata.data.local.TokenManager
import com.example.strata.data.model.StravaAthlete
import com.example.strata.data.remote.StravaApi
import com.example.strata.data.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val tokenManager: TokenManager,
    private val stravaApi: StravaApi,
    private val supabaseRepository: SupabaseRepository
) : AuthRepository {

    private val _isLoggedIn = MutableStateFlow(tokenManager.getAccessToken() != null)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    override val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()
    
    private val _darkMode = MutableStateFlow(tokenManager.getDarkMode())
    override val darkMode: StateFlow<Boolean?> = _darkMode.asStateFlow()

    private val _useMetricUnits = MutableStateFlow(tokenManager.getUseMetricUnits())
    override val useMetricUnits: StateFlow<Boolean> = _useMetricUnits.asStateFlow()
    
    private val _userProfileTrigger = MutableStateFlow(Unit)

    override fun getStravaLoginUrl(): String {
        return Uri.parse("https://www.strava.com/oauth/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", Secrets.STRAVA_CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", Secrets.STRAVA_REDIRECT_URI)
            .appendQueryParameter("approval_prompt", "force")
            .appendQueryParameter("scope", "activity:read_all,read")
            .build()
            .toString()
    }

    override suspend fun handleStravaCallback(uri: Uri): Result<Unit> {
        return try {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                val response = stravaApi.exchangeToken(code)
                tokenManager.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    expiresAt = response.expiresAt
                )
                
                try {
                     val athlete = stravaApi.getAuthenticatedAthlete(response.accessToken)
                     saveAndSyncAthlete(athlete)
                } catch (_: Exception) {
                     response.athlete?.let { athlete ->
                        saveAndSyncAthlete(athlete)
                    }
                }

                _isGuestMode.value = false
                _isLoggedIn.value = true
                Result.success(Unit)
            } else {
                val error = uri.getQueryParameter("error")
                Result.failure(Exception("Auth failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getValidAccessToken(): Result<String> {
        val accessToken = tokenManager.getAccessToken() ?: return Result.failure(Exception("No access token"))
        val expiresAt = tokenManager.getExpiresAt()
        
        if (tokenManager.getAthleteDetails().first == null) {
            try {
                val athlete = stravaApi.getAuthenticatedAthlete(accessToken)
                saveAndSyncAthlete(athlete)
                _userProfileTrigger.value = Unit 
            } catch (_: Exception) {
                // Best-effort athlete profile fetch
            }
        }

        if (System.currentTimeMillis() / 1000 > expiresAt - 300) {
            val refreshToken = tokenManager.getRefreshToken() ?: return Result.failure(Exception("No refresh token"))
            try {
                val response = stravaApi.refreshToken(refreshToken)
                tokenManager.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    expiresAt = response.expiresAt
                )
                return Result.success(response.accessToken)
            } catch (e: Exception) {
                _isLoggedIn.value = false // Token refresh failed, likely revoked
                return Result.failure(e)
            }
        }
        
        return Result.success(accessToken)
    }

    override fun getUserProfile(): UserProfile {
        val profile = tokenManager.getFullAthleteProfile()
        return UserProfile(
            stravaId = profile["stravaId"] as? Long,
            firstName = profile["firstName"] as? String,
            lastName = profile["lastName"] as? String,
            profileUrl = profile["profileUrl"] as? String,
            username = profile["username"] as? String,
            city = profile["city"] as? String,
            state = profile["state"] as? String,
            country = profile["country"] as? String,
            sex = profile["sex"] as? String,
            weight = profile["weight"] as? Float
        )
    }

    override fun setDarkMode(isDark: Boolean?) {
        tokenManager.setDarkMode(isDark)
        _darkMode.value = isDark
    }

    override fun setUseMetricUnits(useMetric: Boolean) {
        tokenManager.setUseMetricUnits(useMetric)
        _useMetricUnits.value = useMetric
    }

    override fun enterGuestMode() {
        _isGuestMode.value = true
    }

    override fun exitGuestMode() {
        _isGuestMode.value = false
    }

    override suspend fun logout() {
        val token = tokenManager.getAccessToken()
        if (token != null) {
            try {
                stravaApi.deauthorize(token)
            } catch (e: Exception) {
            }
        }
        tokenManager.clear()
        _isGuestMode.value = false
        _isLoggedIn.value = false
    }

    // Helper: save to local cache + upsert to Supabase
    private suspend fun saveAndSyncAthlete(athlete: StravaAthlete) {
        tokenManager.saveAthleteDetails(
            stravaId = athlete.id,
            firstName = athlete.firstName,
            lastName = athlete.lastName,
            profileUrl = athlete.profileUrl,
            username = athlete.username,
            city = athlete.city,
            state = athlete.state,
            country = athlete.country,
            sex = athlete.sex,
            weight = athlete.weight
        )
        // Persist to Supabase for cross-device recall
        supabaseRepository.upsertUserProfile(athlete)
    }
}
