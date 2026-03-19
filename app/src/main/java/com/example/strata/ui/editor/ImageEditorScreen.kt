package com.example.strata.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// ImageEditorScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ImageEditorScreen(
    imageUri: String,
    editorViewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val imageEditorViewModel: ImageEditorViewModel = viewModel()

    // ── State from ViewModel ──────────────────────────────────────────────
    val rotationDegrees by imageEditorViewModel.rotationDegrees.collectAsState()
    val zoomScale       by imageEditorViewModel.zoomScale.collectAsState()
    val panOffset       by imageEditorViewModel.panOffset.collectAsState()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading    by remember { mutableStateOf(true) }

    // Load bitmap once on first composition
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val bmp = decodeBitmapFromUri(context, imageUri)
            withContext(Dispatchers.Main) {
                isLoading = false
                if (bmp != null) {
                    imageEditorViewModel.setOriginalBitmap(bmp)
                    loadedBitmap = bmp
                }
            }
        }
    }

    // Animated rotation for smooth button taps
    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegrees.toFloat(),
        animationSpec = spring(stiffness = 400f),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // ── Image Preview Area ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .align(Alignment.TopCenter)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures(panZoomLock = false) { _, pan, zoom, _ ->
                        val newScale = (zoomScale * zoom).coerceIn(1f, 3f)
                        imageEditorViewModel.setZoom(newScale)

                        val maxPanX = ((newScale - 1f) / newScale) * size.width / 2f
                        val maxPanY = ((newScale - 1f) / newScale) * size.height / 2f
                        val newPanX = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX)
                        val newPanY = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                        imageEditorViewModel.setPan(Offset(newPanX, newPanY))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Checkerboard tint behind transparent images
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF161616))
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFFA6000),
                    modifier = Modifier.size(48.dp)
                )
            }

            loadedBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Image preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ    = animatedRotation
                            scaleX       = zoomScale
                            scaleY       = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                )
            }

            // Rotation badge overlay
            if (rotationDegrees != 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${rotationDegrees}°",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Control Panel ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF111111).copy(alpha = 0.95f),
                            Color(0xFF111111)
                        ),
                        startY = 0f,
                        endY = 80f
                    ),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ── Zoom Slider ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZOOM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Text(
                        text = "${(zoomScale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFFA6000),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Slider(
                    value = zoomScale,
                    onValueChange = { imageEditorViewModel.setZoom(it) },
                    valueRange = 1f..3f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFA6000),
                        activeTrackColor = Color(0xFFFA6000),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // ── Rotation Buttons ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ROTATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), CircleShape)
                            .clickable { imageEditorViewModel.rotateRight() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.RotateRight,
                            contentDescription = "Rotate",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // ── Apply / Cancel ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val baked = imageEditorViewModel.applyEdits()
                        if (baked != null) {
                            editorViewModel.setEditedBackground(baked)
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFA6000),
                        contentColor   = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }

        // ── Top App Bar ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Default.Close,
                contentDescription = "Close",
                onClick = onBack
            )

            Text(
                text = "Adjust Photo",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.7f), blurRadius = 8f)
                ),
                textAlign = TextAlign.Center
            )

            // Reset button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { imageEditorViewModel.resetAll() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↺",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RotationButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Decodes a [Bitmap] from a content/file URI, down-sampling to max 2048 px
 * on the longest side to keep memory usage reasonable.
 */
private fun decodeBitmapFromUri(context: Context, uriString: String): Bitmap? {
    return try {
        val uri = Uri.parse(uriString)
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        val maxDim = 2048
        val sample = maxOf(opts.outWidth / maxDim, opts.outHeight / maxDim, 1)
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
