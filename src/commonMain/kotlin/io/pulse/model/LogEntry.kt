package io.pulse.model

data class LogEntry(
    val id: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: String? = null,
    val timestamp: Long,
)

enum class LogLevel(val label: String) {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARN("W"),
    ERROR("E"),
}
