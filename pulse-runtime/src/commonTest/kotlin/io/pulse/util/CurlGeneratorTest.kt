package io.pulse.util

import io.pulse.createTestTransaction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CurlGeneratorTest {

    // ---------------------------------------------------------------
    // toCurlCommand
    // ---------------------------------------------------------------

    @Test
    fun toCurlCommand_starts_with_curl() {
        val txn = createTestTransaction(method = "GET")
        val curl = txn.toCurlCommand()
        assertTrue(curl.startsWith("curl"), "cURL command should start with 'curl'")
    }

    @Test
    fun toCurlCommand_contains_method() {
        val txn = createTestTransaction(method = "GET")
        val curl = txn.toCurlCommand()
        assertTrue(curl.contains("-X GET"), "Should contain -X GET")
    }

    @Test
    fun toCurlCommand_contains_url() {
        val txn = createTestTransaction(url = "https://api.example.com/users")
        val curl = txn.toCurlCommand()
        assertTrue(curl.contains("https://api.example.com/users"), "Should contain the full URL")
    }

    @Test
    fun toCurlCommand_includes_request_headers() {
        val txn = createTestTransaction(
            requestHeaders = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer token123",
            ),
        )
        val curl = txn.toCurlCommand()
        assertTrue(curl.contains("-H 'Content-Type: application/json'"), "Should include Content-Type header")
        assertTrue(curl.contains("-H 'Authorization: Bearer token123'"), "Should include Authorization header")
    }

    @Test
    fun toCurlCommand_includes_body_for_post() {
        val body = """{"name":"Alice"}"""
        val txn = createTestTransaction(method = "POST", requestBody = body)
        val curl = txn.toCurlCommand()
        assertTrue(curl.contains("-X POST"), "Should contain -X POST")
        assertTrue(curl.contains("-d"), "Should contain -d flag for body")
        assertTrue(curl.contains(body), "Should contain the body content")
    }

    @Test
    fun toCurlCommand_omits_body_flag_when_body_is_null() {
        val txn = createTestTransaction(method = "GET", requestBody = null)
        val curl = txn.toCurlCommand()
        assertFalse(curl.contains("-d"), "GET without body should not contain -d flag")
    }

    @Test
    fun toCurlCommand_omits_body_flag_when_body_is_blank() {
        val txn = createTestTransaction(method = "POST", requestBody = "   ")
        val curl = txn.toCurlCommand()
        assertFalse(curl.contains("-d"), "Blank body should not produce -d flag")
    }

    @Test
    fun toCurlCommand_escapes_single_quotes_in_body() {
        val body = """{"msg":"it's a test"}"""
        val txn = createTestTransaction(method = "POST", requestBody = body)
        val curl = txn.toCurlCommand()
        // The single quote inside should be escaped as '\''
        assertFalse(
            curl.contains("it's a test"),
            "Raw single quote should be escaped in the cURL body",
        )
        assertTrue(
            curl.contains("it'\\''s a test"),
            "Single quotes should be escaped as '\\'' in cURL",
        )
    }

    @Test
    fun toCurlCommand_handles_put_method() {
        val txn = createTestTransaction(method = "PUT", requestBody = """{"id":1}""")
        val curl = txn.toCurlCommand()
        assertTrue(curl.contains("-X PUT"), "Should contain -X PUT")
    }

    @Test
    fun toCurlCommand_handles_delete_method() {
        val txn = createTestTransaction(method = "DELETE")
        val curl = txn.toCurlCommand()
        assertTrue(curl.contains("-X DELETE"), "Should contain -X DELETE")
    }

    // ---------------------------------------------------------------
    // toShareText
    // ---------------------------------------------------------------

    @Test
    fun toShareText_contains_method_and_url() {
        val txn = createTestTransaction(method = "GET", url = "https://api.example.com/users")
        val text = txn.toShareText()
        assertTrue(text.contains("GET https://api.example.com/users"), "Share text should start with method and URL")
    }

    @Test
    fun toShareText_contains_status_info() {
        val txn = createTestTransaction(responseCode = 200, responseMessage = "OK")
        val text = txn.toShareText()
        assertTrue(text.contains("200"), "Share text should contain the response code")
    }

    @Test
    fun toShareText_contains_duration() {
        val txn = createTestTransaction(duration = 500L)
        val text = txn.toShareText()
        assertTrue(text.contains("Duration"), "Share text should contain duration label")
    }

    @Test
    fun toShareText_does_not_contain_branding() {
        val txn = createTestTransaction()
        val text = txn.toShareText()
        assertFalse(
            text.contains("Pulse", ignoreCase = true),
            "Share text should not contain Pulse branding",
        )
    }

    @Test
    fun toShareText_includes_request_headers() {
        val txn = createTestTransaction(
            requestHeaders = mapOf("Accept" to "application/json"),
        )
        val text = txn.toShareText()
        assertTrue(text.contains("Accept"), "Share text should contain request header key")
        assertTrue(text.contains("application/json"), "Share text should contain request header value")
    }

    @Test
    fun toShareText_includes_request_body_when_present() {
        val body = """{"data":"test"}"""
        val txn = createTestTransaction(requestBody = body)
        val text = txn.toShareText()
        assertTrue(text.contains(body), "Share text should contain the request body")
    }

    @Test
    fun toShareText_includes_response_body_when_present() {
        val body = """{"result":"ok"}"""
        val txn = createTestTransaction(responseBody = body)
        val text = txn.toShareText()
        assertTrue(text.contains(body), "Share text should contain the response body")
    }

    @Test
    fun toShareText_contains_response_section() {
        val txn = createTestTransaction()
        val text = txn.toShareText()
        assertTrue(text.contains("--- Response ---"), "Share text should contain Response section header")
    }

    @Test
    fun toShareText_contains_request_section() {
        val txn = createTestTransaction()
        val text = txn.toShareText()
        assertTrue(text.contains("--- Request ---"), "Share text should contain Request section header")
    }
}
