package io.pulse.model

data class CrashEntry(
    val id: String,
    val threadName: String,
    val exceptionClass: String,
    val message: String,
    val stackTrace: String,
    val timestamp: Long,
)
