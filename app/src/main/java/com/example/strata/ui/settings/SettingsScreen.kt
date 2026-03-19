package com.example.strata.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.strata.R
import androidx.compose.ui.platform.LocalUriHandler
import com.example.strata.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onTermsClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val darkMode by authViewModel.darkMode.collectAsState()
    val useMetric by authViewModel.useMetricUnits.collectAsState()
    val isGuestMode by authViewModel.isGuestMode.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBack)
                            .background(Color(0xFFFA6000).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFA6000)
                        )
                    }
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFFFA6000),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable(onClick = onBack)
                            .padding(8.dp)
                    )
                }
            }

            // User Profile Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (isGuestMode) "https://www.strava.com/assets/users/v2/avatars/athlete.png"
                                    else userProfile?.profileUrl ?: "https://www.strava.com/assets/users/v2/avatars/athlete.png",
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = if (isGuestMode) "Guest Athlete"
                                       else "${userProfile?.firstName ?: ""} ${userProfile?.lastName ?: ""}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (isGuestMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isGuestMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isGuestMode) "GUEST MODE" else "PREMIUM MEMBER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isGuestMode) MaterialTheme.colorScheme.onSurfaceVariant
                                                else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Strava Integration
            item {
                SectionHeader("INTEGRATIONS")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFC5200), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_strava_powered_by_white),
                                        contentDescription = "Powered by Strava",
                                        modifier = Modifier.padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Strava Connection",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                    Text(
                                        if (isGuestMode) "Not connected"
                                        else "Connected as @${userProfile?.username ?: userProfile?.firstName?.lowercase() ?: "athlete"}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                            if (!isGuestMode) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = Color.Green)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isGuestMode) {
                            Button(
                                onClick = { uriHandler.openUri(authViewModel.getLoginUrl()) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC5200))
                            ) {
                                Text("Connect to Strava", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA6000))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Disconnect Strava", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Preferences Section
            item {
                SectionHeader("PREFERENCES")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    // Measurement Units
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Measurement Units", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium))
                        }
                        
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(if (useMetric) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { authViewModel.setUseMetricUnits(true) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Metric",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (useMetric) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(if (!useMetric) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { authViewModel.setUseMetricUnits(false) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Imperial",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (!useMetric) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Theme
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Theme",
                        value = when (darkMode) {
                            true -> "Dark Mode"
                            false -> "Light Mode"
                            else -> "System Mode"
                        },
                        onClick = {
                            val nextMode = when (darkMode) {
                                true -> false
                                false -> null
                                else -> true
                            }
                            authViewModel.setDarkMode(nextMode)
                        }
                    )
                }
            }

            // Data & Security
            item {
                SectionHeader("DATA & SECURITY")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                     SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Clear Local Cache",
                        value = "24.5 MB",
                        onClick = {}
                    )
                     SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Terms of Service",
                        iconRight = Icons.Default.ArrowForward,
                        onClick = onTermsClick
                    )
                }
            }

            // Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Strata v1.2.0 (Build 442)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!isGuestMode) {
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Log Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        ),
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp, start = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    iconRight: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium))
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        } else if (iconRight != null) {
            Icon(iconRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
