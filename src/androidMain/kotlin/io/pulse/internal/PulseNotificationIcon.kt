package io.pulse.internal

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.drawable.IconCompat

/**
 * Creates a monochrome pulse-waveform [IconCompat] for use as the notification
 * small icon. The path matches the FAB's [PulseIcon] composable so both icons
 * are visually identical.
 *
 * Status-bar icons use only the alpha channel, so we draw white-on-transparent.
 */
internal object PulseNotificationIcon {

    private var cached: IconCompat? = null

    fun get(): IconCompat = cached ?: create().also { cached = it }

    private fun create(): IconCompat {
        val size = 96 // px — sharp at all densities
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val w = size.toFloat()
        val h = size.toFloat()
        // Vertical padding so the waveform isn't edge-to-edge
        val pad = h * 0.18f
        val drawH = h - pad * 2
        val mid = pad + drawH * 0.5f
        val strokeWidth = w * 0.06f

        val path = Path().apply {
            moveTo(w * 0.04f, mid)
            // Flat lead-in
            lineTo(w * 0.15f, mid)
            // Small P-wave bump
            lineTo(w * 0.22f, mid - drawH * 0.10f)
            lineTo(w * 0.28f, mid)
            // Lead into QRS: slight dip
            lineTo(w * 0.33f, mid + drawH * 0.04f)
            // Sharp R-wave spike up
            lineTo(w * 0.40f, pad + drawH * 0.08f)
            // Deep S-wave trough
            lineTo(w * 0.48f, pad + drawH * 0.82f)
            // Recovery back to baseline
            lineTo(w * 0.55f, mid - drawH * 0.06f)
            lineTo(w * 0.60f, mid)
            // Gentle T-wave
            lineTo(w * 0.68f, mid - drawH * 0.12f)
            lineTo(w * 0.76f, mid)
            // Flat tail-out
            lineTo(w * 0.96f, mid)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt() // white — system tints it
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.strokeWidth = strokeWidth
        }

        canvas.drawPath(path, paint)

        // Dot at R-wave peak
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(w * 0.40f, pad + drawH * 0.08f, strokeWidth * 1.1f, dotPaint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
