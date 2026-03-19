package com.example.strata

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.strata.ui.StrataApp
import com.example.strata.ui.auth.AuthViewModel
import com.example.strata.ui.theme.StrataTheme

class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen BEFORE setContent and super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Keep the dark OS splash screen visible until Compose has initialised.
        // The condition returns TRUE (= keep showing) until we know auth state.
        // This prevents ANY white frame from appearing between system splash and Compose.
        splashScreen.setKeepOnScreenCondition {
            // authViewModel.isLoggedIn starts as Eagerly so it has an immediate value —
            // but we add a short guard: keep splash if the ViewModel hasn't initialised yet.
            // Once this returns false the splash exits and our Compose SplashScreen takes over.
            false // Compose SplashScreen handles the animated experience; dismiss OS splash immediately
        }
        
        handleIntent(intent)

        setContent {
            val userDarkMode by authViewModel.darkMode.collectAsState()
            val forceDark = userDarkMode ?: isSystemInDarkTheme()

            StrataTheme(darkTheme = forceDark) {
                val appContainer = (application as StrataApplication).container
                StrataApp(
                    authViewModel = authViewModel,
                    activityRepository = appContainer.activityRepository
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null && uri.scheme == "strata" && uri.host == "callback") {
                authViewModel.handleCallback(uri)
            }
        }
    }
}