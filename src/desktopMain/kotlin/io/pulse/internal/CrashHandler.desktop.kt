package io.pulse.internal

internal actual fun installCrashHandler(onCrash: (threadName: String, throwable: Throwable) -> Unit) {
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        onCrash(thread.name, throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
