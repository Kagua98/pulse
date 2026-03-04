package io.pulse.util

import io.pulse.model.HttpTransaction

internal fun exportAsSingleText(transaction: HttpTransaction): String = buildString {
    // General
    appendLine("--- General ---")
    appendLine("URL:      ${transaction.url}")
    appendLine("Method:   ${transaction.method}")
    appendLine("Scheme:   ${transaction.scheme}")
    appendLine("Host:     ${transaction.host}")
    appendLine("Path:     ${transaction.path}")
    appendLine("Time:     ${formatTimestamp(transaction.timestamp)}")
    appendLine()

    // Status
    appendLine("--- Status ---")
    appendLine("Code:     ${transaction.responseCode ?: "N/A"}")
    appendLine("Message:  ${transaction.responseMessage ?: "N/A"}")
    appendLine("Duration: ${formatDuration(transaction.duration)}")
    appendLine("Status:   ${transaction.status.name}")
    appendLine()

    // Request
    appendLine("--- Request ---")
    appendLine("Content-Type: ${transaction.requestContentType ?: "N/A"}")
    appendLine("Size:         ${formatBytes(transaction.requestSize)}")
    appendLine()

    if (transaction.requestHeaders.isNotEmpty()) {
        appendLine("Request Headers:")
        transaction.requestHeaders.forEach { (key, value) ->
            appendLine("  $key: $value")
        }
        appendLine()
    }

    if (!transaction.requestBody.isNullOrBlank()) {
        appendLine("Request Body:")
        appendLine(transaction.requestBody)
        appendLine()
    }

    // Response
    appendLine("--- Response ---")
    appendLine("Content-Type: ${transaction.responseContentType ?: "N/A"}")
    appendLine("Size:         ${formatBytes(transaction.responseSize)}")
    appendLine()

    if (transaction.responseHeaders.isNotEmpty()) {
        appendLine("Response Headers:")
        transaction.responseHeaders.forEach { (key, value) ->
            appendLine("  $key: $value")
        }
        appendLine()
    }

    if (!transaction.responseBody.isNullOrBlank()) {
        appendLine("Response Body:")
        appendLine(transaction.responseBody)
        appendLine()
    }

    // Error
    if (transaction.error != null) {
        appendLine("--- Error ---")
        appendLine(transaction.error)
        appendLine()
    }

}

internal fun exportAsText(transactions: List<HttpTransaction>): String = buildString {

    transactions.forEachIndexed { index, transaction ->
        appendLine("[ ${index + 1} / ${transactions.size} ]")
        append(exportAsSingleText(transaction))
        appendLine()
    }
}
