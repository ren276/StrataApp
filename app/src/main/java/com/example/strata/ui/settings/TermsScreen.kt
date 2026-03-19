package com.example.strata.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TermsScreen(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Scrollable text content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Last Updated: October 2024\n\n" +
                            "Welcome to Strata, a premium fitness tracking platform designed to analyze and enhance your athletic performance.\n\n" +
                            "1. Acceptance of Terms\n" +
                            "By accessing and using this application, you accept and agree to be bound by the terms and provisions of this agreement. If you do not agree to abide by these terms, please do not use this service.\n\n" +
                            "2. User Accounts and Privacy\n" +
                            "To utilize certain features, you must authenticate through supported third-party providers (e.g., Strava). We access your training data only with your explicit consent and do not sell your personal data. We securely store your profile information using encrypted mechanisms and cloud infrastructure (Supabase) to persist your identity securely.\n\n" +
                            "3. Service usage\n" +
                            "Strata is provided on an 'as is' basis. While we strive to ensure 100% uptime and accuracy of data processing, we disclaim all warranties regarding the completeness or accuracy of any metrics shown.\n\n" +
                            "4. Premium Features\n" +
                            "If you possess a Premium Membership status, your account unlocks advanced analytics, offline cache support, and custom templates. This subscription may be subject to additional billing terms as listed during the purchase flow.\n\n" +
                            "5. Changes to the Terms\n" +
                            "We reserve the right to modify these terms from time to time at our sole discretion. Therefore, you should review these pages periodically. Your continued use of the Website or our service after any such change constitutes your acceptance of the new Terms.\n\n" +
                            "6. Contact Us\n" +
                            "If you encounter any issues or require support regarding your data or these terms, please reach out via our official support channels.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
