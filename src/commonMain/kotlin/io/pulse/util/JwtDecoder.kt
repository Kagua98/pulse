package io.pulse.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal data class DecodedJwt(
    val header: String,
    val payload: String,
    val signature: String,
    val isExpired: Boolean,
    val expiresAt: Long?,
    val issuedAt: Long?,
    val issuer: String?,
    val subject: String?,
)

private val JWT_PATTERN = Regex("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+")

internal fun isJwtToken(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed.matches(Regex("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")) &&
        trimmed.split(".").let { parts ->
            parts.size == 3 && parts.all { it.isNotEmpty() }
        }
}

@OptIn(ExperimentalEncodingApi::class)
internal fun decodeJwt(token: String): DecodedJwt? {
    val trimmed = token.trim()
    if (!isJwtToken(trimmed)) return null

    val parts = trimmed.split(".")
    if (parts.size != 3) return null

    return try {
        val headerJson = base64UrlDecode(parts[0]) ?: return null
        val payloadJson = base64UrlDecode(parts[1]) ?: return null
        val signature = parts[2]

        val prettyHeader = prettyPrintJson(headerJson)
        val prettyPayload = prettyPrintJson(payloadJson)

        val expiresAt = extractNumericClaim(payloadJson, "exp")
        val issuedAt = extractNumericClaim(payloadJson, "iat")
        val issuer = extractStringClaim(payloadJson, "iss")
        val subject = extractStringClaim(payloadJson, "sub")

        val isExpired = expiresAt?.let { exp ->
            val nowSeconds = io.pulse.internal.epochMillis() / 1000
            nowSeconds > exp
        } ?: false

        DecodedJwt(
            header = prettyHeader,
            payload = prettyPayload,
            signature = signature,
            isExpired = isExpired,
            expiresAt = expiresAt,
            issuedAt = issuedAt,
            issuer = issuer,
            subject = subject,
        )
    } catch (_: Exception) {
        null
    }
}

internal fun findJwtTokens(text: String): List<Pair<IntRange, String>> {
    return JWT_PATTERN.findAll(text)
        .filter { matchResult ->
            val token = matchResult.value
            val parts = token.split(".")
            parts.size == 3 &&
                parts.all { it.isNotEmpty() } &&
                isLikelyJwt(token)
        }
        .map { matchResult ->
            matchResult.range to matchResult.value
        }
        .toList()
}

@OptIn(ExperimentalEncodingApi::class)
private fun base64UrlDecode(encoded: String): String? {
    return try {
        // Add padding if necessary
        val padded = when (encoded.length % 4) {
            2 -> "$encoded=="
            3 -> "$encoded="
            else -> encoded
        }
        val bytes = Base64.UrlSafe.decode(padded)
        bytes.decodeToString()
    } catch (_: Exception) {
        null
    }
}

/**
 * Quick check that the first part decodes to something JSON-like (header).
 */
private fun isLikelyJwt(token: String): Boolean {
    return try {
        val headerPart = token.split(".")[0]
        val decoded = base64UrlDecode(headerPart) ?: return false
        val trimmed = decoded.trim()
        trimmed.startsWith("{") && trimmed.endsWith("}")
    } catch (_: Exception) {
        false
    }
}

/**
 * Extract a numeric claim from a simple JSON string without a JSON parser.
 * Handles: "claimName": 1234567890 or "claimName":1234567890
 */
private fun extractNumericClaim(json: String, claim: String): Long? {
    val pattern = Regex("\"$claim\"\\s*:\\s*(\\d+)")
    return pattern.find(json)?.groupValues?.get(1)?.toLongOrNull()
}

/**
 * Extract a string claim from a simple JSON string without a JSON parser.
 * Handles: "claimName": "value" or "claimName":"value"
 */
private fun extractStringClaim(json: String, claim: String): String? {
    val pattern = Regex("\"$claim\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"")
    return pattern.find(json)?.groupValues?.get(1)
}
