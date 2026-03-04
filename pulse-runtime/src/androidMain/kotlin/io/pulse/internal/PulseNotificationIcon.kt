package io.pulse.internal

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.drawable.IconCompat

/**
 * Creates a monochrome radar-ping [IconCompat] for use as the notification
 * small icon. The design matches the FAB's [PulseIcon] composable: a center
 * dot with three concentric arcs radiating outward.
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
        val strokeWidth = w * 0.07f

        // Center dot at ~35% left, ~65% down (matches composable)
        val cx = w * 0.35f
        val cy = h * 0.65f

        val white = 0xFFFFFFFF.toInt()

        // Center dot
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, w * 0.065f, dotPaint)

        // Three concentric arcs radiating upper-right
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = strokeWidth
        }

        val alphas = intArrayOf(255, 179, 102) // 1.0, 0.7, 0.4
        val radii = floatArrayOf(w * 0.22f, w * 0.38f, w * 0.54f)

        for (i in alphas.indices) {
            val r = radii[i]
            arcPaint.alpha = alphas[i]
            val rect = RectF(cx - r, cy - r, cx + r, cy + r)
            canvas.drawArc(rect, -135f, 90f, false, arcPaint)
        }

        return IconCompat.createWithBitmap(bitmap)
    }
}
