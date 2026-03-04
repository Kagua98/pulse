package io.pulse.log

import platform.Foundation.NSDate
import platform.Foundation.NSLog
import platform.Foundation.timeIntervalSince1970

internal actual fun epochMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    NSLog("${level.label}/$tag: $message")
    if (throwable != null) {
        NSLog("${throwable.stackTraceToString()}")
    }
}

internal actual fun callerTag(): String {
    // Kotlin/Native stack traces are not reliably parseable at runtime
    return "Pulse"
}
