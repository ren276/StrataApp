package com.example.strata.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.strata.StrataApplication
import com.example.strata.data.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: ActivityRepository) : ViewModel() {
    
    val activities = repository.getActivities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            try {
                repository.refreshActivities()
            } catch (e: Exception) {
                // Silently ignore sync errors; state remains from local DB
            }
        }
    }

    /** Seed realistic demo activities for guest / unauthenticated mode. */
    fun seedGuestData() {
        viewModelScope.launch {
            try {
                repository.seedMockActivities()
            } catch (_: Exception) {
                // Best-effort; UI will show empty state if it fails
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StrataApplication)
                FeedViewModel(application.container.activityRepository)
            }
        }
    }
}
