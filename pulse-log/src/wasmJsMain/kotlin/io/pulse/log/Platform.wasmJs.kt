package io.pulse.log

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun dateNow(): Double = js("Date.now()")

internal actual fun epochMillis(): Long = dateNow().toLong()

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val text = "${level.label}/$tag: $message"
    println(text)
    throwable?.printStackTrace()
}

internal actual fun callerTag(): String = "Pulse"
