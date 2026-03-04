package io.pulse.util

import io.pulse.model.HttpTransaction

internal fun HttpTransaction.toCurlCommand(): String = buildString {
    append("curl -X $method")

    requestHeaders.forEach { (key, value) ->
        append(" \\\n  -H '${key}: ${value}'")
    }

    if (requestBody != null && requestBody.isNotBlank()) {
        val escapedBody = requestBody.replace("'", "'\\''")
        append(" \\\n  -d '$escapedBody'")
    }

    append(" \\\n  '${url}'")
}

internal fun HttpTransaction.toShareText(): String = buildString {
    appendLine("--- Request ---")
    appendLine("$method $url")
    appendLine("Status: $responseSummary")
    appendLine("Duration: ${formatDuration(duration)}")
    appendLine("Time: ${formatTimestamp(timestamp)}")
    appendLine()

    if (requestHeaders.isNotEmpty()) {
        appendLine("Request Headers:")
        requestHeaders.forEach { (key, value) ->
            appendLine("  $key: $value")
        }
        appendLine()
    }

    if (!requestBody.isNullOrBlank()) {
        appendLine("Request Body:")
        appendLine(requestBody)
        appendLine()
    }

    appendLine("--- Response ---")
    appendLine("Status: ${responseCode ?: "N/A"} ${responseMessage ?: ""}")
    appendLine("Size: ${formatBytes(responseSize)}")
    appendLine()

    if (responseHeaders.isNotEmpty()) {
        appendLine("Response Headers:")
        responseHeaders.forEach { (key, value) ->
            appendLine("  $key: $value")
        }
        appendLine()
    }

    if (!responseBody.isNullOrBlank()) {
        appendLine("Response Body:")
        appendLine(responseBody)
    }
}
