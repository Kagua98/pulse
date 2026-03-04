package io.pulse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.ui.theme.PulseColors
import io.pulse.util.DecodedJwt
import io.pulse.util.decodeJwt
import io.pulse.util.findJwtTokens
import io.pulse.util.prettyPrintJson

private const val COLLAPSE_CHAR_THRESHOLD = 500

@Composable
internal fun BodyViewer(
    body: String?,
    contentType: String?,
    modifier: Modifier = Modifier,
    authorizationHeader: String? = null,
) {
    if (body.isNullOrBlank()) {
        Text(
            text = "No body",
            color = PulseColors.onSurfaceDim,
            fontSize = 13.sp,
            modifier = modifier.padding(16.dp),
        )
        return
    }

    val formatted = remember(body, contentType) {
        val isJson = contentType?.contains("json", ignoreCase = true) == true ||
            body.trimStart().let { it.startsWith("{") || it.startsWith("[") }
        if (isJson) prettyPrintJson(body) else body
    }

    val shouldCollapse = formatted.length > COLLAPSE_CHAR_THRESHOLD
    var bodyExpanded by remember(body) { mutableStateOf(!shouldCollapse) }

    Column(modifier = modifier) {
        // Raw body display
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .background(
                        color = PulseColors.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState()),
            ) {
                Column {
                    val displayText = if (bodyExpanded || !shouldCollapse) {
                        formatted
                    } else {
                        formatted.take(COLLAPSE_CHAR_THRESHOLD)
                    }

                    Text(
                        text = displayText,
                        color = PulseColors.onSurface,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                    )

                    if (shouldCollapse && !bodyExpanded) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "...truncated",
                            color = PulseColors.onSurfaceDim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }

        // Collapse/expand toggle
        if (shouldCollapse) {
            Text(
                text = if (bodyExpanded) {
                    "Collapse body"
                } else {
                    "Show full body (${formatted.length} chars)"
                },
                color = PulseColors.redirect,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { bodyExpanded = !bodyExpanded }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        // JWT detection in body
        val bodyJwts = remember(body) { findJwtTokens(body) }

        // JWT detection in Authorization header
        val bearerJwt = remember(authorizationHeader) {
            if (authorizationHeader != null &&
                authorizationHeader.startsWith("Bearer ", ignoreCase = true)
            ) {
                val token = authorizationHeader.removePrefix("Bearer ")
                    .removePrefix("bearer ")
                    .trim()
                decodeJwt(token)?.let { token to it }
            } else {
                null
            }
        }

        val hasJwts = bodyJwts.isNotEmpty() || bearerJwt != null

        if (hasJwts) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Decoded Tokens",
                color = PulseColors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )

            // Bearer token from Authorization header
            if (bearerJwt != null) {
                JwtCard(
                    label = "Authorization: Bearer",
                    tokenPreview = bearerJwt.first.take(50) + if (bearerJwt.first.length > 50) "..." else "",
                    decoded = bearerJwt.second,
                )
            }

            // Tokens found in body
            bodyJwts.forEachIndexed { index, (_, token) ->
                val decoded = remember(token) { decodeJwt(token) }
                if (decoded != null) {
                    JwtCard(
                        label = if (bodyJwts.size == 1) "Body Token" else "Body Token ${index + 1}",
                        tokenPreview = token.take(50) + if (token.length > 50) "..." else "",
                        decoded = decoded,
                    )
                }
            }
        }
    }
}

@Composable
private fun JwtCard(
    label: String,
    tokenPreview: String,
    decoded: DecodedJwt,
) {
    var headerExpanded by remember { mutableStateOf(false) }
    var payloadExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(
                color = PulseColors.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
    ) {
        // Label + preview
        Text(
            text = label,
            color = PulseColors.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = tokenPreview,
            color = PulseColors.onSurfaceDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(Modifier.height(8.dp))

        // Expiry status
        Row {
            if (decoded.isExpired) {
                Text(
                    text = "Expired",
                    color = PulseColors.serverError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else if (decoded.expiresAt != null) {
                Text(
                    text = "Valid until ${formatEpochSeconds(decoded.expiresAt)}",
                    color = PulseColors.success,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = "No expiry",
                    color = PulseColors.onSurfaceDim,
                    fontSize = 11.sp,
                )
            }
        }

        // Issuer / Subject info
        if (decoded.issuer != null) {
            Spacer(Modifier.height(2.dp))
            Row {
                Text(
                    text = "Issuer: ",
                    color = PulseColors.onSurfaceDim,
                    fontSize = 11.sp,
                )
                Text(
                    text = decoded.issuer,
                    color = PulseColors.onSurface,
                    fontSize = 11.sp,
                )
            }
        }
        if (decoded.subject != null) {
            Spacer(Modifier.height(2.dp))
            Row {
                Text(
                    text = "Subject: ",
                    color = PulseColors.onSurfaceDim,
                    fontSize = 11.sp,
                )
                Text(
                    text = decoded.subject,
                    color = PulseColors.onSurface,
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Collapsible header section
        CollapsibleJsonSection(
            title = "Header",
            json = decoded.header,
            expanded = headerExpanded,
            onToggle = { headerExpanded = !headerExpanded },
        )

        Spacer(Modifier.height(4.dp))

        // Collapsible payload section
        CollapsibleJsonSection(
            title = "Payload",
            json = decoded.payload,
            expanded = payloadExpanded,
            onToggle = { payloadExpanded = !payloadExpanded },
        )
    }
}

@Composable
private fun CollapsibleJsonSection(
    title: String,
    json: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val indicator = if (expanded) "[-]" else "[+]"

    Text(
        text = "$indicator $title",
        color = PulseColors.redirect,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clickable(onClick = onToggle)
            .padding(vertical = 2.dp),
    )

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(
                        color = PulseColors.surface,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
            ) {
                Text(
                    text = json,
                    color = PulseColors.onSurface,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

/**
 * Format epoch seconds to a simple readable date/time string.
 */
private fun formatEpochSeconds(epochSeconds: Long): String {
    val totalSeconds = epochSeconds
    val days = totalSeconds / 86400
    val remainingAfterDays = totalSeconds % 86400
    val hours = (remainingAfterDays / 3600) % 24
    val minutes = (remainingAfterDays / 60) % 60
    val seconds = remainingAfterDays % 60

    // Calculate approximate date from epoch
    // Simple calculation: days since epoch -> year/month/day
    var remainingDays = days
    var year = 1970L
    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (remainingDays < daysInYear) break
        remainingDays -= daysInYear
        year++
    }

    val monthDays = if (isLeapYear(year)) {
        intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    } else {
        intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }

    var month = 1
    for (md in monthDays) {
        if (remainingDays < md) break
        remainingDays -= md
        month++
    }
    val day = remainingDays + 1

    return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} " +
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')} UTC"
}

private fun isLeapYear(year: Long): Boolean =
    (year % 4 == 0L && year % 100 != 0L) || (year % 400 == 0L)
