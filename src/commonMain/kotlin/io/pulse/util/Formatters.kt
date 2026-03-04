package io.pulse.util

internal fun formatBytes(bytes: Long): String = when {
    bytes < 0 -> "0 B"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${(bytes * 10 / 1024).toFloat() / 10} KB"
    bytes < 1024 * 1024 * 1024 -> "${(bytes * 10 / (1024 * 1024)).toFloat() / 10} MB"
    else -> "${(bytes * 100 / (1024L * 1024 * 1024)).toFloat() / 100} GB"
}

internal fun formatDuration(ms: Long): String = when {
    ms < 0 -> "0 ms"
    ms < 1000 -> "$ms ms"
    ms < 60_000 -> "${(ms / 100).toFloat() / 10}s"
    else -> {
        val minutes = ms / 60_000
        val seconds = (ms % 60_000) / 1000
        "${minutes}m ${seconds}s"
    }
}

internal fun formatTimestamp(epochMs: Long): String {
    val totalSeconds = epochMs / 1000
    val hours = (totalSeconds / 3600) % 24
    val minutes = (totalSeconds / 60) % 60
    val seconds = totalSeconds % 60
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

private fun Long.pad(): String = toString().padStart(2, '0')

internal fun prettyPrintJson(json: String): String {
    if (json.isBlank()) return json
    val firstChar = json.trimStart().firstOrNull()
    if (firstChar != '{' && firstChar != '[') return json

    return try {
        buildString {
            var indent = 0
            var inString = false
            var escaped = false

            for (char in json) {
                when {
                    escaped -> {
                        append(char)
                        escaped = false
                    }
                    char == '\\' && inString -> {
                        append(char)
                        escaped = true
                    }
                    char == '"' -> {
                        inString = !inString
                        append(char)
                    }
                    inString -> append(char)
                    char == '{' || char == '[' -> {
                        append(char)
                        indent++
                        appendLine()
                        appendIndent(indent)
                    }
                    char == '}' || char == ']' -> {
                        indent--
                        appendLine()
                        appendIndent(indent)
                        append(char)
                    }
                    char == ',' -> {
                        append(char)
                        appendLine()
                        appendIndent(indent)
                    }
                    char == ':' -> append(": ")
                    !char.isWhitespace() -> append(char)
                }
            }
        }
    } catch (_: Exception) {
        json
    }
}

private fun StringBuilder.appendIndent(level: Int) {
    repeat(level) { append("  ") }
}
