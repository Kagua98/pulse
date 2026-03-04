package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.Pulse
import io.pulse.log.LogEntry
import io.pulse.log.LogLevel
import io.pulse.ui.components.EmptyState
import io.pulse.ui.components.LogListItem
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.components.levelColor
import io.pulse.ui.theme.PulseColors
import io.pulse.util.formatTimestamp

@Composable
internal fun LogViewerScreen(onBack: () -> Unit) {
    val logs by Pulse.logs.collectAsState()
    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedLogId by remember { mutableStateOf<String?>(null) }

    val filteredLogs by remember(logs, selectedLevel, searchQuery) {
        derivedStateOf {
            logs
                .filter { selectedLevel == null || it.level == selectedLevel }
                .filter { entry ->
                    searchQuery.isBlank() ||
                        entry.tag.contains(searchQuery, ignoreCase = true) ||
                        entry.message.contains(searchQuery, ignoreCase = true)
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        PulseTopBar(
            title = "Logs",
            subtitle = "${logs.size} entries",
            onBack = onBack,
            actions = {
                TextButton(onClick = { Pulse.clearLogs() }) {
                    Text("Clear", color = PulseColors.serverError, fontSize = 12.sp)
                }
            },
        )

        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        LogSearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            selectedLevel = selectedLevel,
            onLevelChange = { selectedLevel = it },
            filteredCount = filteredLogs.size,
            totalCount = logs.size,
        )

        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        if (filteredLogs.isEmpty()) {
            if (logs.isEmpty()) {
                EmptyState(
                    title = "No logs recorded",
                    subtitle = "Log entries from your app will appear here. Use Pulse.d(), Pulse.i(), Pulse.w(), or Pulse.e() to log.",
                    icon = "LOG",
                )
            } else {
                EmptyState(
                    title = "No matching logs",
                    subtitle = "Try adjusting your search or filter criteria",
                    icon = "?",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState(),
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            ) {
                items(items = filteredLogs, key = { it.id }) { entry ->
                    if (expandedLogId == entry.id) {
                        ExpandedLogItem(entry = entry, onCollapse = { expandedLogId = null })
                    } else {
                        LogListItem(
                            entry = entry,
                            onClick = { expandedLogId = entry.id },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogSearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedLevel: LogLevel?,
    onLevelChange: (LogLevel?) -> Unit,
    filteredCount: Int,
    totalCount: Int,
) {
    Column(
        modifier = Modifier
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
            textStyle = TextStyle(color = PulseColors.onSurface, fontSize = 14.sp),
            cursorBrush = SolidColor(PulseColors.onSurface),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseColors.searchBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    if (query.isEmpty()) {
                        Text("Search tag, message...", color = PulseColors.onSurfaceDim, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            },
        )

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogLevelChip(
                label = "All",
                selected = selectedLevel == null,
                onClick = { onLevelChange(null) },
            )
            LogLevel.entries.forEach { level ->
                LogLevelChip(
                    label = level.label,
                    selected = selectedLevel == level,
                    onClick = { onLevelChange(level) },
                    accentColor = levelColor(level),
                )
            }
        }

        // Log count indicator
        if (totalCount > 0) {
            val countText = if (filteredCount == totalCount) {
                "$totalCount entries"
            } else {
                "$filteredCount of $totalCount entries"
            }
            Text(
                text = countText,
                color = PulseColors.onSurfaceDim.copy(alpha = 0.7f),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun LogLevelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color? = null,
) {
    val effectiveColor = accentColor ?: PulseColors.onSurface
    val bgColor = if (selected) {
        effectiveColor.copy(alpha = 0.15f)
    } else {
        PulseColors.surfaceVariant
    }
    val textColor = if (selected) {
        effectiveColor
    } else {
        PulseColors.onSurfaceDim
    }
    val borderModifier = if (selected) {
        Modifier.border(1.dp, effectiveColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .then(borderModifier)
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Color dot indicator for level chips
        if (accentColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (selected) accentColor else accentColor.copy(alpha = 0.4f),
                        CircleShape,
                    ),
            )
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ExpandedLogItem(entry: LogEntry, onCollapse: () -> Unit) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PulseColors.surfaceVariant)
                .padding(12.dp),
        ) {
            Text(
                text = "${entry.level.label} | ${formatTimestamp(entry.timestamp)} | ${entry.tag}",
                color = levelColor(entry.level),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = entry.message,
                color = PulseColors.onSurface,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            val throwableText = entry.throwable
            if (!throwableText.isNullOrBlank()) {
                Text(
                    text = throwableText,
                    color = PulseColors.serverError,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            TextButton(onClick = onCollapse) {
                Text("Collapse", color = PulseColors.onSurfaceDim, fontSize = 11.sp)
            }
        }
    }
}
