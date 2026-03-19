package com.example.strata.ui.editor

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.PixelCopy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.strata.data.repository.ActivityRepository
import com.example.strata.ui.components.MapRenderer
import com.example.strata.StrataApplication
import com.example.strata.data.model.ActivityTemplate

@Composable
fun EditorScreen(
    activityId: Long,
    templateId: String? = null,
    repository: ActivityRepository,
    navController: NavController,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onSave: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(
            activityId, 
            templateId, 
            repository,
            (context.applicationContext as StrataApplication).container.authRepository
        )
    )
    
    val activity by viewModel.activity.collectAsState()
    val elements by viewModel.elements.collectAsState()
    val selectedElementId by viewModel.selectedElementId.collectAsState()
    val currentBackground by viewModel.background.collectAsState() // Renamed to avoid shadowing
    val templateTitle by viewModel.templateTitle.collectAsState()
    val templateDate by viewModel.templateDate.collectAsState()
    val templateDistance by viewModel.templateDistance.collectAsState()
    val templateDuration by viewModel.templateDuration.collectAsState()
    val templateElevation by viewModel.templateElevation.collectAsState()
    val templatePrimaryColor by viewModel.templatePrimaryColor.collectAsState()
    val templateSecondaryColor by viewModel.templateSecondaryColor.collectAsState()
    
    // Media Pickers
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                // Navigate to ImageEditorScreen so user can rotate/zoom before applying
                val encoded = android.net.Uri.encode(it.toString())
                navController.navigate("image_editor/$encoded")
            }
        }
    )
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.setBackgroundVideo(it.toString()) } }
    )
    
    val density = LocalDensity.current

    val activeTemplate = remember(templateId) {
        ActivityTemplate.allTemplates.find { it.id == templateId }
    }

    // Tracks which data fields the Canvas template renderer should draw (all on by default)
    var templateVisibleFields by remember {
        mutableStateOf(setOf("title", "date", "distance", "duration", "elevation"))
    }

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()
        
        // 1. Background Layer
        when (val bg = currentBackground) {
            is EditorViewModel.BackgroundType.Image -> {
                AsyncImage(
                    model = bg.uri,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            is EditorViewModel.BackgroundType.Video -> {
                VideoPlayer(uri = android.net.Uri.parse(bg.uri))
            }
            is EditorViewModel.BackgroundType.Map -> {
                MapRenderer(polyline = bg.polyline, lineColor = MaterialTheme.colorScheme.primary)
            }
            is EditorViewModel.BackgroundType.Gradient -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(bg.colors.map { Color(it) }))
                )
            }
            is EditorViewModel.BackgroundType.EditedBitmap -> {
                androidx.compose.foundation.Image(
                    bitmap = bg.bitmap.asImageBitmap(),
                    contentDescription = "Edited background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // 2. Editor Elements / Template Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { viewModel.selectElement(null) }
        ) {
            if (activeTemplate != null && activity != null) {
                // Template Mode: full Canvas template with per-field visibility control.
                // No freeform elements are spawned in this mode (ViewModel sets elements = empty),
                // so the Canvas is the single source of data rendering — no duplication.
                ActivityTemplateRenderer(
                    activity = activity!!,
                    template = activeTemplate,
                    modifier = Modifier.fillMaxSize(),
                    overrideTitle = templateTitle,
                    overrideDate = templateDate,
                    overrideDistance = templateDistance,
                    overrideDuration = templateDuration,
                    overrideElevation = templateElevation,
                    overridePrimaryColor = templatePrimaryColor,
                    overrideSecondaryColor = templateSecondaryColor,
                    visibleFields = templateVisibleFields
                )
            }
            
            // Always render Freeform Editor Elements for interactivity
            elements.forEach { element ->
                key(element.id) {
                    val isSelected = selectedElementId == element.id
                    val currentElement by rememberUpdatedState(element)
                    
                    // Element Container with Drag & Selection
                    Box(
                        modifier = Modifier
                            .align(androidx.compose.ui.BiasAlignment(
                                horizontalBias = (element.x * 2) - 1,
                                verticalBias = (element.y * 2) - 1
                            ))
                        .graphicsLayer {
                            rotationX = element.rotationX
                            rotationY = element.rotationY
                            rotationZ = element.rotationZ
                            scaleX = element.scale
                            scaleY = element.scale
                            cameraDistance = 12f * density.density
                        }
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent, 
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp, 
                            if (isSelected) Color.White else Color.Transparent, 
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                        .clickable { viewModel.selectElement(element.id) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { viewModel.selectElement(element.id) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newX = (currentElement.x + dragAmount.x / maxWidthPx).coerceIn(0f, 1f)
                                    val newY = (currentElement.y + dragAmount.y / maxHeightPx).coerceIn(0f, 1f)
                                    viewModel.updateElementPosition(currentElement.id, newX, newY)
                                }
                            )
                        }
                    ) {
                        when (element) {
                            is EditorViewModel.EditorElement.Text -> {
                                val fontFamily = when (element.font) {
                                    EditorViewModel.FontType.DEFAULT -> androidx.compose.ui.text.font.FontFamily.Default
                                    EditorViewModel.FontType.SERIF -> androidx.compose.ui.text.font.FontFamily.Serif
                                    EditorViewModel.FontType.MONOSPACE -> androidx.compose.ui.text.font.FontFamily.Monospace
                                    EditorViewModel.FontType.CURSIVE -> androidx.compose.ui.text.font.FontFamily.Cursive
                                    EditorViewModel.FontType.BOLD -> androidx.compose.ui.text.font.FontFamily.Default 
                                    EditorViewModel.FontType.BEBAS_NEUE -> androidx.compose.ui.text.font.FontFamily.SansSerif 
                                    EditorViewModel.FontType.PLAYFAIR -> androidx.compose.ui.text.font.FontFamily.Serif 
                                }
                                
                                Text(
                                    text = element.text,
                                    style = androidx.compose.ui.text.TextStyle(
                                        color = Color(element.color),
                                        fontSize = element.fontSize.sp,
                                        fontFamily = fontFamily,
                                        fontWeight = when (element.font) {
                                            EditorViewModel.FontType.BOLD -> FontWeight.ExtraBold
                                            EditorViewModel.FontType.BEBAS_NEUE -> FontWeight.Black
                                            EditorViewModel.FontType.PLAYFAIR -> FontWeight.Bold
                                            else -> FontWeight.Normal
                                        },
                                        fontStyle = if (element.font == EditorViewModel.FontType.CURSIVE)
                                            androidx.compose.ui.text.font.FontStyle.Italic
                                        else androidx.compose.ui.text.font.FontStyle.Normal,
                                        textAlign = when (element.textAlign) {
                                            EditorViewModel.TextAlign.LEFT -> androidx.compose.ui.text.style.TextAlign.Left
                                            EditorViewModel.TextAlign.CENTER -> androidx.compose.ui.text.style.TextAlign.Center
                                            EditorViewModel.TextAlign.RIGHT -> androidx.compose.ui.text.style.TextAlign.Right
                                        }
                                    )
                                )
                            }
                            is EditorViewModel.EditorElement.Data -> {
                                 val fontFamily = when (element.font) {
                                    EditorViewModel.FontType.DEFAULT -> androidx.compose.ui.text.font.FontFamily.Default
                                    EditorViewModel.FontType.SERIF -> androidx.compose.ui.text.font.FontFamily.Serif
                                    EditorViewModel.FontType.MONOSPACE -> androidx.compose.ui.text.font.FontFamily.Monospace
                                    EditorViewModel.FontType.CURSIVE -> androidx.compose.ui.text.font.FontFamily.Cursive
                                    EditorViewModel.FontType.BOLD -> androidx.compose.ui.text.font.FontFamily.Default
                                    EditorViewModel.FontType.BEBAS_NEUE -> androidx.compose.ui.text.font.FontFamily.SansSerif
                                    EditorViewModel.FontType.PLAYFAIR -> androidx.compose.ui.text.font.FontFamily.Serif
                                }
                            
                                 Column(horizontalAlignment = when (element.textAlign) {
                                    EditorViewModel.TextAlign.LEFT -> Alignment.Start
                                    EditorViewModel.TextAlign.CENTER -> Alignment.CenterHorizontally
                                    EditorViewModel.TextAlign.RIGHT -> Alignment.End
                                }) {
                                    if (element.label.isNotEmpty()) {
                                        Text(
                                            text = element.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(element.color).copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = fontFamily
                                            )
                                        )
                                    }
                                    Text(
                                        text = element.value,
                                        style = androidx.compose.ui.text.TextStyle(
                                            color = Color(element.color),
                                            fontSize = 32.sp,
                                            fontFamily = fontFamily,
                                            fontWeight = when (element.font) {
                                                EditorViewModel.FontType.BOLD -> FontWeight.ExtraBold
                                                EditorViewModel.FontType.BEBAS_NEUE -> FontWeight.Black
                                                EditorViewModel.FontType.PLAYFAIR -> FontWeight.Bold
                                                else -> FontWeight.Black
                                            },
                                            fontStyle = if (element.font == EditorViewModel.FontType.CURSIVE)
                                                androidx.compose.ui.text.font.FontStyle.Italic
                                            else androidx.compose.ui.text.font.FontStyle.Normal
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Delete Button for Selected
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 12.dp, y = (-12).dp)
                                    .size(24.dp) // Increased touch target
                                    .background(Color.Red, CircleShape)
                                    .clickable { viewModel.removeElement(element.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
        // 3. Top Nav Bar (Save/Close)
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Total Liberty Editor",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = "View on Strava",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFFC5200),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://www.strava.com/activities/$activityId")
                    }
                )
            }

            val currentContext = LocalContext.current
            val currentView = LocalView.current
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Save Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFA6000))
                        .clickable {
                            val activity = currentContext as? Activity
                            val window = activity?.window
                            if (window != null) {
                                val bitmap = Bitmap.createBitmap(currentView.width, currentView.height, Bitmap.Config.ARGB_8888)
                                val location = IntArray(2)
                                currentView.getLocationInWindow(location)
                                val rect = Rect(location[0], location[1], location[0] + currentView.width, location[1] + currentView.height)
                                
                                PixelCopy.request(window, rect, bitmap, { copyResult ->
                                    if (copyResult == PixelCopy.SUCCESS) {
                                        saveBitmapToMediaStore(currentContext, bitmap)
                                        // Show acknowledgement toast — stay in editor
                                        activity.runOnUiThread {
                                            android.widget.Toast.makeText(
                                                currentContext,
                                                "✓ Saved to Gallery",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        activity.runOnUiThread {
                                            android.widget.Toast.makeText(
                                                currentContext,
                                                "Save failed — try again",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }, android.os.Handler(android.os.Looper.getMainLooper()))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Save", tint = Color.White)
                }

                // Share Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .clickable {
                            val activity = currentContext as? Activity
                            val window = activity?.window
                            if (window != null) {
                                val bitmap = Bitmap.createBitmap(currentView.width, currentView.height, Bitmap.Config.ARGB_8888)
                                val location = IntArray(2)
                                currentView.getLocationInWindow(location)
                                val rect = Rect(location[0], location[1], location[0] + currentView.width, location[1] + currentView.height)
                                
                                 PixelCopy.request(window, rect, bitmap, { copyResult ->
                                     if (copyResult == PixelCopy.SUCCESS) {
                                         shareBitmap(currentContext, bitmap)
                                     } else {
                                         activity.runOnUiThread {
                                             android.widget.Toast.makeText(
                                                 currentContext,
                                                 "Share failed — capture error",
                                                 android.widget.Toast.LENGTH_SHORT
                                             ).show()
                                         }
                                     }
                                 }, android.os.Handler(android.os.Looper.getMainLooper()))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // 4. Control Panel
        val selectedElement = elements.find { it.id == selectedElementId }
        
        var isPanelExpanded by remember { mutableStateOf(true) }
        val panelHeight by animateDpAsState(
            targetValue = if (isPanelExpanded) 400.dp else 50.dp,
            label = "PanelHeight"
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(panelHeight)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
             // Drag Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clickable { isPanelExpanded = !isPanelExpanded },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedElement != null) {
                    // --- Freeform Selected Element Controls ---
                    Text(
                        "EDIT SELECTED",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 3D Controls
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RotationSlider("Rotation X", selectedElement.rotationX) { newValue -> viewModel.updateElementRotation(selectedElement.id, newValue, selectedElement.rotationY, selectedElement.rotationZ) }
                        RotationSlider("Rotation Y", selectedElement.rotationY) { newValue -> viewModel.updateElementRotation(selectedElement.id, selectedElement.rotationX, newValue, selectedElement.rotationZ) }
                        RotationSlider("Rotation Z", selectedElement.rotationZ) { newValue -> viewModel.updateElementRotation(selectedElement.id, selectedElement.rotationX, selectedElement.rotationY, newValue) }
                        RotationSlider("Scale", selectedElement.scale * 10f) { newValue -> viewModel.updateElementScale(selectedElement.id, newValue / 10f) } 
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                                   // Color Picker (Available for ALL elements)
                     Spacer(modifier = Modifier.height(16.dp))
                     Text("TEXT COLOR", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                     Spacer(modifier = Modifier.height(8.dp))
                     val currentColor = when(selectedElement) {
                         is EditorViewModel.EditorElement.Text -> selectedElement.color
                         is EditorViewModel.EditorElement.Data -> selectedElement.color
                     }

                     com.example.strata.ui.components.ColorPickerCircle(
                         modifier = Modifier.size(80.dp),
                         initialColor = Color(currentColor),
                         onColorSelected = { color -> viewModel.updateTextColor(selectedElement.id, color.toArgb().toLong() and 0xFFFFFFFF) }
                     )
                     
                     // Font Picker (Available for ALL elements)
                     Spacer(modifier = Modifier.height(16.dp))
                     Text("FONT", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                     Spacer(modifier = Modifier.height(8.dp))

                     val currentFont = when(selectedElement) {
                         is EditorViewModel.EditorElement.Text -> selectedElement.font
                         is EditorViewModel.EditorElement.Data -> selectedElement.font
                     }

                     // Map each FontType to a display label and its FontFamily+weight+style
                     val fontOptions = listOf(
                         EditorViewModel.FontType.DEFAULT  to Triple("Default",  androidx.compose.ui.text.font.FontFamily.Default,    FontWeight.Normal),
                         EditorViewModel.FontType.SERIF    to Triple("Serif",    androidx.compose.ui.text.font.FontFamily.Serif,      FontWeight.Normal),
                         EditorViewModel.FontType.BOLD     to Triple("Bold",     androidx.compose.ui.text.font.FontFamily.Default,    FontWeight.ExtraBold),
                         EditorViewModel.FontType.MONOSPACE to Triple("Mono",   androidx.compose.ui.text.font.FontFamily.Monospace,  FontWeight.Normal),
                         EditorViewModel.FontType.CURSIVE  to Triple("Italic",  androidx.compose.ui.text.font.FontFamily.Cursive,    FontWeight.Normal),
                         EditorViewModel.FontType.BEBAS_NEUE to Triple("Heavy", androidx.compose.ui.text.font.FontFamily.SansSerif,  FontWeight.Black),
                         EditorViewModel.FontType.PLAYFAIR to Triple("Classic", androidx.compose.ui.text.font.FontFamily.Serif,      FontWeight.Bold),
                     )

                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .horizontalScroll(rememberScrollState()),
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         fontOptions.forEach { (fontType, triple) ->
                             val (label, family, weight) = triple
                             val isSelected = currentFont == fontType
                             val fontStyle = if (fontType == EditorViewModel.FontType.CURSIVE)
                                 androidx.compose.ui.text.font.FontStyle.Italic
                             else androidx.compose.ui.text.font.FontStyle.Normal

                             Box(
                                 modifier = Modifier
                                     .background(
                                         if (isSelected) MaterialTheme.colorScheme.primary
                                         else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                         RoundedCornerShape(8.dp)
                                     )
                                     .border(
                                         1.dp,
                                         if (isSelected) MaterialTheme.colorScheme.primary
                                         else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                         RoundedCornerShape(8.dp)
                                     )
                                     .clickable { viewModel.updateTextFont(selectedElement.id, fontType) }
                                     .padding(horizontal = 14.dp, vertical = 8.dp)
                             ) {
                                 Text(
                                     text = label,
                                     style = MaterialTheme.typography.labelSmall.copy(
                                         color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                 else MaterialTheme.colorScheme.onSurface,
                                         fontFamily = family,
                                         fontWeight = weight,
                                         fontStyle = fontStyle,
                                         fontSize = 11.sp
                                     )
                                 )
                             }
                         }
                     }

                     // Alignment Controls
                     Spacer(modifier = Modifier.height(16.dp))
                     Text("ALIGNMENT", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                     Spacer(modifier = Modifier.height(8.dp))
                     Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                     @Suppress("DEPRECATION")
                     val alignments = listOf(
                         EditorViewModel.TextAlign.LEFT to Icons.Default.FormatAlignLeft,
                         EditorViewModel.TextAlign.CENTER to Icons.Default.FormatAlignCenter,
                         EditorViewModel.TextAlign.RIGHT to Icons.Default.FormatAlignRight
                     )
                         val currentAlign = when(selectedElement) {
                             is EditorViewModel.EditorElement.Text -> selectedElement.textAlign
                             is EditorViewModel.EditorElement.Data -> selectedElement.textAlign
                         }
                         
                         alignments.forEach { (align, icon) ->
                             Box(
                                 modifier = Modifier
                                     .size(32.dp)
                                     .background(if (currentAlign == align) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                     .clickable { viewModel.updateElementAlignment(selectedElement.id, align) },
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(icon, contentDescription = align.name, tint = if (currentAlign == align) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                             }
                         }
                     }
                     
                     // Text Specific Controls (Content Editing)
                     if (selectedElement is EditorViewModel.EditorElement.Text) {
                         Spacer(modifier = Modifier.height(16.dp))
                         
                         // Edit Text Content
                         OutlinedTextField(
                             value = selectedElement.text,
                             onValueChange = { viewModel.updateElementText(selectedElement.id, it) },
                             label = { Text("Text Content", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                             colors = TextFieldDefaults.colors(
                                 focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                 unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                 focusedContainerColor = Color.Transparent,
                                 unfocusedContainerColor = Color.Transparent,
                                 focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                 unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                 focusedLabelColor = MaterialTheme.colorScheme.primary,
                                 unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                             ),
                             modifier = Modifier.fillMaxWidth()
                         )
                     }
                     
                } else if (activeTemplate != null) {
                    // --- Template Mode Panel ---
                    // Data fields are drawn by ActivityTemplateRenderer; toggles control
                    // the visibleFields set passed to the Canvas renderer.

                    Text(
                        "DATA FIELDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val templateFieldDefs = listOf(
                        "title"     to "Show Title",
                        "date"      to "Show Date",
                        "distance"  to "Show Distance",
                        "duration"  to "Show Duration",
                        "elevation" to "Show Elevation"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        templateFieldDefs.forEach { (key, label) ->
                            val isOn = key in templateVisibleFields
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        templateVisibleFields = if (isOn)
                                            templateVisibleFields - key
                                        else
                                            templateVisibleFields + key
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface))
                                Switch(
                                    checked = isOn,
                                    onCheckedChange = { checked ->
                                        templateVisibleFields = if (checked)
                                            templateVisibleFields + key
                                        else
                                            templateVisibleFields - key
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "BACKGROUND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = "Image", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Photo", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                            videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        }) {
                            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Video", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                        }
                        if (activity?.mapPolyline != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                viewModel.setBackgroundMap(activity?.mapPolyline!!)
                            }) {
                                Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Map, contentDescription = "Map", tint = MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Map", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                            }
                        }
                    }
                } else {
                    // --- Global Controls (No Selection) ---
                    
                    // Stats Visibility
                    Text(
                        "STATS DISPLAY",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val dataTypes = listOf(
                        EditorViewModel.DataType.TITLE to "Show Title",
                        EditorViewModel.DataType.DATE to "Show Date",
                        EditorViewModel.DataType.DISTANCE to "Show Distance",
                        EditorViewModel.DataType.TIME to "Show Time",
                        EditorViewModel.DataType.ELEVATION to "Show Elevation",
                        EditorViewModel.DataType.CALORIES to "Show Calories"
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dataTypes.forEach { (type, label) ->
                            val isPresent = elements.any { it is EditorViewModel.EditorElement.Data && it.type == type }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isPresent) {
                                            // Find and remove
                                            val element = elements.find { it is EditorViewModel.EditorElement.Data && it.type == type }
                                            element?.let { viewModel.removeElement(it.id) }
                                        } else {
                                            // Add
                                            viewModel.addData(type)
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface))
                                Switch(
                                    checked = isPresent,
                                    onCheckedChange = { checked -> 
                                        if (checked) viewModel.addData(type) else {
                                             val element = elements.find { el -> el is EditorViewModel.EditorElement.Data && el.type == type }
                                             element?.let { id -> viewModel.removeElement(id.id) }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Background Picker
                    Text(
                        "BACKGROUND",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                         // Image Picker 
                         Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                             imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                         }) {
                             Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                 Icon(Icons.Default.Image, contentDescription = "Image", tint = MaterialTheme.colorScheme.onSurface)
                             }
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("Photo", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                         }
                         
                          // Video Picker 
                         Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                             videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                         }) {
                             Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                 Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = MaterialTheme.colorScheme.onSurface)
                             }
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("Video", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                         }
                         
                         // Map Button
                          if (activity?.mapPolyline != null) {
                             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                                 viewModel.setBackgroundMap(activity?.mapPolyline!!)
                             }) {
                                 Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                     Icon(Icons.Default.Map, contentDescription = "Map", tint = MaterialTheme.colorScheme.onSurface)
                                 }
                                 Spacer(modifier = Modifier.height(4.dp))
                                 Text("Map", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                             }
                         }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Add Text Button
                    Button(
                        onClick = { viewModel.addText() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TextFields, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Text", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                
                Spacer(modifier = Modifier.height(50.dp)) // Bottom padding
            }
        }
    }
}

@Composable
fun RotationSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(
            text = "$label: ${value.toInt()}°",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}

private fun saveBitmapToMediaStore(context: android.content.Context, bitmap: Bitmap) {
    val filename = "STRATA_${System.currentTimeMillis()}.jpg"
    var fos: java.io.OutputStream? = null
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
    }

    try {
        val contentResolver = context.contentResolver
        val imageUri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let {
            fos = contentResolver.openOutputStream(it)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos!!)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        fos?.close()
    }
}

private fun shareBitmap(context: android.content.Context, bitmap: Bitmap) {
    try {
        val filename = "share_${System.currentTimeMillis()}.png"
        val cachePath = java.io.File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = java.io.FileOutputStream("$cachePath/$filename")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val imagePath = java.io.File(context.cacheDir, "images")
        val newFile = java.io.File(imagePath, filename)
        val authority = "${context.packageName}.fileprovider"
        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, newFile)

        if (contentUri != null) {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            val chooser = android.content.Intent.createChooser(shareIntent, "Share via")
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        (context as? Activity)?.runOnUiThread {
             android.widget.Toast.makeText(context, "Share error: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(uri: android.net.Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }

    DisposableEffect(uri) {
        val mediaItem = androidx.media3.common.MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        onDispose {
            exoPlayer.release()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = {
            androidx.media3.ui.PlayerView(context).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
