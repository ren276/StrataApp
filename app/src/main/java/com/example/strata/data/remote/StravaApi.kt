package com.example.strata.data.remote

import com.example.strata.BuildConfig
import com.example.strata.data.Secrets
import com.example.strata.data.model.StravaTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.HttpStatusCode
import com.example.strata.data.model.StravaActivity
import timber.log.Timber

/**
 * Strava API client.
 *
 * Rate limits: 100 req/15-min, 1000 req/day per application.
 * A 429 response means the limit is hit — we surface a typed exception
 * so the UI can show a user-friendly message instead of crashing.
 */
class StrataRateLimitException(message: String) : Exception(message)

class StravaApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            // BODY logs full request/response including auth tokens — never in release
            level = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE
        }
    }

    // ---------- helpers --------------------------------------------------

    /**
     * Checks for 429 (rate limit) and logs remaining quota from headers.
     * Throws [StrataRateLimitException] if rate-limited so callers can handle it.
     */
    private fun checkRateLimit(response: HttpResponse) {
        val usage = response.headers["X-RateLimit-Usage"]   // e.g. "42,753"
        val limit = response.headers["X-RateLimit-Limit"]   // e.g. "100,1000"
        if (usage != null && limit != null) {
            Timber.d("Strava rate limit — usage: $usage / limit: $limit")
        }
        if (response.status == HttpStatusCode.TooManyRequests) {
            val msg = "Strava rate limit reached ($usage / $limit). Try again later."
            Timber.w(msg)
            throw StrataRateLimitException(msg)
        }
    }

    // ---------- auth endpoints -------------------------------------------

    suspend fun exchangeToken(code: String): StravaTokenResponse {
        return client.post("${Secrets.SUPABASE_URL}/functions/v1/strava-auth") {
            headers {
                append("Authorization", "Bearer ${Secrets.SUPABASE_ANON_KEY}")
            }
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "code" to code,
                    "grant_type" to "authorization_code"
                )
            )
        }.body()
    }

    suspend fun refreshToken(refreshToken: String): StravaTokenResponse {
        return client.post("${Secrets.SUPABASE_URL}/functions/v1/strava-auth") {
            headers {
                append("Authorization", "Bearer ${Secrets.SUPABASE_ANON_KEY}")
            }
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "refresh_token" to refreshToken,
                    "grant_type" to "refresh_token"
                )
            )
        }.body()
    }

    suspend fun deauthorize(accessToken: String) {
        // Deauthorize can still hit Strava directly as it only requires the access_token, not the secret
        return client.post("https://www.strava.com/oauth/deauthorize") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("access_token" to accessToken))
        }.body() // Discards body but waits for completion
    }

    // ---------- data endpoints -------------------------------------------

    suspend fun getActivities(accessToken: String, page: Int = 1, perPage: Int = 30): List<StravaActivity> {
        val response = client.get("https://www.strava.com/api/v3/athlete/activities") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
            parameter("page", page)
            parameter("per_page", perPage)
        }
        checkRateLimit(response)
        return response.body()
    }

    suspend fun getAuthenticatedAthlete(accessToken: String): com.example.strata.data.model.StravaAthlete {
        val response = client.get("https://www.strava.com/api/v3/athlete") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }
        checkRateLimit(response)
        return response.body()
    }

    suspend fun getActivity(accessToken: String, id: Long): StravaActivity {
        val response = client.get("https://www.strava.com/api/v3/activities/$id") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }
        checkRateLimit(response)
        return response.body()
    }
}
