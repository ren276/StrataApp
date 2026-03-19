package com.example.strata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MapRenderer(
    polyline: String,
    modifier: Modifier = Modifier,
    lineColor: Color,
    lineWidth: Float = 8f
) {
    val points = remember(polyline) { decodePolyline(polyline) }
    
    if (points.isEmpty()) return
    
    val minLat = points.minOf { it.first }
    val maxLat = points.maxOf { it.first }
    val minLng = points.minOf { it.second }
    val maxLng = points.maxOf { it.second }
    
    val latRange = maxLat - minLat
    val lngRange = maxLng - minLng
    
    if (latRange == 0.0 || lngRange == 0.0) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx() // Reduced padding for smaller cards
        
        val availableWidth = width - 2 * padding
        val availableHeight = height - 2 * padding
        
        val scaleX = availableWidth / lngRange
        val scaleY = availableHeight / latRange
        val scale = minOf(scaleX, scaleY)
        
        // Center the map
        val outputWidth = lngRange * scale
        val outputHeight = latRange * scale
        val offsetX = (width - outputWidth) / 2
        val offsetY = (height - outputHeight) / 2
        
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = offsetX + (point.second - minLng) * scale
            // Latitude increases upwards, screen Y increases downwards
            val y = height - (offsetY + (point.first - minLat) * scale)
            
            if (index == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = lineWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

fun decodePolyline(encoded: String): List<Pair<Double, Double>> {
    val poly = ArrayList<Pair<Double, Double>>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = Pair(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }

    return poly
}
