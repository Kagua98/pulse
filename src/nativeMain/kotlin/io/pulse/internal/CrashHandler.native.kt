package io.pulse.internal

internal actual fun installCrashHandler(onCrash: (threadName: String, throwable: Throwable) -> Unit) {
    // Native crash handling requires platform-specific setup (e.g., NSSetUncaughtExceptionHandler on iOS)
}
