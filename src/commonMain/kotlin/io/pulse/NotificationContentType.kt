package io.pulse

/**
 * Controls what content the persistent notification displays when using
 * [PulseAccessMode.Notification].
 */
enum class NotificationContentType(val label: String, val description: String) {
    /** Real-time HTTP request counts and status breakdown (default, Chucker-style). */
    NetworkActivity("Network Activity", "Request counts and status codes"),

    /** Log level summary with recent entries. */
    LogSummary("Log Summary", "Log counts by severity level"),

    /** Combined health dashboard: crashes, errors, failed requests. */
    AppHealth("App Health", "Crashes, errors, and failed requests"),
}
