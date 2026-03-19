package com.example.strata

import android.app.Application
import android.util.Log
import com.example.strata.data.AppContainer
import com.example.strata.data.DefaultAppContainer

import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.strata.data.work.SyncWorker
import java.util.concurrent.TimeUnit

class StrataApplication : Application(), Configuration.Provider {
    
    lateinit var container: AppContainer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging and error handling
        if (BuildConfig.DEBUG) {
            timber.log.Timber.plant(timber.log.Timber.DebugTree())
        } else {
            // In release, we could plant a custom crash reporting tree
            // timber.log.Timber.plant(CrashReportingTree())
        }

        container = DefaultAppContainer(this)
        
        scheduleSync()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncActivities",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
