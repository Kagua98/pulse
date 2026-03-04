package io.pulse.log

internal actual fun epochMillis(): Long = System.currentTimeMillis()

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val prefix = "${level.label}/$tag"
    println("$prefix: $message")
    throwable?.printStackTrace()
}
