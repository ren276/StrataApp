package com.example.strata.ui.feed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.strata.ui.auth.AuthViewModel
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun FeedScreen(
    onActivityClick: (Long) -> Unit,
    viewModel: FeedViewModel = viewModel(factory = FeedViewModel.Factory),
    authViewModel: AuthViewModel
) {
    val activities by viewModel.activities.collectAsState(initial = emptyList())
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val useMetric by authViewModel.useMetricUnits.collectAsState()
    val isGuestMode by authViewModel.isGuestMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // When in guest mode and no activities are loaded yet, seed the mock data
    LaunchedEffect(isGuestMode, activities.isEmpty()) {
        if (isGuestMode && activities.isEmpty()) {
            viewModel.seedGuestData()
        }
    }

    val filteredActivities = remember(activities, searchQuery) {
        if (searchQuery.isBlank()) {
            activities
        } else {
            activities.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Guest-mode banner pinned above the scroll area
            if (isGuestMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFA6000))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You're in guest mode — data is for preview only.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            item {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearchActive) {
                        androidx.compose.material3.OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            placeholder = { Text("Search activity...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            trailingIcon = {
                                androidx.compose.material3.IconButton(onClick = { 
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    } else {
                        val profileUrl = userProfile?.profileUrl
                        AsyncImage(
                            model = if (profileUrl == null || profileUrl == "null") {
                                "https://www.strava.com/assets/users/v2/avatars/athlete.png"
                            } else {
                                profileUrl
                            },
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape), // Primary border
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = getGreeting(),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            
                            val firstName = userProfile?.firstName?.takeUnless { it == "null" || it.isNullOrBlank() }
                            val lastName = userProfile?.lastName?.takeUnless { it == "null" || it.isNullOrBlank() }
                            
                            val name = listOfNotNull(firstName, lastName)
                                .joinToString(" ")
                                .ifBlank { "Strata Athlete" }
                            
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        // Search/Action button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                .clickable { isSearchActive = true },
                            contentAlignment = Alignment.Center
                        ) {
                             androidx.compose.material3.Icon(
                                 imageVector = Icons.Filled.Search,
                                 contentDescription = "Search",
                                 tint = MaterialTheme.colorScheme.primary,
                                 modifier = Modifier.size(20.dp)
                             )
                        }
                    }
                }
            }
            
            // Stories Section
            item {
                if (isGuestMode) {
                    // Demo stats for guest mode
                    val distanceLabel = if (useMetric) "KM" else "MI"
                    val elevationLabel = if (useMetric) "MTRS" else "FT"
                    val demoDistance = if (useMetric) "42.0" else "26.1"
                    val demoElevation = if (useMetric) "1200" else "3937"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StoryItem(label = "Weekly Goal", value = "84%", subLabel = "GOAL", color = Color(0xFFFA6000))
                        StoryItem(label = "Distance", value = demoDistance, subLabel = distanceLabel, color = Color(0xFF3B82F6))
                        StoryItem(label = "Calories", value = "2.7k", subLabel = "KCAL", color = Color(0xFFEF4444))
                        StoryItem(label = "Elevation", value = demoElevation, subLabel = elevationLabel, color = Color(0xFF10B981))
                    }
                } else {
                    val now = Instant.now()
                    val oneWeekAgo = now.minus(7, ChronoUnit.DAYS)
                    val weeklyActivities = activities.filter { it.date.isAfter(oneWeekAgo) }

                    val totalDistanceMeters = weeklyActivities.sumOf { it.distance.toDouble() }
                    val totalElevationMeters = weeklyActivities.sumOf { it.totalElevationGain.toDouble() }
                    val totalDistanceKm = totalDistanceMeters / 1000.0

                    val totalCalories = (totalDistanceKm * 65.0).toInt()
                    val weeklyGoalKm = 50.0
                    val weeklyGoalPercentage = ((totalDistanceKm / weeklyGoalKm) * 100).coerceAtMost(100.0).toInt()

                    val displayDistance: Double
                    val distanceLabel: String
                    val displayElevation: Double
                    val elevationLabel: String

                    if (useMetric) {
                        displayDistance = totalDistanceKm
                        distanceLabel = "KM"
                        displayElevation = totalElevationMeters
                        elevationLabel = "MTRS"
                    } else {
                        displayDistance = totalDistanceMeters / 1609.344
                        distanceLabel = "MI"
                        displayElevation = totalElevationMeters * 3.28084
                        elevationLabel = "FT"
                    }

                    val formattedDistance = String.format(java.util.Locale.US, "%.1f", displayDistance)
                    val formattedCalories = if (totalCalories >= 1000) {
                        String.format(java.util.Locale.US, "%.1fk", totalCalories / 1000.0)
                    } else {
                        totalCalories.toString()
                    }
                    val formattedElevation = displayElevation.toInt().toString()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StoryItem(label = "Weekly Goal", value = "${weeklyGoalPercentage}%", subLabel = "GOAL", color = Color(0xFFFA6000))
                        StoryItem(label = "Distance", value = formattedDistance, subLabel = distanceLabel, color = Color(0xFF3B82F6))
                        StoryItem(label = "Calories", value = formattedCalories, subLabel = "KCAL", color = Color(0xFFEF4444))
                        StoryItem(label = "Elevation", value = formattedElevation, subLabel = elevationLabel, color = Color(0xFF10B981))
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "RECENT FEED",
                    style = MaterialTheme.typography.labelSmall.copy(
                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                         letterSpacing = 1.5.sp,
                         fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Group activities by month
            val groupedActivities = filteredActivities.groupBy { activity ->
                val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.getDefault())
                    .withZone(java.time.ZoneId.systemDefault())
                formatter.format(activity.date)
            }

            if (filteredActivities.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                         Text(
                             if (searchQuery.isNotBlank()) "No activities match '${searchQuery}'" else "No activities found. Pull to refresh?", 
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                    }
                }
            } else {
                groupedActivities.forEach { (month, monthActivities) ->
                    item {
                        Text(
                            text = month.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                        )
                    }
                    items(monthActivities) { activity ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ActivityCard(
                                activity = activity,
                                onClick = { onActivityClick(activity.id) },
                                useMetric = useMetric
                            )
                        }
                    }
                }
            }
        } // close LazyColumn
        } // close Column
        
    } // close Scaffold
} // close FeedScreen

@Composable
fun StoryItem(label: String, value: String, subLabel: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(2.dp, color, CircleShape)
                .background(color.copy(alpha = 0.1f), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = color,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun getGreeting(): String {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}
