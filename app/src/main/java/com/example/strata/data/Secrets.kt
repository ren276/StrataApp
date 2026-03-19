package com.example.strata.data

import com.example.strata.BuildConfig

/**
 * All secrets are read from BuildConfig, which is populated at compile time
 * from local.properties. No hardcoded values exist in this file.
 * See app/build.gradle.kts for the injection logic.
 */
object Secrets {
    val SUPABASE_URL        get() = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY   get() = BuildConfig.SUPABASE_ANON_KEY
    val STRAVA_CLIENT_ID    get() = BuildConfig.STRAVA_CLIENT_ID
    val STRAVA_REDIRECT_URI get() = BuildConfig.STRAVA_REDIRECT_URI
}

