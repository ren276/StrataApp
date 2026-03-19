package com.example.strata.ui.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel scoped to the ImageEditorScreen back-stack entry.
 *
 * Responsibilities:
 *  - Cache the original bitmap so the user can reset without re-loading from disk.
 *  - Expose rotation (multiples of 90°), zoom (1f–3f), and pan offset as state.
 *  - Bake all transforms into a new Bitmap when the user taps "Apply".
 */
class ImageEditorViewModel : ViewModel() {

    // ── Original bitmap (never mutated) ────────────────────────────────────
    private var originalBitmap: Bitmap? = null

    fun setOriginalBitmap(bmp: Bitmap) {
        if (originalBitmap == null) {          // cache only once
            originalBitmap = bmp
        }
    }

    fun getOriginalBitmap(): Bitmap? = originalBitmap

    // ── Rotation state ─────────────────────────────────────────────────────
    private val _rotationDegrees = MutableStateFlow(0)
    /** Cumulative rotation in degrees; always a multiple of 90 in range [0, 360). */
    val rotationDegrees: StateFlow<Int> = _rotationDegrees.asStateFlow()

    fun rotateLeft() {
        _rotationDegrees.value = (_rotationDegrees.value - 90 + 360) % 360
        resetPan()
    }

    fun rotateRight() {
        _rotationDegrees.value = (_rotationDegrees.value + 90) % 360
        resetPan()
    }

    // ── Zoom state (1× – 3×) ───────────────────────────────────────────────
    private val _zoomScale = MutableStateFlow(1f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    fun setZoom(scale: Float) {
        _zoomScale.value = scale.coerceIn(1f, 3f)
    }

    // ── Pan state ──────────────────────────────────────────────────────────
    private val _panOffset = MutableStateFlow(Offset.Zero)
    val panOffset: StateFlow<Offset> = _panOffset.asStateFlow()

    fun setPan(offset: Offset) {
        _panOffset.value = offset
    }

    private fun resetPan() {
        _panOffset.value = Offset.Zero
    }

    // ── Reset all ──────────────────────────────────────────────────────────
    fun resetAll() {
        _rotationDegrees.value = 0
        _zoomScale.value = 1f
        _panOffset.value = Offset.Zero
    }

    // ── Bake transforms into a new Bitmap ──────────────────────────────────
    /**
     * Applies the current rotation, zoom, and pan to [originalBitmap] and
     * returns the resulting Bitmap ready to be used as the editor background.
     *
     * The output bitmap dimensions match the SOURCE bitmap dimensions so that
     * no quality is lost; the pan offset is interpreted as a pixel-level crop
     * inside the zoomed image, i.e. the viewport shifts by (-panX, -panY) in
     * source-image coordinates before zoom is applied.
     */
    fun applyEdits(): Bitmap? {
        val src = originalBitmap ?: return null
        val rotation = _rotationDegrees.value.toFloat()
        val zoom = _zoomScale.value
        val pan = _panOffset.value

        // --- Step 1: rotate the source bitmap ---
        val rotMatrix = Matrix().apply { postRotate(rotation) }
        val rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, rotMatrix, true)

        // --- Step 2: apply zoom + pan crop ---
        // Output canvas matches the rotated bitmap size.
        val outW = rotated.width
        val outH = rotated.height

        val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Center transform: scale from center then translate by pan (in output px)
        val scaleMatrix = Matrix().apply {
            // Scale from center of the rotated bitmap
            postScale(zoom, zoom, outW / 2f, outH / 2f)
            // Translate by the pan amount (we invert it so dragging right shifts view left)
            postTranslate(-pan.x, -pan.y)
        }

        val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(rotated, scaleMatrix, paint)

        return result
    }
}
