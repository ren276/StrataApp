package com.example.strata.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.strata.StrataApplication
import com.example.strata.data.repository.ActivityRepository
import com.example.strata.data.auth.AuthRepository
import com.example.strata.data.auth.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    
    // Explicitly using Eagerly sharing to ensure the value is ready when accessed by Splash
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isLoggedIn.value
        )

    val isGuestMode: StateFlow<Boolean> = authRepository.isGuestMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isGuestMode.value
        )

    val currentUserProfile: StateFlow<UserProfile?> = isLoggedIn.map { loggedIn ->
        if (loggedIn) authRepository.getUserProfile() else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = if (authRepository.isLoggedIn.value) authRepository.getUserProfile() else null
    )

    val darkMode: StateFlow<Boolean?> = authRepository.darkMode
    val useMetricUnits: StateFlow<Boolean> = authRepository.useMetricUnits

    fun setDarkMode(isDark: Boolean?) {
        authRepository.setDarkMode(isDark)
    }

    fun setUseMetricUnits(useMetric: Boolean) {
        authRepository.setUseMetricUnits(useMetric)
    }

    fun getLoginUrl(): String = authRepository.getStravaLoginUrl()

    fun enterGuestMode() {
        authRepository.enterGuestMode()
    }
    
    fun handleCallback(uri: Uri) {
        viewModelScope.launch {
            val result = authRepository.handleStravaCallback(uri)
            if (result.isSuccess) {
                try {
                    activityRepository.refreshActivities()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            activityRepository.clearActivitiesCache() // Delete offline data upon disconnect
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StrataApplication)
                AuthViewModel(
                    application.container.authRepository,
                    application.container.activityRepository
                )
            }
        }
    }
}
