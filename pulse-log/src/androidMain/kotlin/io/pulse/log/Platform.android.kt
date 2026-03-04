package io.pulse.log

import android.util.Log

internal actual fun epochMillis(): Long = System.currentTimeMillis()

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    when (level) {
        LogLevel.VERBOSE -> Log.v(tag, message, throwable)
        LogLevel.DEBUG -> Log.d(tag, message, throwable)
        LogLevel.INFO -> Log.i(tag, message, throwable)
        LogLevel.WARN -> Log.w(tag, message, throwable)
        LogLevel.ERROR -> Log.e(tag, message, throwable)
    }
}

private val IGNORED = setOf(
    "io.pulse.Pulse",
)

internal actual fun callerTag(): String {
    val stack = Throwable().stackTrace
    val frame = stack.firstOrNull { element ->
        val className = element.className.substringBefore('$')
        className !in IGNORED &&
            !className.startsWith("io.pulse.log.")
    }
    return frame?.className
        ?.substringAfterLast('.')
        ?.substringBefore('$')
        ?.take(23) // Android Logcat tag limit
        ?: "Pulse"
}
