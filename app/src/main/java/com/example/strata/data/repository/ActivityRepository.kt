package com.example.strata.data.repository

import com.example.strata.data.auth.AuthRepository
import com.example.strata.data.local.ActivityDao
import com.example.strata.data.local.ActivityEntity
import com.example.strata.data.remote.StrataRateLimitException
import com.example.strata.data.remote.StravaApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit

interface ActivityRepository {
    fun getActivities(): Flow<List<ActivityEntity>>
    fun getActivity(id: Long): Flow<ActivityEntity?>
    suspend fun refreshActivities()
    suspend fun fetchActivityDetails(id: Long)
    suspend fun clearActivitiesCache()
    /** Seed realistic demo activities into the local DB for guest / unauthenticated mode. */
    suspend fun seedMockActivities()
}

class ActivityRepositoryImpl(
    private val activityDao: ActivityDao,
    private val stravaApi: StravaApi,
    private val authRepository: AuthRepository,
    private val supabaseRepository: SupabaseRepository
) : ActivityRepository {

    override fun getActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    override fun getActivity(id: Long): Flow<ActivityEntity?> = activityDao.getActivity(id)
    override suspend fun clearActivitiesCache() = activityDao.clearAll()

    /**
     * Insert 5 realistic demo activities into the local Room DB so the guest UI
     * has something to display without touching Strava or Supabase.
     * Uses negative IDs (guaranteed never to clash with real Strava IDs which are positive).
     *
     * Safe to call multiple times — uses upsert semantics (insertAll with REPLACE strategy).
     */
    override suspend fun seedMockActivities() {
        val now = Instant.now()
        val mockActivities = listOf(
            ActivityEntity(
                id = -1L,
                title = "Morning Run – Riverside Loop",
                type = "Run",
                date = now.minus(1, ChronoUnit.DAYS),
                distance = 8_430f,          // 8.43 km
                movingTime = 2_520,         // 42 min
                totalElevationGain = 54f,
                averageSpeed = 3.346f,      // ~5:00 /km
                lastSynced = null           // null = not from Strava; stable in DB
            ),
            ActivityEntity(
                id = -2L,
                title = "Sunday Long Run",
                type = "Run",
                date = now.minus(3, ChronoUnit.DAYS),
                distance = 21_097f,         // half marathon
                movingTime = 6_900,         // 115 min
                totalElevationGain = 210f,
                averageSpeed = 3.058f,      // ~5:27 /km
                lastSynced = null
            ),
            ActivityEntity(
                id = -3L,
                title = "Evening Ride – Coastal Path",
                type = "Ride",
                date = now.minus(5, ChronoUnit.DAYS),
                distance = 35_200f,         // 35.2 km
                movingTime = 4_800,         // 80 min
                totalElevationGain = 320f,
                averageSpeed = 7.333f,      // ~26.4 km/h
                lastSynced = null
            ),
            ActivityEntity(
                id = -4L,
                title = "Tempo Run",
                type = "Run",
                date = now.minus(7, ChronoUnit.DAYS),
                distance = 5_000f,
                movingTime = 1_380,         // 23 min
                totalElevationGain = 12f,
                averageSpeed = 3.623f,      // ~4:36 /km
                lastSynced = null
            ),
            ActivityEntity(
                id = -5L,
                title = "Recovery Walk",
                type = "Walk",
                date = now.minus(9, ChronoUnit.DAYS),
                distance = 4_100f,
                movingTime = 2_700,         // 45 min
                totalElevationGain = 18f,
                averageSpeed = 1.518f,
                lastSynced = null
            )
        )
        activityDao.insertAll(mockActivities)
    }

    /**
     * Refresh activities from Strava, but only if local cache is stale.
     * Also aggressively delete any data older than 7 days to comply with Strava terms.
     */
    override suspend fun refreshActivities() {
        // Enforce Strava API Agreement Section 59: Max 7-day retention for cached data
        val sevenDaysAgoEpoch = Instant.now().minusSeconds(MAX_RETENTION_SECONDS).epochSecond
        activityDao.clearStaleActivities(sevenDaysAgoEpoch)

        // Staleness check — read first emission from the cache AFTER clearing
        val cachedActivities = activityDao.getAllActivities().first()
        val mostRecentSync = cachedActivities
            .mapNotNull { it.lastSynced }
            .maxOrNull()

        if (mostRecentSync != null) {
            val ageSeconds = Instant.now().epochSecond - mostRecentSync.epochSecond
            if (ageSeconds < CACHE_TTL_SECONDS) {
                Timber.d("Cache is fresh (${ageSeconds}s old, TTL=${CACHE_TTL_SECONDS}s) — skipping Strava fetch")
                return
            }
        }


        val tokenResult = authRepository.getValidAccessToken()
        val accessToken = tokenResult.getOrNull() ?: return

        try {
            val stravaActivities = stravaApi.getActivities(accessToken)
            val now = Instant.now()

            // Backup to Supabase
            supabaseRepository.backupActivities(stravaActivities)

            val entities = stravaActivities.map { activity ->
                ActivityEntity(
                    id = activity.id,
                    title = activity.name,
                    type = activity.type,
                    date = java.time.Instant.parse(activity.startDate),
                    distance = activity.distance,
                    movingTime = activity.movingTime,
                    totalElevationGain = activity.totalElevationGain,
                    averageSpeed = activity.averageSpeed,
                    mapPolyline = activity.map?.summaryPolyline,
                    totalPhotos = activity.totalPhotoCount,
                    photoUrl = activity.photos?.primary?.urls?.get("600"),
                    lastSynced = now
                )
            }
            activityDao.insertAll(entities)

            // Auto-fetch missing photos for recent activities
            entities.filter { it.totalPhotos > 0 && it.photoUrl.isNullOrEmpty() }
                .forEach { incompleteActivity ->
                    try {
                        fetchActivityDetails(incompleteActivity.id)
                        kotlinx.coroutines.delay(250) // gentle throttle
                    } catch (e: StrataRateLimitException) {
                        // Rate limit hit during detail fetching — stop early, preserve what we have
                        Timber.w("Rate limit hit during photo fetch, stopping: ${e.message}")
                        return
                    } catch (_: Exception) {
                        // Continue to next activity on other failures
                    }
                }
        } catch (e: StrataRateLimitException) {
            Timber.w("Strava rate limit hit on activity list fetch: ${e.message}")
            throw e // Re-throw so ViewModel/UI can show a user message
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun fetchActivityDetails(id: Long) {
        val tokenResult = authRepository.getValidAccessToken()
        val accessToken = tokenResult.getOrNull() ?: return

        try {
            val activity = stravaApi.getActivity(accessToken, id)
            val photoUrl = activity.photos?.primary?.urls?.get("600")
            val entity = ActivityEntity(
                id = activity.id,
                title = activity.name,
                type = activity.type,
                date = java.time.Instant.parse(activity.startDate),
                distance = activity.distance,
                movingTime = activity.movingTime,
                totalElevationGain = activity.totalElevationGain,
                averageSpeed = activity.averageSpeed,
                mapPolyline = activity.map?.summaryPolyline,
                totalPhotos = activity.totalPhotoCount,
                photoUrl = photoUrl,
                lastSynced = Instant.now()
            )
            activityDao.insertAll(listOf(entity))
        } catch (e: StrataRateLimitException) {
            Timber.w("Rate limit hit fetching detail for activity $id")
            throw e
        } catch (_: Exception) {
            // Fail silently — detail fetch is best-effort
        }
    }

    companion object {
        /** Minimum age in seconds before we re-fetch from Strava. 15 minutes = 900s. */
        private const val CACHE_TTL_SECONDS = 900L
        /** Maximum age in seconds before cached data must be deleted (Strava API Agreement Sec 59). 7 days = 604,800s. */
        private const val MAX_RETENTION_SECONDS = 604800L
    }
}
