package io.pulse

import io.pulse.model.CrashEntry
import io.pulse.model.HttpTransaction
import io.pulse.log.LogEntry
import io.pulse.log.LogLevel
import io.pulse.model.TransactionStatus

/**
 * Creates a test [HttpTransaction] with sensible defaults that can be overridden.
 */
fun createTestTransaction(
    id: String = "txn-1",
    method: String = "GET",
    url: String = "https://api.example.com/users",
    host: String = "api.example.com",
    path: String = "/users",
    scheme: String = "https",
    requestHeaders: Map<String, String> = emptyMap(),
    requestBody: String? = null,
    requestContentType: String? = null,
    requestSize: Long = 0L,
    responseCode: Int? = 200,
    responseMessage: String? = "OK",
    responseHeaders: Map<String, String> = emptyMap(),
    responseBody: String? = null,
    responseContentType: String? = null,
    responseSize: Long = 0L,
    duration: Long = 150L,
    timestamp: Long = 1_700_000_000_000L,
    error: String? = null,
    status: TransactionStatus = TransactionStatus.Complete,
): HttpTransaction = HttpTransaction(
    id = id,
    method = method,
    url = url,
    host = host,
    path = path,
    scheme = scheme,
    requestHeaders = requestHeaders,
    requestBody = requestBody,
    requestContentType = requestContentType,
    requestSize = requestSize,
    responseCode = responseCode,
    responseMessage = responseMessage,
    responseHeaders = responseHeaders,
    responseBody = responseBody,
    responseContentType = responseContentType,
    responseSize = responseSize,
    duration = duration,
    timestamp = timestamp,
    error = error,
    status = status,
)

/**
 * Creates a test [LogEntry] with sensible defaults.
 */
fun createTestLogEntry(
    id: String = "log-1",
    level: LogLevel = LogLevel.DEBUG,
    tag: String = "TestTag",
    message: String = "Test log message",
    throwable: String? = null,
    timestamp: Long = 1_700_000_000_000L,
): LogEntry = LogEntry(
    id = id,
    level = level,
    tag = tag,
    message = message,
    throwable = throwable,
    timestamp = timestamp,
)

/**
 * Creates a test [CrashEntry] with sensible defaults.
 */
fun createTestCrashEntry(
    id: String = "crash-1",
    threadName: String = "main",
    exceptionClass: String = "NullPointerException",
    message: String = "Something was null",
    stackTrace: String = "java.lang.NullPointerException: Something was null\n\tat com.example.Test.run(Test.kt:10)",
    timestamp: Long = 1_700_000_000_000L,
): CrashEntry = CrashEntry(
    id = id,
    threadName = threadName,
    exceptionClass = exceptionClass,
    message = message,
    stackTrace = stackTrace,
    timestamp = timestamp,
)
