package io.pulse.internal

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SecurityManagerTest {

    /**
     * Reset SecurityManager to a clean state after each test to avoid cross-test pollution.
     */
    @AfterTest
    fun tearDown() {
        SecurityManager.redactSensitiveHeaders = false
        // Restore default sensitive headers
        SecurityManager.sensitiveHeaders.clear()
        SecurityManager.sensitiveHeaders.addAll(
            listOf(
                "authorization",
                "cookie",
                "set-cookie",
                "x-api-key",
                "x-auth-token",
                "proxy-authorization",
                "www-authenticate",
            ),
        )
    }

    // ---------------------------------------------------------------
    // Redaction disabled (default)
    // ---------------------------------------------------------------

    @Test
    fun redactHeaderValue_returns_original_value_when_redaction_disabled() {
        SecurityManager.redactSensitiveHeaders = false
        val result = SecurityManager.redactHeaderValue("Authorization", "Bearer token123")
        assertEquals("Bearer token123", result, "Should return original value when redaction is off")
    }

    @Test
    fun redactHeaderValue_returns_original_for_non_sensitive_header_when_disabled() {
        SecurityManager.redactSensitiveHeaders = false
        val result = SecurityManager.redactHeaderValue("Content-Type", "application/json")
        assertEquals("application/json", result)
    }

    // ---------------------------------------------------------------
    // Redaction enabled
    // ---------------------------------------------------------------

    @Test
    fun redactHeaderValue_redacts_authorization_header_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("Authorization", "Bearer secret-token")
        assertNotEquals("Bearer secret-token", result, "Sensitive header should be redacted")
    }

    @Test
    fun redactHeaderValue_redacts_cookie_header_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("Cookie", "session=abc123")
        assertNotEquals("session=abc123", result, "Cookie header should be redacted")
    }

    @Test
    fun redactHeaderValue_redacts_set_cookie_header_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("Set-Cookie", "session=abc; Path=/")
        assertNotEquals("session=abc; Path=/", result)
    }

    @Test
    fun redactHeaderValue_redacts_x_api_key_header_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("X-Api-Key", "my-api-key-123")
        assertNotEquals("my-api-key-123", result)
    }

    @Test
    fun redactHeaderValue_redacts_x_auth_token_header_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("X-Auth-Token", "tok-999")
        assertNotEquals("tok-999", result)
    }

    @Test
    fun redactHeaderValue_redacts_proxy_authorization_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("Proxy-Authorization", "Basic abc")
        assertNotEquals("Basic abc", result)
    }

    @Test
    fun redactHeaderValue_redacts_www_authenticate_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("WWW-Authenticate", "Bearer realm=api")
        assertNotEquals("Bearer realm=api", result)
    }

    @Test
    fun redactHeaderValue_does_not_redact_non_sensitive_header_when_enabled() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("Content-Type", "application/json")
        assertEquals("application/json", result, "Non-sensitive headers should not be redacted")
    }

    @Test
    fun redactHeaderValue_does_not_redact_custom_non_sensitive_header() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("X-Request-Id", "req-42")
        assertEquals("req-42", result, "Custom non-sensitive headers should not be redacted")
    }

    // ---------------------------------------------------------------
    // Case-insensitive matching
    // ---------------------------------------------------------------

    @Test
    fun redactHeaderValue_is_case_insensitive_for_authorization() {
        SecurityManager.redactSensitiveHeaders = true
        val result1 = SecurityManager.redactHeaderValue("authorization", "token")
        val result2 = SecurityManager.redactHeaderValue("AUTHORIZATION", "token")
        val result3 = SecurityManager.redactHeaderValue("Authorization", "token")

        assertNotEquals("token", result1, "lowercase should be redacted")
        assertNotEquals("token", result2, "UPPERCASE should be redacted")
        assertNotEquals("token", result3, "Mixed case should be redacted")
    }

    @Test
    fun redactHeaderValue_is_case_insensitive_for_cookie() {
        SecurityManager.redactSensitiveHeaders = true
        val result = SecurityManager.redactHeaderValue("COOKIE", "value")
        assertNotEquals("value", result, "COOKIE in uppercase should be redacted")
    }

    // ---------------------------------------------------------------
    // Custom sensitive headers
    // ---------------------------------------------------------------

    @Test
    fun addSensitiveHeader_makes_custom_header_redactable() {
        SecurityManager.redactSensitiveHeaders = true
        SecurityManager.addSensitiveHeader("X-Custom-Secret")

        val result = SecurityManager.redactHeaderValue("X-Custom-Secret", "secret-value")
        assertNotEquals("secret-value", result, "Custom sensitive header should be redacted")
    }

    @Test
    fun addSensitiveHeader_stores_header_in_lowercase() {
        SecurityManager.addSensitiveHeader("X-MY-HEADER")
        assertTrue(
            SecurityManager.sensitiveHeaders.contains("x-my-header"),
            "Header should be stored in lowercase",
        )
    }

    @Test
    fun removeSensitiveHeader_makes_header_no_longer_redactable() {
        SecurityManager.redactSensitiveHeaders = true
        SecurityManager.removeSensitiveHeader("Authorization")

        val result = SecurityManager.redactHeaderValue("Authorization", "Bearer token")
        assertEquals("Bearer token", result, "Removed header should no longer be redacted")
    }

    @Test
    fun addSensitiveHeader_case_insensitive_redaction_for_custom_header() {
        SecurityManager.redactSensitiveHeaders = true
        SecurityManager.addSensitiveHeader("x-secret")

        val result = SecurityManager.redactHeaderValue("X-Secret", "my-secret")
        assertNotEquals("my-secret", result, "Case-insensitive match should redact custom header")
    }

    // ---------------------------------------------------------------
    // Redacted placeholder consistency
    // ---------------------------------------------------------------

    @Test
    fun redactHeaderValue_returns_consistent_placeholder_for_all_redacted_headers() {
        SecurityManager.redactSensitiveHeaders = true
        val result1 = SecurityManager.redactHeaderValue("Authorization", "token-a")
        val result2 = SecurityManager.redactHeaderValue("Cookie", "session=xyz")
        assertEquals(result1, result2, "All redacted values should use the same placeholder")
    }
}
