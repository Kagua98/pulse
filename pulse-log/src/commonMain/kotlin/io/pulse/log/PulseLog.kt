package io.pulse.log

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Lightweight logging API for Pulse — use this in feature modules.
 *
 * Works like Timber: call static methods from anywhere. Logs are stored
 * in memory and also printed to the platform logger (Logcat on Android).
 *
 * ```
 * PulseLog.d("AuthRepo", "Token refreshed")
 * PulseLog.e("Network", "Request failed", exception)
 * ```
 */
object PulseLog {

    val store = LogStore()

    @OptIn(ExperimentalUuidApi::class)
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        store.add(
            LogEntry(
                id = Uuid.random().toString(),
                level = level,
                tag = tag,
                message = message,
                throwable = throwable?.stackTraceToString(),
                timestamp = epochMillis(),
            ),
        )
        platformLog(level, tag, message, throwable)
    }

    fun v(tag: String, message: String) = log(LogLevel.VERBOSE, tag, message)
    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.WARN, tag, message, throwable)

    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.ERROR, tag, message, throwable)

    fun clear() = store.clear()
}

internal expect fun epochMillis(): Long

internal expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)
