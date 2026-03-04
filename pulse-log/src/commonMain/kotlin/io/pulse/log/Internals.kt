package io.pulse.log

internal expect fun epochMillis(): Long

internal expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)

internal expect fun callerTag(): String
