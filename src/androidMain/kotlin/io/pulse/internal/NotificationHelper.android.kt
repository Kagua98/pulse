package io.pulse.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

internal const val PULSE_CHANNEL_ID = "Pulse_channel"
internal const val PULSE_NOTIFICATION_ID = 0x504C // "PL"

internal fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            PULSE_CHANNEL_ID,
            "Pulse Developer Tools",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Persistent notification for accessing the Pulse inspector"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
}

internal fun createPulsePendingIntent(context: Context): PendingIntent? {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra("pulse_notification_tap", true)
    }
    return if (launchIntent != null) {
        PendingIntent.getActivity(
            context,
            PULSE_NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    } else {
        null
    }
}

internal fun buildBaseNotificationBuilder(context: Context): NotificationCompat.Builder {
    val pendingIntent = createPulsePendingIntent(context)
    return NotificationCompat.Builder(context, PULSE_CHANNEL_ID)
        .setSmallIcon(PulseNotificationIcon.get())
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .apply {
            if (pendingIntent != null) setContentIntent(pendingIntent)
        }
}

internal actual fun showPersistentNotification(context: Any?) {
    val ctx = context as? Context ?: return

    ensureNotificationChannel(ctx)

    val notification = buildBaseNotificationBuilder(ctx)
        .setContentTitle("Pulse Developer Tools")
        .setContentText("Tap to open inspector")
        .build()

    val notificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(PULSE_NOTIFICATION_ID, notification)
}

internal actual fun dismissPersistentNotification(context: Any?) {
    val ctx = context as? Context ?: return
    val notificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(PULSE_NOTIFICATION_ID)
}
