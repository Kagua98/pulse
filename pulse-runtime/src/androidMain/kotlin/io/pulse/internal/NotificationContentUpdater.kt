package io.pulse.internal

import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.snapshotFlow
import androidx.core.app.NotificationCompat
import io.pulse.Pulse
import io.pulse.PulseCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Observes Pulse data stores and notification content type, then posts
 * dynamic InboxStyle notifications that update at most 2x/sec.
 */
@OptIn(FlowPreview::class)
internal class NotificationContentUpdater(private val context: Context) {

    private var scope: CoroutineScope? = null
    private var job: Job? = null

    fun start() {
        if (scope != null) return
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = newScope

        ensureNotificationChannel(context)

        job = newScope.launch {
            val contentTypeFlow = snapshotFlow { PulseCore.notificationContentType }

            combine(
                PulseCore.transactions,
                Pulse.logs,
                PulseCore.crashes,
                contentTypeFlow,
            ) { transactions, logs, crashes, contentType ->
                NotificationContentFormatter.format(contentType, transactions, logs, crashes)
            }
                .debounce(500L)
                .distinctUntilChanged()
                .collect { content -> postNotification(content) }
        }
    }

    fun stop() {
        scope?.cancel()
        scope = null
        job = null
    }

    private fun postNotification(content: NotificationContent) {
        val builder = buildBaseNotificationBuilder(context)
            .setContentTitle(content.title)
            .setContentText(content.text)

        if (content.lines.isNotEmpty()) {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(content.title)
                .setSummaryText(content.text)
            content.lines.forEach { line -> inboxStyle.addLine(line) }
            builder.setStyle(inboxStyle)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(PULSE_NOTIFICATION_ID, builder.build())
    }
}
