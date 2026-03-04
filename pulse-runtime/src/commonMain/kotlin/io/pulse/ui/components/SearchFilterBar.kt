package io.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.ui.theme.PulseColors

internal enum class StatusFilter(val label: String) {
    All("All"),
    Success("2xx"),
    Redirect("3xx"),
    ClientError("4xx"),
    ServerError("5xx"),
    Failed("Error"),
}

@Composable
internal fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: StatusFilter,
    onFilterChange: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PulseColors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Search field
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = PulseColors.onSurface,
                fontSize = 14.sp,
            ),
            cursorBrush = SolidColor(PulseColors.onSurface),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PulseColors.searchBackground,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search URL, host, method...",
                            color = PulseColors.onSurfaceDim,
                            fontSize = 14.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatusFilter.entries.forEach { filter ->
                FilterChip(
                    label = filter.label,
                    selected = filter == selectedFilter,
                    onClick = { onFilterChange(filter) },
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) {
        PulseColors.onSurface.copy(alpha = 0.15f)
    } else {
        PulseColors.surfaceVariant
    }
    val textColor = if (selected) {
        PulseColors.onSurface
    } else {
        PulseColors.onSurfaceDim
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
