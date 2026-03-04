package io.pulse.internal

internal expect fun installCrashHandler(onCrash: (threadName: String, throwable: Throwable) -> Unit)
