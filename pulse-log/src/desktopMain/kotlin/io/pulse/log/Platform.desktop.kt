package io.pulse.log

internal actual fun epochMillis(): Long = System.currentTimeMillis()

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val prefix = "${level.label}/$tag"
    println("$prefix: $message")
    throwable?.printStackTrace()
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
        ?: "Pulse"
}
