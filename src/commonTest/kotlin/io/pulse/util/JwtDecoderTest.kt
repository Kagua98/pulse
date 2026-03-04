package io.pulse.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtDecoderTest {

    // ---------------------------------------------------------------
    // Helper: build a syntactically valid JWT from raw JSON strings
    // ---------------------------------------------------------------

    @OptIn(ExperimentalEncodingApi::class)
    private fun buildJwt(
        headerJson: String = """{"alg":"HS256","typ":"JWT"}""",
        payloadJson: String = """{"sub":"user123","iss":"auth-server","iat":1700000000,"exp":9999999999}""",
        signature: String = "test-signature",
    ): String {
        val header = Base64.UrlSafe.encode(headerJson.encodeToByteArray()).trimEnd('=')
        val payload = Base64.UrlSafe.encode(payloadJson.encodeToByteArray()).trimEnd('=')
        return "$header.$payload.$signature"
    }

    // ---------------------------------------------------------------
    // isJwtToken
    // ---------------------------------------------------------------

    @Test
    fun isJwtToken_returns_true_for_valid_three_part_base64url_token() {
        val token = buildJwt()
        assertTrue(isJwtToken(token), "Expected a well-formed JWT to be detected as valid")
    }

    @Test
    fun isJwtToken_returns_false_for_plain_text() {
        assertFalse(isJwtToken("hello world"))
    }

    @Test
    fun isJwtToken_returns_false_for_empty_string() {
        assertFalse(isJwtToken(""))
    }

    @Test
    fun isJwtToken_returns_false_for_two_part_string() {
        assertFalse(isJwtToken("abc.def"))
    }

    @Test
    fun isJwtToken_returns_false_when_a_part_is_empty() {
        assertFalse(isJwtToken("abc..def"))
    }

    @Test
    fun isJwtToken_handles_whitespace_around_token() {
        val token = buildJwt()
        assertTrue(isJwtToken("  $token  "), "Should trim whitespace before checking")
    }

    // ---------------------------------------------------------------
    // decodeJwt — happy path
    // ---------------------------------------------------------------

    @Test
    fun decodeJwt_extracts_header_and_payload_from_valid_token() {
        val token = buildJwt()
        val decoded = decodeJwt(token)
        assertNotNull(decoded, "decodeJwt should return non-null for a valid token")
        assertTrue(decoded.header.contains("HS256"), "Header should contain the algorithm")
        assertTrue(decoded.payload.contains("user123"), "Payload should contain the subject")
    }

    @Test
    fun decodeJwt_extracts_iss_claim() {
        val token = buildJwt()
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertEquals("auth-server", decoded.issuer)
    }

    @Test
    fun decodeJwt_extracts_sub_claim() {
        val token = buildJwt()
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertEquals("user123", decoded.subject)
    }

    @Test
    fun decodeJwt_extracts_iat_claim() {
        val token = buildJwt()
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertEquals(1_700_000_000L, decoded.issuedAt)
    }

    @Test
    fun decodeJwt_extracts_exp_claim() {
        val token = buildJwt()
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertEquals(9_999_999_999L, decoded.expiresAt)
    }

    @Test
    fun decodeJwt_preserves_signature_segment() {
        val token = buildJwt(signature = "my-sig-abc")
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertEquals("my-sig-abc", decoded.signature)
    }

    // ---------------------------------------------------------------
    // decodeJwt — expiration
    // ---------------------------------------------------------------

    @Test
    fun decodeJwt_detects_non_expired_token_with_future_exp() {
        val token = buildJwt(payloadJson = """{"exp":9999999999}""")
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertFalse(decoded.isExpired, "Token with far-future exp should not be expired")
    }

    @Test
    fun decodeJwt_detects_expired_token_with_past_exp() {
        // exp = 1 second after epoch => long expired
        val token = buildJwt(payloadJson = """{"exp":1}""")
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertTrue(decoded.isExpired, "Token with exp=1 should be expired")
    }

    @Test
    fun decodeJwt_not_expired_when_exp_is_absent() {
        val token = buildJwt(payloadJson = """{"sub":"no-exp"}""")
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertFalse(decoded.isExpired, "Token without exp should not be considered expired")
        assertNull(decoded.expiresAt)
    }

    // ---------------------------------------------------------------
    // decodeJwt — missing optional claims
    // ---------------------------------------------------------------

    @Test
    fun decodeJwt_returns_null_claims_when_absent() {
        val token = buildJwt(payloadJson = """{"custom":"value"}""")
        val decoded = decodeJwt(token)
        assertNotNull(decoded)
        assertNull(decoded.issuer)
        assertNull(decoded.subject)
        assertNull(decoded.issuedAt)
        assertNull(decoded.expiresAt)
    }

    // ---------------------------------------------------------------
    // decodeJwt — invalid tokens
    // ---------------------------------------------------------------

    @Test
    fun decodeJwt_returns_null_for_empty_string() {
        assertNull(decodeJwt(""))
    }

    @Test
    fun decodeJwt_returns_null_for_plain_text() {
        assertNull(decodeJwt("not-a-jwt"))
    }

    @Test
    fun decodeJwt_returns_null_for_malformed_base64() {
        assertNull(decodeJwt("!!!.@@@.###"))
    }

    // ---------------------------------------------------------------
    // findJwtTokens
    // ---------------------------------------------------------------

    @Test
    fun findJwtTokens_finds_single_token_in_text() {
        val token = buildJwt()
        val text = "Bearer $token"
        val results = findJwtTokens(text)
        assertEquals(1, results.size, "Should find exactly one JWT")
        assertEquals(token, results[0].second)
    }

    @Test
    fun findJwtTokens_finds_multiple_tokens_in_text() {
        val token1 = buildJwt(payloadJson = """{"sub":"user1","iat":1000}""")
        val token2 = buildJwt(payloadJson = """{"sub":"user2","iat":2000}""")
        val text = "First: $token1 Second: $token2"
        val results = findJwtTokens(text)
        assertEquals(2, results.size, "Should find two JWTs")
    }

    @Test
    fun findJwtTokens_returns_empty_list_when_no_tokens_present() {
        val results = findJwtTokens("No tokens here.")
        assertTrue(results.isEmpty())
    }

    @Test
    fun findJwtTokens_returns_correct_ranges() {
        val token = buildJwt()
        val prefix = "Authorization: Bearer "
        val text = "$prefix$token"
        val results = findJwtTokens(text)
        assertEquals(1, results.size)
        val (range, foundToken) = results[0]
        assertEquals(prefix.length, range.first)
        assertEquals(token, foundToken)
    }
}
