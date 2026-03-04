package io.pulse.log

data class LogEntry(
    val id: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: String? = null,
    val timestamp: Long,
)
