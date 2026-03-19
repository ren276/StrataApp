package com.example.strata.data.repository

import com.example.strata.data.SupabaseClient
import com.example.strata.data.model.StravaActivity
import com.example.strata.data.model.StravaAthlete
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityBackup(
    val id: Long,
    val name: String,
    val type: String,
    val distance: Float,
    val start_date: String
)

@Serializable
data class SupabaseUserProfile(
    @SerialName("strava_id") val stravaId: Long,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val username: String? = null,
    @SerialName("profile_url") val profileUrl: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val sex: String? = null,
    val weight: Float? = null
)

class SupabaseRepository {
    private val client = SupabaseClient.client

    suspend fun backupActivities(activities: List<StravaActivity>) {
        try {
            val backupList = activities.map { 
                ActivityBackup(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    distance = it.distance,
                    start_date = it.startDate
                )
            }
            client.from("activities").upsert(backupList)
        } catch (_: Exception) {
            // Supabase backup is best-effort — table may not exist yet
        }
    }

    suspend fun upsertUserProfile(athlete: StravaAthlete) {
        try {
            val profile = SupabaseUserProfile(
                stravaId = athlete.id,
                firstName = athlete.firstName,
                lastName = athlete.lastName,
                username = athlete.username,
                profileUrl = athlete.profileUrl,
                city = athlete.city,
                state = athlete.state,
                country = athlete.country,
                sex = athlete.sex,
                weight = athlete.weight
            )
            client.from("user_profiles").upsert(profile)
        } catch (_: Exception) {
            // Best-effort — table may not be created yet
        }
    }

    suspend fun getUserProfile(stravaId: Long): SupabaseUserProfile? {
        return try {
            client.from("user_profiles")
                .select(Columns.ALL) {
                    filter {
                        eq("strava_id", stravaId)
                    }
                }
                .decodeSingleOrNull<SupabaseUserProfile>()
        } catch (_: Exception) {
            null
        }
    }
}
