package com.example.strata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun ColorPickerCircle(
    modifier: Modifier = Modifier,
    initialColor: Color = Color.White,
    onColorSelected: (Color) -> Unit
) {
    val spectralColors = listOf(
        Color.Red,
        Color.Magenta,
        Color.Blue,
        Color.Cyan,
        Color.Green,
        Color.Yellow,
        Color.Red
    )

    var currentPosition by remember { mutableStateOf<Offset?>(null) }
    var containerWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    currentPosition = offset
                    updateColorAndNotify(offset, size.width.toFloat(), onColorSelected)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    currentPosition = change.position
                    updateColorAndNotify(change.position, size.width.toFloat(), onColorSelected)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            containerWidth = size.width
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Hue sweep gradient
            drawCircle(
                brush = Brush.sweepGradient(spectralColors, center = center),
                radius = radius,
                center = center
            )

            // Saturation radial gradient (white in center, transparent at edge)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Draw a selector ring where the user clicked/dragged
            currentPosition?.let { pos ->
                val dx = pos.x - center.x
                val dy = pos.y - center.y
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                val selectorPos = if (dist > radius) {
                    Offset(
                        center.x + (dx / dist) * radius,
                        center.y + (dy / dist) * radius
                    )
                } else {
                    pos
                }

                drawCircle(
                    color = Color.Black,
                    radius = 12f,
                    center = selectorPos,
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = selectorPos,
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}

private fun updateColorAndNotify(pos: Offset, width: Float, onColorSelected: (Color) -> Unit) {
    val cx = width / 2
    val cy = width / 2
    val radius = width / 2

    val dx = pos.x - cx
    val dy = pos.y - cy
    val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

    var angle = atan2(dy.toDouble(), dx.toDouble())
    if (angle < 0) {
        angle += 2 * Math.PI
    }

    val hue = (angle / (2 * Math.PI) * 360f).toFloat()
    val saturation = (dist / radius).coerceIn(0f, 1f)
    val value = 1f 

    val hsv = floatArrayOf(hue, saturation, value)
    val colorInt = android.graphics.Color.HSVToColor(hsv)
    onColorSelected(Color(colorInt))
}
