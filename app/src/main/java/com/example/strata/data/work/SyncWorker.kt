package com.example.strata.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.strata.StrataApplication
import com.example.strata.data.local.ActivityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appContainer = (applicationContext as StrataApplication).container
        
        try {
            val tokenResult = appContainer.authRepository.getValidAccessToken()
            val accessToken = tokenResult.getOrNull() ?: return@withContext Result.failure()
            
            val activities = appContainer.activityRepository.refreshActivities()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
