package io.pulse.model

data class HttpTransaction(
    val id: String,
    val method: String,
    val url: String,
    val host: String,
    val path: String,
    val scheme: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val requestContentType: String? = null,
    val requestSize: Long = 0L,
    val responseCode: Int? = null,
    val responseMessage: String? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val responseContentType: String? = null,
    val responseSize: Long = 0L,
    val duration: Long = 0L,
    val timestamp: Long = 0L,
    val error: String? = null,
    val status: TransactionStatus = TransactionStatus.Requested,
) {
    val isSuccess: Boolean get() = responseCode in 200..299
    val isRedirect: Boolean get() = responseCode in 300..399
    val isClientError: Boolean get() = responseCode in 400..499
    val isServerError: Boolean get() = responseCode in 500..599
    val isFailed: Boolean get() = status == TransactionStatus.Failed

    val responseSummary: String
        get() = when {
            status == TransactionStatus.Failed -> error ?: "Failed"
            status == TransactionStatus.Requested -> "Pending..."
            responseCode != null -> "$responseCode $responseMessage"
            else -> "Unknown"
        }
}

enum class TransactionStatus {
    Requested,
    Complete,
    Failed,
}
