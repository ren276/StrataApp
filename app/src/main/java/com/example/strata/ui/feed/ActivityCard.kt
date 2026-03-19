package com.example.strata.ui.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.strata.ui.components.MapRenderer
import com.example.strata.data.local.ActivityEntity
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.ui.unit.sp

@Composable
fun ActivityCard(
    activity: ActivityEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useMetric: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- Background Layer ---
            val photoUrl = activity.photoUrl
            val polyline = activity.mapPolyline

            // Track whether we have a dark scrim over the background
            val hasScrimBackground: Boolean

            if (!photoUrl.isNullOrEmpty()) {
                hasScrimBackground = true
                // 1. Photo Background
                coil.compose.AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                // Scrim for text readability
                Box(modifier = Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                ))
            } else if (!polyline.isNullOrEmpty()) {
                hasScrimBackground = true
                // 2. Map Background
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                    MapRenderer(
                        polyline = polyline,
                        modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                        lineColor = MaterialTheme.colorScheme.primary,
                        lineWidth = 6f
                    )
                }
                // Scrim
                Box(modifier = Modifier.fillMaxSize().background(
                     androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                ))
            } else {
                hasScrimBackground = false
                // 3. Gradient Fallback – uses theme surface so it works in light & dark
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background)
                            )
                        )
                )
            }

            // Choose text color: white on dark scrim, theme-aware otherwise
            val contentColor = if (hasScrimBackground) Color.White else MaterialTheme.colorScheme.onSurface

            // --- Content Layer ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                 // Date Badge
                Text(
                    text = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                        .withZone(ZoneId.systemDefault())
                        .format(activity.date).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    ),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Format distance based on unit preference
                val distanceText = if (useMetric) {
                    String.format("%.2f km", activity.distance / 1000)
                } else {
                    String.format("%.2f mi", activity.distance / 1609.344)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatItem(label = "DISTANCE", value = distanceText, color = contentColor)
                    Spacer(modifier = Modifier.width(24.dp))
                    StatItem(label = "TIME", value = formatDuration(activity.movingTime), color = contentColor)
                }
            }
            
            // Type Badge at top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                 Text(
                    text = activity.type,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }

            // View on Strava link (Strava Brand Guidelines §3)
            val uriHandler = LocalUriHandler.current
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { uriHandler.openUri("https://www.strava.com/activities/${activity.id}") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "View on Strava",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFFC5200),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = color))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = color.copy(alpha = 0.7f), fontSize = 10.sp))
    }
}

fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
