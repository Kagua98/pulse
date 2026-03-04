package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.Pulse
import io.pulse.model.CrashEntry
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.theme.PulseColors
import io.pulse.util.formatTimestamp

@Composable
internal fun CrashViewerScreen(onBack: () -> Unit) {
    val crashes by Pulse.crashes.collectAsState()
    var expandedCrashId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        PulseTopBar(
            title = "Crashes",
            subtitle = if (crashes.isEmpty()) "No crashes" else "${crashes.size} caught",
            onBack = onBack,
            actions = {
                if (crashes.isNotEmpty()) {
                    TextButton(onClick = { Pulse.clearCrashes() }) {
                        Text("Clear", color = PulseColors.serverError, fontSize = 12.sp)
                    }
                }
            },
        )

        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        if (crashes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No crashes recorded", color = PulseColors.onSurfaceDim, fontSize = 14.sp)
                    Text(
                        "Uncaught exceptions will appear here",
                        color = PulseColors.onSurfaceDim,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            ) {
                items(items = crashes, key = { it.id }) { crash ->
                    CrashListItem(
                        crash = crash,
                        expanded = expandedCrashId == crash.id,
                        onClick = {
                            expandedCrashId = if (expandedCrashId == crash.id) null else crash.id
                        },
                    )
                    HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun CrashListItem(
    crash: CrashEntry,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (expanded) PulseColors.surfaceVariant else PulseColors.surface)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        PulseColors.serverError.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = crash.exceptionClass,
                    color = PulseColors.serverError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = formatTimestamp(crash.timestamp),
                color = PulseColors.onSurfaceDim,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
            )
        }

        if (crash.message.isNotBlank()) {
            Text(
                text = crash.message,
                color = PulseColors.onSurface,
                fontSize = 12.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Text(
            text = "Thread: ${crash.threadName}",
            color = PulseColors.onSurfaceDim,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 2.dp),
        )

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            SelectionContainer {
                Text(
                    text = crash.stackTrace,
                    color = PulseColors.serverError.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseColors.surface, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                )
            }
        }
    }
}
