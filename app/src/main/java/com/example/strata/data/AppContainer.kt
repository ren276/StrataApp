package com.example.strata.data

import android.content.Context
import com.example.strata.data.auth.AuthRepository
import com.example.strata.data.auth.AuthRepositoryImpl
import com.example.strata.data.local.AppDatabase
import com.example.strata.data.local.TokenManager
import com.example.strata.data.remote.StravaApi
import com.example.strata.data.repository.ActivityRepository
import com.example.strata.data.repository.ActivityRepositoryImpl
import com.example.strata.data.repository.SupabaseRepository

interface AppContainer {
    val authRepository: AuthRepository
    val activityRepository: ActivityRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    private val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }
    
    // Remote
    private val stravaApi: StravaApi by lazy {
        StravaApi()
    }
    
    // Repositories
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(tokenManager, stravaApi, supabaseRepository)
    }

    // Database
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    
    private val supabaseRepository: SupabaseRepository by lazy {
        SupabaseRepository()
    }
    
    override val activityRepository: ActivityRepository by lazy {
        ActivityRepositoryImpl(database.activityDao(), stravaApi, authRepository, supabaseRepository)
    }
}
