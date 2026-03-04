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

private const val COLLAPSE_THRESHOLD = 5
private const val COLLAPSED_VISIBLE_COUNT = 3

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

@Composable
private fun HeaderRow(key: String, value: String) {
    val displayValue = SecurityManager.redactHeaderValue(key, value)
    val isRedacted = displayValue != value

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$key: ",
            color = PulseColors.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = displayValue,
            color = if (isRedacted) PulseColors.onSurfaceDim.copy(alpha = 0.5f)
            else PulseColors.onSurfaceDim,
            fontSize = 12.sp,
            fontStyle = if (isRedacted) FontStyle.Italic else FontStyle.Normal,
        )
        if (isRedacted) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "[REDACTED]",
                color = PulseColors.onSurfaceDim.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
