package io.pulse.util

import io.pulse.createTestTransaction
import io.pulse.model.TransactionStatus
import kotlin.test.Test
import kotlin.test.assertTrue

class TransactionExporterTest {

    // ---------------------------------------------------------------
    // exportAsSingleText
    // ---------------------------------------------------------------

    @Test
    fun exportAsSingleText_contains_url() {
        val txn = createTestTransaction(url = "https://api.example.com/users")
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("https://api.example.com/users"), "Output should contain the URL")
    }

    @Test
    fun exportAsSingleText_contains_method() {
        val txn = createTestTransaction(method = "POST")
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("POST"), "Output should contain the HTTP method")
    }

    @Test
    fun exportAsSingleText_contains_status_code() {
        val txn = createTestTransaction(responseCode = 201, responseMessage = "Created")
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("201"), "Output should contain the response code")
    }

    @Test
    fun exportAsSingleText_contains_scheme_and_host() {
        val txn = createTestTransaction(scheme = "https", host = "api.example.com")
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("https"), "Output should contain the scheme")
        assertTrue(result.contains("api.example.com"), "Output should contain the host")
    }

    @Test
    fun exportAsSingleText_contains_request_headers() {
        val txn = createTestTransaction(
            requestHeaders = mapOf("Content-Type" to "application/json", "Accept" to "text/html"),
        )
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("Content-Type"), "Output should contain request header key")
        assertTrue(result.contains("application/json"), "Output should contain request header value")
        assertTrue(result.contains("Accept"), "Output should contain all request headers")
    }

    @Test
    fun exportAsSingleText_contains_request_body() {
        val body = """{"name":"Alice"}"""
        val txn = createTestTransaction(requestBody = body)
        val result = exportAsSingleText(txn)
        assertTrue(result.contains(body), "Output should contain the request body")
    }

    @Test
    fun exportAsSingleText_contains_response_headers() {
        val txn = createTestTransaction(
            responseHeaders = mapOf("X-Request-Id" to "abc-123"),
        )
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("X-Request-Id"), "Output should contain response header key")
        assertTrue(result.contains("abc-123"), "Output should contain response header value")
    }

    @Test
    fun exportAsSingleText_contains_response_body() {
        val body = """{"id":1,"name":"Bob"}"""
        val txn = createTestTransaction(responseBody = body)
        val result = exportAsSingleText(txn)
        assertTrue(result.contains(body), "Output should contain the response body")
    }

    @Test
    fun exportAsSingleText_contains_error_section_when_error_present() {
        val txn = createTestTransaction(error = "Connection timeout")
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("Error"), "Output should contain Error section header")
        assertTrue(result.contains("Connection timeout"), "Output should contain the error message")
    }

    @Test
    fun exportAsSingleText_omits_error_section_when_no_error() {
        val txn = createTestTransaction(error = null)
        val result = exportAsSingleText(txn)
        // The "--- Error ---" section should not appear
        assertTrue(!result.contains("--- Error ---"), "Output should not contain Error section when no error")
    }

    @Test
    fun exportAsSingleText_contains_duration() {
        val txn = createTestTransaction(duration = 250L)
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("Duration"), "Output should have a Duration label")
    }

    @Test
    fun exportAsSingleText_handles_null_response_code() {
        val txn = createTestTransaction(responseCode = null, responseMessage = null)
        val result = exportAsSingleText(txn)
        assertTrue(result.contains("N/A"), "Null response code should display as N/A")
    }

    // ---------------------------------------------------------------
    // exportAsText (multiple transactions)
    // ---------------------------------------------------------------

    @Test
    fun exportAsText_handles_multiple_transactions() {
        val txn1 = createTestTransaction(id = "txn-1", url = "https://api.example.com/users")
        val txn2 = createTestTransaction(id = "txn-2", url = "https://api.example.com/posts")
        val result = exportAsText(listOf(txn1, txn2))
        assertTrue(result.contains("[ 1 / 2 ]"), "Should contain index label for first transaction")
        assertTrue(result.contains("[ 2 / 2 ]"), "Should contain index label for second transaction")
        assertTrue(result.contains("/users"), "Should contain first transaction URL")
        assertTrue(result.contains("/posts"), "Should contain second transaction URL")
    }

    @Test
    fun exportAsText_handles_empty_list() {
        val result = exportAsText(emptyList())
        assertTrue(result.isEmpty() || result.isBlank(), "Empty list should produce empty or blank output")
    }

    @Test
    fun exportAsText_single_transaction_has_correct_index() {
        val txn = createTestTransaction()
        val result = exportAsText(listOf(txn))
        assertTrue(result.contains("[ 1 / 1 ]"), "Single transaction should have index [ 1 / 1 ]")
    }
}
