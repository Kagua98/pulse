package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.Pulse
import io.pulse.PulseCore
import io.pulse.internal.getRetainedObjectCount
import io.pulse.internal.isLeakDetectionAvailable
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.components.ToolCard
import io.pulse.ui.theme.PulseColors

@Composable
internal fun PulseHome(
    onNavigate: (PulseDestination) -> Unit,
    onClose: () -> Unit,
) {
    val transactions by PulseCore.transactions.collectAsState()
    val logs by Pulse.logs.collectAsState()
    val crashes by PulseCore.crashes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        PulseTopBar(
            title = "Pulse",
            actions = {
                TextButton(onClick = { PulseCore.clear() }) {
                    Text(
                        "Clear All",
                        color = PulseColors.serverError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = PulseColors.onSurfaceDim.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                        ),
                ) {
                    TextButton(onClick = onClose) {
                        Text(
                            "Close",
                            color = PulseColors.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            },
        )

        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ToolCard(
                title = "Network",
                subtitle = "${transactions.size} transactions",
                accentColor = PulseColors.success,
                icon = "NET",
                onClick = { onNavigate(PulseDestination.Network) },
            )
            ToolCard(
                title = "Logs",
                subtitle = "${logs.size} entries",
                accentColor = PulseColors.redirect,
                icon = "LOG",
                onClick = { onNavigate(PulseDestination.Logs) },
            )
            ToolCard(
                title = "Crashes",
                subtitle = if (crashes.isEmpty()) "No crashes" else "${crashes.size} caught",
                accentColor = PulseColors.serverError,
                icon = "ERR",
                onClick = { onNavigate(PulseDestination.Crashes) },
            )
            ToolCard(
                title = "Leaks",
                subtitle = if (isLeakDetectionAvailable) {
                    "${getRetainedObjectCount()} retained objects"
                } else {
                    "Not available"
                },
                accentColor = PulseColors.clientError,
                icon = "MEM",
                onClick = { onNavigate(PulseDestination.Leaks) },
            )
            ToolCard(
                title = "Device Info",
                subtitle = "App, device, OS, memory",
                accentColor = PulseColors.teal,
                icon = "SYS",
                onClick = { onNavigate(PulseDestination.Device) },
            )
            ToolCard(
                title = "Settings",
                subtitle = "Access mode, storage, debug",
                accentColor = PulseColors.blueGrey,
                icon = "CFG",
                onClick = { onNavigate(PulseDestination.Settings) },
            )

            // Version indicator
            Text(
                text = "Pulse v${io.pulse.PulseVersion.NAME}",
                color = PulseColors.onSurfaceDim.copy(alpha = 0.4f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
            )
        }
    }
}
