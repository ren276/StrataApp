package com.example.strata.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.strata.data.local.ActivityEntity
import com.example.strata.data.model.ActivityTemplate
import com.example.strata.ui.editor.ActivityTemplateRenderer
import com.example.strata.ui.feed.FeedViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onTemplateSelected: (String, Long) -> Unit,
    feedViewModel: FeedViewModel = viewModel(factory = FeedViewModel.Factory)
) {
    val templates = remember { ActivityTemplate.allTemplates }
    val groupedTemplates = remember { templates.groupBy { it.category } }
    
    var selectedTemplate by remember { mutableStateOf<ActivityTemplate?>(null) }
    var showActivitySheet by remember { mutableStateOf(false) }
    
    val activities by feedViewModel.activities.collectAsState(initial = emptyList())

    if (showActivitySheet && selectedTemplate != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showActivitySheet = false 
                selectedTemplate = null
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select an Activity",
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    items(activities) { activity ->
                        ActivityPickerItem(activity = activity) {
                            onTemplateSelected(selectedTemplate!!.id, activity.id)
                            showActivitySheet = false
                            selectedTemplate = null
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection()
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                groupedTemplates.forEach { (category, categoryTemplates) ->
                    item(key = category) {
                        SectionHeader(title = category)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.heightIn(max = 800.dp) // Constrained nested grid
                        ) {
                            items(categoryTemplates) { template ->
                                TemplatePreviewCard(template = template) {
                                    selectedTemplate = template
                                    showActivitySheet = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clickable { /* Handle Back */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = "Strata Templates",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun TemplatePreviewCard(template: ActivityTemplate, onClick: () -> Unit) {
    val mockActivity = remember {
        ActivityEntity(
            id = 0L,
            title = "Morning Run",
            type = "Run",
            date = Instant.now(),
            distance = 5000f,
            movingTime = 1800,
            totalElevationGain = 120f,
            averageSpeed = 2.5f
        )
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E)) // Dark bg to pop colors
            .clickable(onClick = onClick)
    ) {
        // Native Canvas Renderer Preview!
        ActivityTemplateRenderer(
            activity = mockActivity,
            template = template,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        )
        
        // Dark gradient overlay at bottom for legible title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
        )
        
        Text(
            text = template.title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )
    }
}

@Composable
fun ActivityPickerItem(activity: ActivityEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
             Text(
                text = DateTimeFormatter.ofPattern("dd", Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
                    .format(activity.date),
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = String.format(Locale.US, "%.2f km", activity.distance / 1000f),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
