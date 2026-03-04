package io.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.log.LogEntry
import io.pulse.log.LogLevel
import io.pulse.ui.theme.PulseColors
import io.pulse.util.formatTimestamp

@Composable
internal fun LogListItem(
    entry: LogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val levelColor = levelColor(entry.level)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .background(levelColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .width(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.level.label,
                color = levelColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = formatTimestamp(entry.timestamp),
            color = PulseColors.onSurfaceDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.tag,
                color = levelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.message,
                color = PulseColors.onSurface,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
            )
        }
    }
}

internal fun levelColor(level: LogLevel): Color = when (level) {
    LogLevel.VERBOSE -> PulseColors.logVerbose
    LogLevel.DEBUG -> PulseColors.logDebug
    LogLevel.INFO -> PulseColors.logInfo
    LogLevel.WARN -> PulseColors.logWarn
    LogLevel.ERROR -> PulseColors.logError
}
