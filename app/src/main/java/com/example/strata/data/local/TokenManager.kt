package com.example.strata.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        try {
            EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // EncryptedSharedPreferences can fail if the keystore entry is corrupted
            // (e.g. after a factory reset on some devices). Safe recovery: delete the
            // corrupt file and recreate it. User will need to log in again, but
            // we NEVER fall back to plaintext storage.
            context.deleteSharedPreferences("secure_prefs")
            EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String, expiresAt: Long) {
        sharedPreferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("expires_at", expiresAt)
            .apply()
    }

    fun saveAthleteDetails(
        stravaId: Long?,
        firstName: String?,
        lastName: String?,
        profileUrl: String?,
        username: String? = null,
        city: String? = null,
        state: String? = null,
        country: String? = null,
        sex: String? = null,
        weight: Float? = null
    ) {
        sharedPreferences.edit()
            .putLong("athlete_strava_id", stravaId ?: -1L)
            .putString("athlete_firstname", firstName)
            .putString("athlete_lastname", lastName)
            .putString("athlete_profile", profileUrl)
            .putString("athlete_username", username)
            .putString("athlete_city", city)
            .putString("athlete_state", state)
            .putString("athlete_country", country)
            .putString("athlete_sex", sex)
            .putFloat("athlete_weight", weight ?: -1f)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun getExpiresAt(): Long {
        return sharedPreferences.getLong("expires_at", 0)
    }

    fun getAthleteDetails(): Triple<String?, String?, String?> {
        val firstName = sharedPreferences.getString("athlete_firstname", null)
        val lastName = sharedPreferences.getString("athlete_lastname", null)
        val profileUrl = sharedPreferences.getString("athlete_profile", null)
        return Triple(firstName, lastName, profileUrl)
    }

    fun getStravaId(): Long? {
        val id = sharedPreferences.getLong("athlete_strava_id", -1L)
        return if (id == -1L) null else id
    }

    fun getFullAthleteProfile(): Map<String, Any?> {
        return mapOf(
            "stravaId" to getStravaId(),
            "firstName" to sharedPreferences.getString("athlete_firstname", null),
            "lastName" to sharedPreferences.getString("athlete_lastname", null),
            "profileUrl" to sharedPreferences.getString("athlete_profile", null),
            "username" to sharedPreferences.getString("athlete_username", null),
            "city" to sharedPreferences.getString("athlete_city", null),
            "state" to sharedPreferences.getString("athlete_state", null),
            "country" to sharedPreferences.getString("athlete_country", null),
            "sex" to sharedPreferences.getString("athlete_sex", null),
            "weight" to sharedPreferences.getFloat("athlete_weight", -1f).takeIf { it >= 0 }
        )
    }

    fun getUseMetricUnits(): Boolean {
        // default true
        return sharedPreferences.getBoolean("pref_metric_units", true)
    }

    fun setUseMetricUnits(metric: Boolean) {
        sharedPreferences.edit().putBoolean("pref_metric_units", metric).apply()
    }

    fun getDarkMode(): Boolean? {
        return if (sharedPreferences.contains("pref_dark_mode")) {
            sharedPreferences.getBoolean("pref_dark_mode", true)
        } else {
            null // system default
        }
    }

    fun setDarkMode(dark: Boolean?) {
        val editor = sharedPreferences.edit()
        if (dark == null) {
            editor.remove("pref_dark_mode")
        } else {
            editor.putBoolean("pref_dark_mode", dark)
        }
        editor.apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
