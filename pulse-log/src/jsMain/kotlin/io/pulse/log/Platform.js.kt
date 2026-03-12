package io.pulse.log

import kotlin.js.Date

internal actual fun epochMillis(): Long = Date.now().toLong()

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val text = "${level.label}/$tag: $message"
    when (level) {
        LogLevel.ERROR -> console.error(text)
        LogLevel.WARN -> console.warn(text)
        else -> console.log(text)
    }
    throwable?.let { console.error(it) }
}

internal actual fun callerTag(): String = "Pulse"
