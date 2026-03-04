package io.pulse

import io.pulse.log.LogEntry
import io.pulse.log.LogLevel
import io.pulse.log.LogStore
import io.pulse.log.callerTag
import io.pulse.log.epochMillis
import io.pulse.log.platformLog
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Lightweight logging API — use this in feature modules.
 *
 * Tag is auto-generated from the calling class (like Timber),
 * or you can provide one explicitly.
 *
 * ```
 * Pulse.d("Token refreshed")                    // tag = calling class
 * Pulse.d("Token refreshed", tag = "AuthRepo")  // explicit tag
 * Pulse.e("Request failed", throwable = ex)
 * ```
 */
object Pulse {

    val logStore = LogStore()

    /** All stored log entries as a reactive flow. */
    val logs: StateFlow<List<LogEntry>> get() = logStore.logs

    @OptIn(ExperimentalUuidApi::class)
    fun log(level: LogLevel, message: String, tag: String? = null, throwable: Throwable? = null) {
        val resolvedTag = tag ?: callerTag()
        logStore.add(
            LogEntry(
                id = Uuid.random().toString(),
                level = level,
                tag = resolvedTag,
                message = message,
                throwable = throwable?.stackTraceToString(),
                timestamp = epochMillis(),
            ),
        )
        platformLog(level, resolvedTag, message, throwable)
    }

    fun v(message: String, tag: String? = null) = log(LogLevel.VERBOSE, message, tag)
    fun d(message: String, tag: String? = null) = log(LogLevel.DEBUG, message, tag)
    fun i(message: String, tag: String? = null) = log(LogLevel.INFO, message, tag)
    fun w(message: String, tag: String? = null, throwable: Throwable? = null) =
        log(LogLevel.WARN, message, tag, throwable)

    fun e(message: String, tag: String? = null, throwable: Throwable? = null) =
        log(LogLevel.ERROR, message, tag, throwable)

    fun clearLogs() = logStore.clear()
}
