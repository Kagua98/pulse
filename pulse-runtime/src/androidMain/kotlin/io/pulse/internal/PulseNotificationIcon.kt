package io.pulse.internal

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.drawable.IconCompat

/**
 * Creates a monochrome debug/bug [IconCompat] for use as the notification
 * small icon. The design matches the FAB's PulseIcon composable.
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

        val sx = size / 24f
        val sy = size / 24f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }

        val path = Path().apply {
            // Antenna + legs
            moveTo(10.94f * sx, 13.5f * sy)
            lineTo(9.62f * sx, 14.82f * sy)
            cubicTo(8.8f * sx, 13.1f * sy, 5.3f * sx, 12.6f * sy, 3.38f * sx, 14.82f * sy)
            lineTo(1.06f * sx, 13.5f * sy)
            lineTo(0f * sx, 14.56f * sy)
            lineTo(1.72f * sx, 16.28f * sy)
            lineTo(1.5f * sx, 16.5f * sy)
            lineTo(1.5f * sx, 18f * sy)
            lineTo(0f * sx, 18f * sy)
            lineTo(0f * sx, 19.5f * sy)
            lineTo(1.5f * sx, 19.5f * sy)
            lineTo(1.5f * sx, 19.58f * sy)
            cubicTo(1.577f * sx, 20.069f * sy, 1.714f * sx, 20.546f * sy, 1.91f * sx, 21f * sy)
            lineTo(0f * sx, 22.94f * sy)
            lineTo(1.06f * sx, 24f * sy)
            lineTo(2.71f * sx, 22.35f * sy)
            cubicTo(3.71f * sx, 23.35f * sy, 4.81f * sx, 24f * sy, 6f * sx, 24f * sy)
            cubicTo(7.19f * sx, 24f * sy, 8.29f * sx, 23.35f * sy, 9.29f * sx, 22.35f * sy)
            lineTo(10.94f * sx, 24f * sy)
            lineTo(12f * sx, 22.94f * sy)
            lineTo(10.09f * sx, 21f * sy)
            cubicTo(10.288f * sx, 20.536f * sy, 10.426f * sx, 20.049f * sy, 10.5f * sx, 19.55f * sy)
            lineTo(10.5f * sx, 19.45f * sy)
            lineTo(12f * sx, 19.45f * sy)
            lineTo(12f * sx, 18f * sy)
            lineTo(10.5f * sx, 18f * sy)
            lineTo(10.5f * sx, 16.5f * sy)
            lineTo(10.28f * sx, 16.28f * sy)
            lineTo(12f * sx, 14.56f * sy)
            lineTo(10.94f * sx, 13.5f * sy)
            close()

            // Bug head
            moveTo(6f * sx, 13.5f * sy)
            cubicTo(7.24f * sx, 13.5f * sy, 8.25f * sx, 14.51f * sy, 8.25f * sx, 15.75f * sy)
            lineTo(3.75f * sx, 15.75f * sy)
            cubicTo(3.75f * sx, 14.51f * sy, 4.76f * sx, 13.5f * sy, 6f * sx, 13.5f * sy)
            close()

            // Bug body
            moveTo(9f * sx, 19.5f * sy)
            cubicTo(9f * sx, 21.16f * sy, 7.66f * sx, 22.5f * sy, 6f * sx, 22.5f * sy)
            cubicTo(4.34f * sx, 22.5f * sy, 3f * sx, 21.16f * sy, 3f * sx, 19.5f * sy)
            lineTo(3f * sx, 17.25f * sy)
            lineTo(9f * sx, 17.25f * sy)
            lineTo(9f * sx, 19.5f * sy)
            close()

            // Play/signal triangle
            moveTo(23.76f * sx, 0.6f * sy)
            lineTo(23.76f * sx, 1.86f * sy)
            lineTo(13.5f * sx, 8.37f * sy)
            lineTo(13.5f * sx, 6.6f * sy)
            lineTo(22f * sx, 1.23f * sy)
            lineTo(9f * sx, 2f * sy)
            lineTo(9f * sx, 11.46f * sy)
            cubicTo(8.51f * sx, 11.18f * sy, 7.99f * sx, 10.97f * sy, 7.5f * sx, 10.74f * sy)
            lineTo(7.5f * sx, 0.63f * sy)
            lineTo(8.64f * sx, 0f * sy)
            lineTo(23.76f * sx, 9.6f * sy)
            close()
        }

        canvas.drawPath(path, paint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
