package io.pulse.internal

/**
 * Manages sensitive data redaction and security policies for Pulse.
 *
 * By default, redaction is disabled. Enable it via [redactSensitiveHeaders] to mask
 * values of common authentication and session headers in the Pulse UI.
 */
object SecurityManager {

    /**
     * Header names (stored lowercase) whose values should be redacted when displayed.
     */
    val sensitiveHeaders: MutableSet<String> = mutableSetOf(
        "authorization",
        "cookie",
        "set-cookie",
        "x-api-key",
        "x-auth-token",
        "proxy-authorization",
        "www-authenticate",
    )

    /**
     * Auto-clear data after this duration in milliseconds. `0` means never.
     */
    var dataRetentionMs: Long = 0L

    /**
     * When `true`, values of headers listed in [sensitiveHeaders] are replaced
     * with a redacted placeholder in the UI.
     */
    var redactSensitiveHeaders: Boolean = false

    private const val REDACTED_PLACEHOLDER = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"

    /**
     * Returns the redacted form of a header value if [redactSensitiveHeaders] is enabled
     * and the [key] matches a known sensitive header; otherwise returns [value] as-is.
     */
    fun redactHeaderValue(key: String, value: String): String {
        if (!redactSensitiveHeaders) return value
        if (key.lowercase() in sensitiveHeaders) return REDACTED_PLACEHOLDER
        return value
    }

    /**
     * Registers an additional header name as sensitive so its value will be redacted.
     */
    fun addSensitiveHeader(header: String) {
        sensitiveHeaders.add(header.lowercase())
    }

    /**
     * Removes a header name from the sensitive set.
     */
    fun removeSensitiveHeader(header: String) {
        sensitiveHeaders.remove(header.lowercase())
    }
}
