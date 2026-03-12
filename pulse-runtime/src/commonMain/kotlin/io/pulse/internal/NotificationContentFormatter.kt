package io.pulse.internal

import io.pulse.NotificationContentType
import io.pulse.model.CrashEntry
import io.pulse.model.HttpTransaction
import io.pulse.log.LogEntry
import io.pulse.log.LogLevel
/**
 * Formatted notification content ready to be posted.
 */
internal data class NotificationContent(
    val title: String,
    val text: String,
    val lines: List<String>,
)

/**
 * Pure formatting logic for notification content — no platform dependencies.
 */
internal object NotificationContentFormatter {

    fun format(
        type: NotificationContentType,
        transactions: List<HttpTransaction>,
        logs: List<LogEntry>,
        crashes: List<CrashEntry>,
    ): NotificationContent = when (type) {
        NotificationContentType.NetworkActivity -> formatNetworkActivity(transactions)
        NotificationContentType.LogSummary -> formatLogSummary(logs)
        NotificationContentType.AppHealth -> formatAppHealth(transactions, logs, crashes)
    }

    private fun formatNetworkActivity(transactions: List<HttpTransaction>): NotificationContent {
        val total = transactions.size

        if (transactions.isEmpty()) {
            return NotificationContent(
                title = "Pulse",
                text = "No requests yet",
                lines = emptyList(),
            )
        }

        // Show the most recent transaction as the collapsed text (like Chucker)
        val latest = transactions.first()
        val latestCode = latest.responseCode?.toString() ?: "..."
        val latestDuration = if (latest.duration > 0) " (${latest.duration}ms)" else ""
        val text = "$latestCode ${latest.method} ${latest.path}$latestDuration"

        // Expanded InboxStyle: last 5 transactions
        val lines = transactions.take(5).map { txn ->
            val code = txn.responseCode?.toString() ?: "..."
            val duration = if (txn.duration > 0) "(${txn.duration}ms)" else ""
            "$code  ${txn.method}  ${txn.path}  $duration".trim()
        }

        return NotificationContent(
            title = "Pulse \u2014 $total request${if (total != 1) "s" else ""}",
            text = text,
            lines = lines,
        )
    }

    private fun formatLogSummary(logs: List<LogEntry>): NotificationContent {
        val total = logs.size
        val counts = LogLevel.entries.associateWith { level -> logs.count { it.level == level } }
        val parts = LogLevel.entries.reversed().mapNotNull { level ->
            val count = counts[level] ?: 0
            if (count > 0) "${count}${level.label}" else null
        }
        val text = if (parts.isEmpty()) "No logs yet" else parts.joinToString(" / ")

        val lines = logs.take(5).map { entry ->
            "${entry.level.label}  [${entry.tag}] ${entry.message}"
        }

        return NotificationContent(
            title = "Pulse \u2014 $total log${if (total != 1) "s" else ""}",
            text = text,
            lines = lines,
        )
    }

    private fun formatAppHealth(
        transactions: List<HttpTransaction>,
        logs: List<LogEntry>,
        crashes: List<CrashEntry>,
    ): NotificationContent {
        val crashCount = crashes.size
        val errorLogs = logs.count { it.level == LogLevel.ERROR }
        val failedRequests = transactions.count {
            it.isClientError || it.isServerError || it.isFailed
        }

        val text = "$crashCount crash${if (crashCount != 1) "es" else ""}" +
            " / $errorLogs error${if (errorLogs != 1) "s" else ""}" +
            " / $failedRequests failed request${if (failedRequests != 1) "s" else ""}"

        // Most severe items first: crashes, then failed requests, then error logs
        val lines = buildList {
            crashes.take(2).forEach { crash ->
                add("CRASH  ${crash.exceptionClass}: ${crash.message}")
            }
            transactions.filter { it.isClientError || it.isServerError || it.isFailed }
                .take(2)
                .forEach { txn ->
                    val code = txn.responseCode?.toString() ?: "ERR"
                    add("$code  ${txn.method}  ${txn.path}")
                }
            logs.filter { it.level == LogLevel.ERROR }
                .take(5 - size)
                .forEach { entry ->
                    add("E  [${entry.tag}] ${entry.message}")
                }
        }.take(5)

        return NotificationContent(
            title = "Pulse \u2014 App Health",
            text = text,
            lines = lines,
        )
    }
}
