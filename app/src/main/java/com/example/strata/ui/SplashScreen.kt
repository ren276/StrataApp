package com.example.strata.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strata.R
import com.example.strata.ui.theme.DarkBackground
import com.example.strata.ui.theme.StrataOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Wait a tiny bit before starting the animation so the screen fully draws first
        delay(300)
        
        // Animate from 0 to 1 over 2.5 seconds linearly
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
        
        // Wait another tiny bit after 100% is reached
        delay(200)
        onComplete()
    }

    // Force dark background regardless of system theme for Splash Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top Right Info Icon
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = Color(0xFFAAAAAA),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(24.dp)
        )

        // Center Logo (Image only as requested by user)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(140.dp)
            )
        }

        // Bottom Loading Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text Header above progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "OPTIMIZING PERFORMANCE",
                    color = StrataOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${(progressAnim.value * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = StrataOrange,
                trackColor = Color(0xFF332015), // Dark brown/orange trace
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Powered By Logo
            Image(
                painter = painterResource(id = R.drawable.ic_strava_powered_by_white),
                contentDescription = "Powered by Strava",
                modifier = Modifier.height(32.dp).padding(vertical = 4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Three orange dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(4.dp).background(StrataOrange, androidx.compose.foundation.shape.CircleShape))
                Box(modifier = Modifier.size(4.dp).background(StrataOrange, androidx.compose.foundation.shape.CircleShape))
                Box(modifier = Modifier.size(4.dp).background(StrataOrange, androidx.compose.foundation.shape.CircleShape))
            }
        }
    }
}
