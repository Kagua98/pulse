package io.pulse.internal

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.drawable.IconCompat

/**
 * Creates a monochrome wrench/spanner [IconCompat] for use as the notification
 * small icon. The design matches the FAB's [PulseIcon] composable.
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

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }

        // Rotate canvas -45° around center
        canvas.save()
        canvas.rotate(-45f, w / 2f, h / 2f)

        val cx = w * 0.5f
        val handleW = w * 0.18f
        val headW = w * 0.44f
        val jawW = w * 0.13f
        val halfHandle = handleW / 2f
        val halfHead = headW / 2f

        // Handle
        val hr = halfHandle
        canvas.drawRoundRect(
            RectF(cx - halfHandle, h * 0.42f, cx + halfHandle, h * 0.94f),
            hr, hr, paint,
        )

        // Shoulder
        val sr = w * 0.04f
        canvas.drawRoundRect(
            RectF(cx - halfHead, h * 0.30f, cx + halfHead, h * 0.46f),
            sr, sr, paint,
        )

        // Left jaw
        val jr = jawW * 0.35f
        canvas.drawRoundRect(
            RectF(cx - halfHead, h * 0.08f, cx - halfHead + jawW, h * 0.36f),
            jr, jr, paint,
        )

        // Right jaw
        canvas.drawRoundRect(
            RectF(cx + halfHead - jawW, h * 0.08f, cx + halfHead, h * 0.36f),
            jr, jr, paint,
        )

        canvas.restore()

        return IconCompat.createWithBitmap(bitmap)
    }
}
