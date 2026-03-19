package com.example.strata.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivity(id: Long): Flow<ActivityEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>)
    
    @Query("DELETE FROM activities")
    suspend fun clearAll()

    @Query("DELETE FROM activities WHERE lastSynced < :thresholdEpochSeconds")
    suspend fun clearStaleActivities(thresholdEpochSeconds: Long)
}
