package com.example.strata.ui.editor

import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.strata.data.local.ActivityEntity
import com.example.strata.data.model.ActivityTemplate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

/**
 * Renders a template onto a Compose Canvas.
 *
 * @param visibleFields Controls which data labels to draw.
 *   Defaults to all fields visible. Pass a subset to hide specific fields.
 *   Recognised keys: "title", "date", "distance", "duration", "elevation".
 */
@Composable
fun ActivityTemplateRenderer(
    activity: ActivityEntity,
    template: ActivityTemplate,
    modifier: Modifier = Modifier,
    overrideTitle: String? = null,
    overrideDistance: String? = null,
    overrideDuration: String? = null,
    overrideElevation: String? = null,
    overrideDate: String? = null,
    overridePrimaryColor: androidx.compose.ui.graphics.Color? = null,
    overrideSecondaryColor: androidx.compose.ui.graphics.Color? = null,
    visibleFields: Set<String> = setOf("title", "date", "distance", "duration", "elevation")
) {
    val distanceStr = String.format(Locale.US, "%.2f", activity.distance / 1000f)
    val distanceWithUnit = overrideDistance ?: "$distanceStr KM"
    val durationMin = activity.movingTime / 60
    val durationFormatted = overrideDuration ?: "${durationMin / 60}H ${durationMin % 60}M"
    val elevationStr = overrideElevation ?: "${activity.totalElevationGain.toInt()}M ELEV"
    val dateStr = overrideDate ?: DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(activity.date).uppercase()
    val title = overrideTitle ?: activity.title.uppercase(Locale.getDefault())

    val tempForColors = if (overridePrimaryColor != null || overrideSecondaryColor != null) {
        ActivityTemplate.Custom(
            base = template,
            primary = overridePrimaryColor ?: template.primaryColor,
            secondary = overrideSecondaryColor ?: template.secondaryColor
        )
    } else template

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minDim = min(width, height)

        drawContext.canvas.nativeCanvas.apply {
            when (template) {
                is ActivityTemplate.ThreeDee1      -> renderThreeDee1(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                is ActivityTemplate.ThreeDee2      -> renderThreeDee2(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                is ActivityTemplate.Curved1        -> renderCurved1(this, width, height, minDim, title, distanceWithUnit, durationFormatted, tempForColors, visibleFields)
                is ActivityTemplate.Curved2        -> renderCurved2(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                is ActivityTemplate.GradientShadow1 -> renderGradientShadow1(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                is ActivityTemplate.GradientShadow2 -> renderGradientShadow2(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                is ActivityTemplate.MinimalistLines -> renderMinimalistLines(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, dateStr, tempForColors, visibleFields)
                is ActivityTemplate.BoldGeometric  -> renderBoldGeometric(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                is ActivityTemplate.VerticalStack  -> renderVerticalStack(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, dateStr, tempForColors, visibleFields)
                is ActivityTemplate.RotationTransform -> renderRotationTransform(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
                else -> renderThreeDee1(this, width, height, title, distanceWithUnit, durationFormatted, elevationStr, tempForColors, visibleFields)
            }
        }
    }
}

private fun createPaint(
    colorArgb: Int,
    textSizePx: Float,
    typeface: Typeface = Typeface.DEFAULT_BOLD,
    align: Paint.Align = Paint.Align.CENTER
) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = colorArgb
    this.textSize = textSizePx
    this.typeface = typeface
    this.textAlign = align
}

// 1. Retro 3D — extruded perspective text
private fun renderThreeDee1(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb()
    val secondary = temp.secondaryColor.toArgb()
    val titlePaint = createPaint(secondary, w * 0.12f, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC))
    val distPaint  = createPaint(secondary, w * 0.25f, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC))
    val subPaint   = createPaint(secondary, w * 0.08f, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC))

    for (i in 20 downTo 0) {
        val offset = i * (w * 0.005f)
        val factor = (1f - i * 0.02f).coerceAtLeast(0f)
        val depthColor = AndroidColor.argb(255,
            (AndroidColor.red(secondary) * factor).toInt().coerceIn(0, 255),
            (AndroidColor.green(secondary) * factor).toInt().coerceIn(0, 255),
            (AndroidColor.blue(secondary) * factor).toInt().coerceIn(0, 255)
        )
        val c = if (i == 0) primary else depthColor
        titlePaint.color = c; distPaint.color = c; subPaint.color = c

        if ("distance" in visible) canvas.drawText(dist,  w / 2 - offset, h * 0.45f + offset, distPaint)
        if ("title"    in visible) canvas.drawText(title, w / 2 - offset, h * 0.62f + offset, titlePaint)

        val sub = buildSub(dur, elev, visible, "  •  ")
        if (sub.isNotEmpty()) canvas.drawText(sub, w / 2 - offset, h * 0.73f + offset, subPaint)
    }
}

// 2. Neon Depth — isometric skew
private fun renderThreeDee2(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb()
    val paint = createPaint(primary, w * 0.18f, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), Paint.Align.LEFT)

    canvas.save()
    canvas.translate(w * 0.1f, h * 0.45f)
    canvas.rotate(-15f)
    canvas.skew(-0.3f, 0f)

    for (i in 10 downTo 0) {
        paint.color = if (i == 0) primary else AndroidColor.rgb(40, 40, 60)
        val off = i * 3f
        paint.textSize = w * 0.18f
        if ("distance" in visible) canvas.drawText(dist,  off, off, paint)
        if ("title"    in visible) canvas.drawText(title, off, off + paint.textSize * 1.2f, paint)
        paint.textSize = w * 0.08f
        val sub = buildSub(dur, elev, visible, "  ")
        if (sub.isNotEmpty()) canvas.drawText(sub, off, off + w * 0.18f * 2.5f, paint)
        paint.textSize = w * 0.18f
    }
    canvas.restore()
}

// 3. Cyclo Path — circular text on path
private fun renderCurved1(canvas: android.graphics.Canvas, w: Float, h: Float, minDim: Float, title: String, dist: String, dur: String, temp: ActivityTemplate, visible: Set<String>) {
    val r = minDim * 0.35f; val cx = w / 2; val cy = h / 2

    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = temp.primaryColor.toArgb(); style = Paint.Style.STROKE; strokeWidth = 4f; alpha = 80
    }
    canvas.drawCircle(cx, cy, r, ringPaint)
    canvas.drawCircle(cx, cy, r - minDim * 0.08f, ringPaint)

    val path  = Path().apply { addCircle(cx, cy, r,                   Path.Direction.CW) }
    val path2 = Path().apply { addCircle(cx, cy, r - minDim * 0.08f, Path.Direction.CCW) }
    val titlePaint = createPaint(temp.primaryColor.toArgb(),   minDim * 0.08f, align = Paint.Align.CENTER).apply { letterSpacing = 0.1f }
    val distPaint  = createPaint(temp.secondaryColor.toArgb(), minDim * 0.15f, align = Paint.Align.CENTER)

    if ("distance" in visible) canvas.drawText(dist, cx, cy + distPaint.textSize / 3, distPaint)
    if ("title"    in visible) canvas.drawTextOnPath(title, path,  0f, 0f, titlePaint)
    if ("duration" in visible) canvas.drawTextOnPath(dur,   path2, 0f, 0f, titlePaint)
}

// 4. Wave Data — text on wave path
private fun renderCurved2(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val path = Path().apply {
        moveTo(0f, h * 0.5f)
        quadTo(w * 0.25f, h * 0.3f, w * 0.5f, h * 0.5f)
        quadTo(w * 0.75f, h * 0.7f, w, h * 0.5f)
    }
    val titlePaint = createPaint(temp.secondaryColor.toArgb(), w * 0.08f, align = Paint.Align.LEFT)
    val distPaint  = createPaint(temp.primaryColor.toArgb(),   w * 0.18f, align = Paint.Align.LEFT)

    if ("distance" in visible) canvas.drawTextOnPath(dist,  path, w * 0.1f, -20f,                       distPaint)
    if ("title"    in visible) canvas.drawTextOnPath(title, path, w * 0.1f, titlePaint.textSize * 1.5f,  titlePaint)
    val sub = buildSub(dur, elev, visible, " | ")
    if (sub.isNotEmpty()) canvas.drawTextOnPath(sub, path, w * 0.1f, titlePaint.textSize * 2.8f, titlePaint)
}

// 5. Vaporwave Glow — neon glow text
private fun renderGradientShadow1(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb(); val secondary = temp.secondaryColor.toArgb()
    val distPaint = createPaint(AndroidColor.WHITE, w * 0.22f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)).apply { setShadowLayer(40f, 0f, 0f, primary) }
    val titlePaint = createPaint(AndroidColor.WHITE, w * 0.10f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD)).apply { setShadowLayer(30f, 0f, 0f, secondary) }

    if ("distance" in visible) canvas.drawText(dist,  w / 2, h * 0.40f, distPaint)
    if ("title"    in visible) canvas.drawText(title, w / 2, h * 0.55f, titlePaint)
    titlePaint.textSize = w * 0.06f
    val sub = buildSub(dur, elev, visible, "    ")
    if (sub.isNotEmpty()) canvas.drawText(sub, w / 2, h * 0.65f, titlePaint)
}

// 6. Sunset Blur — gradient shader
private fun renderGradientShadow2(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val distPaint = createPaint(AndroidColor.WHITE, w * 0.25f).apply {
        shader = LinearGradient(0f, h * 0.3f, 0f, h * 0.6f, temp.primaryColor.toArgb(), temp.secondaryColor.toArgb(), Shader.TileMode.CLAMP)
    }
    val titlePaint = createPaint(AndroidColor.WHITE, w * 0.08f).apply { alpha = 200 }
    val blurPaint  = Paint(distPaint).apply { setShadowLayer(60f, 0f, 20f, AndroidColor.BLACK) }

    if ("distance" in visible) { canvas.drawText(dist, w / 2, h * 0.45f, blurPaint); canvas.drawText(dist, w / 2, h * 0.45f, distPaint) }
    if ("title"    in visible) canvas.drawText(title, w / 2, h * 0.55f, titlePaint)
    val sub = buildSub(dur, elev, visible, " • ")
    if (sub.isNotEmpty()) canvas.drawText(sub, w / 2, h * 0.62f, titlePaint)
}

// 7. Swiss Lines — horizontal rule layout
private fun renderMinimalistLines(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, date: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb(); val secondary = temp.secondaryColor.toArgb()
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary; strokeWidth = 4f }
    val textPaint = createPaint(primary, w * 0.06f, Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL), Paint.Align.LEFT)
    val distPaint = createPaint(primary, w * 0.18f, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), Paint.Align.LEFT)
    val left = w * 0.1f; val right = w * 0.9f; var y = h * 0.55f

    canvas.drawLine(left, y, right, y, linePaint)
    if ("date" in visible) canvas.drawText(date, left, y - 10f, textPaint)
    y += h * 0.15f
    canvas.drawLine(left, y, right, y, linePaint)
    if ("distance" in visible) canvas.drawText(dist, left, y - 15f, distPaint)
    y += h * 0.10f
    canvas.drawLine(left, y, right, y, linePaint)
    val parts = mutableListOf<String>()
    if ("title"    in visible) parts.add(title)
    if ("duration" in visible) parts.add(dur)
    if ("elevation" in visible) parts.add(elev)
    if (parts.isNotEmpty()) canvas.drawText(parts.joinToString("  /  "), left, y - 15f, textPaint)
}

// 8. Block Action — bold geometric shapes
private fun renderBoldGeometric(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb(); val secondary = temp.secondaryColor.toArgb()
    canvas.drawRect(w * 0.05f, h * 0.35f, w * 0.95f, h * 0.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary })
    canvas.drawCircle(w * 0.8f, h * 0.425f, w * 0.1f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary })

    if ("distance" in visible) canvas.drawText(dist, w * 0.08f, h * 0.46f, createPaint(AndroidColor.BLACK, w * 0.16f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), Paint.Align.LEFT))
    if ("title"    in visible) canvas.drawText(title, w * 0.08f, h * 0.56f, createPaint(primary, w * 0.08f, align = Paint.Align.LEFT))
    val sub = buildSub(dur, elev, visible, "   |   ")
    if (sub.isNotEmpty()) canvas.drawText(sub, w * 0.08f, h * 0.62f, createPaint(AndroidColor.WHITE, w * 0.05f, align = Paint.Align.LEFT))
}

// 9. Stat Card — rounded card with stacked stats
private fun renderVerticalStack(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, date: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb()
    val left = w * 0.15f; val top = h * 0.25f; val right = w * 0.85f; val bottom = h * 0.78f
    canvas.drawRoundRect(left, top, right, bottom, 40f, 40f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(200, 0, 0, 0); setShadowLayer(20f, 0f, 10f, AndroidColor.BLACK) })

    val titlePaint = createPaint(primary, w * 0.07f, align = Paint.Align.LEFT)
    val distPaint  = createPaint(AndroidColor.WHITE, w * 0.18f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), Paint.Align.LEFT)
    val labelPaint = createPaint(AndroidColor.GRAY,   w * 0.04f, align = Paint.Align.LEFT)

    var y = top + h * 0.10f
    if ("title" in visible) { canvas.drawText(title, left + 40f, y, titlePaint) }; y += h * 0.03f
    if ("date"  in visible) { canvas.drawText(date,  left + 40f, y, labelPaint) }; y += h * 0.12f
    if ("distance" in visible) { canvas.drawText(dist, left + 40f, y, distPaint); canvas.drawText("DISTANCE", left + 40f, y + h * 0.04f, labelPaint) }; y += h * 0.09f
    if ("duration" in visible) { canvas.drawText(dur,  left + 40f, y, titlePaint); canvas.drawText("DURATION",  left + 40f, y + 30f, labelPaint) }; y += h * 0.09f
    if ("elevation" in visible) { canvas.drawText(elev, left + 40f, y, titlePaint); canvas.drawText("ELEVATION", left + 40f, y + 30f, labelPaint) }
}

// 10. X-Treme — intersecting rotated text
private fun renderRotationTransform(canvas: android.graphics.Canvas, w: Float, h: Float, title: String, dist: String, dur: String, elev: String, temp: ActivityTemplate, visible: Set<String>) {
    val primary = temp.primaryColor.toArgb(); val secondary = temp.secondaryColor.toArgb()
    val cx = w / 2; val cy = h / 2

    if ("distance" in visible) {
        canvas.save(); canvas.rotate(-45f, cx, cy)
        canvas.drawText(dist, cx, cy, createPaint(primary, w * 0.25f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)))
        canvas.restore()
    }

    canvas.save(); canvas.rotate(45f, cx, cy)
    val titlePaint = createPaint(secondary, w * 0.12f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
    val subPaint   = createPaint(AndroidColor.WHITE, w * 0.06f)
    val tw = titlePaint.measureText(title)
    canvas.drawRect(cx - tw / 2 - 20f, cy - titlePaint.textSize, cx + tw / 2 + 20f, cy + 20f, Paint().apply { color = AndroidColor.argb(200, 0, 0, 0) })
    if ("title" in visible) canvas.drawText(title, cx, cy, titlePaint)
    val sub = buildSub(dur, elev, visible, "  //  ")
    if (sub.isNotEmpty()) canvas.drawText(sub, cx, cy + titlePaint.textSize * 1.5f, subPaint)
    canvas.restore()
}

/** Builds a combined duration/elevation string respecting visibility. */
private fun buildSub(dur: String, elev: String, visible: Set<String>, separator: String): String {
    val parts = mutableListOf<String>()
    if ("duration"  in visible) parts.add(dur)
    if ("elevation" in visible) parts.add(elev)
    return parts.joinToString(separator)
}
