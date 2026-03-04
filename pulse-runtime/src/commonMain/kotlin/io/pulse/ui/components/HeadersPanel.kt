package io.pulse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.internal.SecurityManager
import io.pulse.ui.theme.PulseColors
import io.pulse.util.decodeJwt
import io.pulse.util.isJwtToken

private const val COLLAPSE_THRESHOLD = 5
private const val COLLAPSED_VISIBLE_COUNT = 3

private val TOKEN_HEADER_NAMES = setOf(
    "service_token",
    "access_token",
    "access token",
)

@Composable
internal fun HeadersPanel(
    headers: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    if (headers.isEmpty()) {
        Text(
            text = "No headers",
            color = PulseColors.onSurfaceDim,
            fontSize = 13.sp,
            modifier = modifier.padding(16.dp),
        )
        return
    }

    val headerEntries = remember(headers) { headers.entries.toList() }
    val shouldCollapse = headerEntries.size > COLLAPSE_THRESHOLD
    var expanded by remember(headers) { mutableStateOf(!shouldCollapse) }

    val visibleHeaders = if (expanded) {
        headerEntries
    } else {
        headerEntries.take(COLLAPSED_VISIBLE_COUNT)
    }
    val hiddenCount = headerEntries.size - COLLAPSED_VISIBLE_COUNT

    SelectionContainer {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(
                    color = PulseColors.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(12.dp),
        ) {
            // Always-visible headers
            visibleHeaders.forEach { (key, value) ->
                HeaderRow(key = key, value = value)
            }

            // Animated expansion for remaining headers
            if (shouldCollapse) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        headerEntries.drop(COLLAPSED_VISIBLE_COUNT).forEach { (key, value) ->
                            HeaderRow(key = key, value = value)
                        }
                    }
                }

                // Toggle button
                Text(
                    text = if (expanded) {
                        "Show less"
                    } else {
                        "Show $hiddenCount more..."
                    },
                    color = PulseColors.redirect,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}

/**
 * Extracts the raw JWT token string from a header if it looks like a JWT.
 * Checks:
 * 1. Authorization header with "Bearer <jwt>"
 * 2. Known token header names (service_token, access_token, etc.)
 * 3. Any header whose value is a valid JWT format
 */
private fun extractTokenValue(key: String, value: String): String? {
    val lowerKey = key.lowercase()

    // Authorization: Bearer <token>
    if (lowerKey == "authorization" && value.startsWith("Bearer ", ignoreCase = true)) {
        val token = value.substringAfter(" ").trim()
        if (isJwtToken(token)) return token
    }

    // Known token header names
    if (lowerKey in TOKEN_HEADER_NAMES) {
        val candidate = value.trim()
        if (isJwtToken(candidate)) return candidate
    }

    // Any header whose raw value is a JWT
    if (isJwtToken(value.trim())) return value.trim()

    return null
}

@Composable
private fun HeaderRow(key: String, value: String) {
    val displayValue = SecurityManager.redactHeaderValue(key, value)
    val isRedacted = displayValue != value

    // JWT detection — skip if value is redacted
    val tokenValue = remember(key, value, isRedacted) {
        if (isRedacted) null else extractTokenValue(key, value)
    }
    val decodedJwt = remember(tokenValue) {
        tokenValue?.let { decodeJwt(it) }
    }
    var jwtExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "$key:",
                color = PulseColors.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayValue,
                    color = if (isRedacted) PulseColors.onSurfaceDim.copy(alpha = 0.5f)
                    else PulseColors.onSurfaceDim,
                    fontSize = 12.sp,
                    fontStyle = if (isRedacted) FontStyle.Italic else FontStyle.Normal,
                )
                if (isRedacted) {
                    Text(
                        text = "[REDACTED]",
                        color = PulseColors.onSurfaceDim.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (decodedJwt != null) {
                Text(
                    text = if (jwtExpanded) "Hide JWT" else "Decode JWT",
                    color = PulseColors.redirect,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { jwtExpanded = !jwtExpanded }
                        .padding(start = 8.dp, top = 1.dp),
                )
            }
        }

        // Inline JWT decode card
        if (decodedJwt != null) {
            AnimatedVisibility(
                visible = jwtExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                JwtCard(
                    label = key,
                    tokenPreview = (tokenValue ?: "").take(50) +
                        if ((tokenValue?.length ?: 0) > 50) "..." else "",
                    decoded = decodedJwt,
                )
            }
        }
    }
}
