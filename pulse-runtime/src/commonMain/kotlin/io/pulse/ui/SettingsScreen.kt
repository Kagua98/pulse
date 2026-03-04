package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.NotificationContentType
import io.pulse.Pulse
import io.pulse.PulseAccessMode
import io.pulse.internal.SecurityManager
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.theme.PulseColors
import io.pulse.ui.theme.PulseTheme
import io.pulse.ui.theme.paletteFor

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsScreen(onBack: () -> Unit) {
    var accessMode by remember { mutableStateOf(Pulse.accessMode) }
    var notificationContentType by remember { mutableStateOf(Pulse.notificationContentType) }
    var maxTransactions by remember { mutableStateOf(Pulse.maxTransactions) }
    var selectedTheme by remember { mutableStateOf(Pulse.currentTheme) }
    var showPerfOverlay by remember { mutableStateOf(Pulse.showPerformanceOverlay) }
    var redactHeaders by remember { mutableStateOf(SecurityManager.redactSensitiveHeaders) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        PulseTopBar(title = "Settings", onBack = onBack)
        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // --- Access Mode Section ---
            SectionHeader("Access Mode")
            SectionDescription("Choose how to launch the Pulse inspector")
            SettingsCard {
                AccessModeOption(
                    label = "FAB",
                    description = "Floating action button overlay",
                    selected = accessMode == PulseAccessMode.Fab,
                    onClick = {
                        accessMode = PulseAccessMode.Fab
                        Pulse.accessMode = PulseAccessMode.Fab
                    },
                )
                AccessModeOption(
                    label = "Notification",
                    description = "Persistent notification (Android only)",
                    selected = accessMode == PulseAccessMode.Notification,
                    onClick = {
                        accessMode = PulseAccessMode.Notification
                        Pulse.accessMode = PulseAccessMode.Notification
                    },
                )
                AccessModeOption(
                    label = "Shake Gesture",
                    description = "Shake device to open (Android only)",
                    selected = accessMode == PulseAccessMode.ShakeGesture,
                    onClick = {
                        accessMode = PulseAccessMode.ShakeGesture
                        Pulse.accessMode = PulseAccessMode.ShakeGesture
                    },
                )
            }

            // --- Notification Content Section (visible only in Notification mode) ---
            if (accessMode == PulseAccessMode.Notification) {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Notification Content")
                SectionDescription("Choose what the notification displays")
                SettingsCard {
                    NotificationContentType.entries.forEach { type ->
                        AccessModeOption(
                            label = type.label,
                            description = type.description,
                            selected = notificationContentType == type,
                            onClick = {
                                notificationContentType = type
                                Pulse.notificationContentType = type
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- Theme Section ---
            SectionHeader("Theme")
            SectionDescription("Customize the inspector appearance")
            SettingsCard {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PulseTheme.entries.forEach { theme ->
                        ThemeChip(
                            theme = theme,
                            selected = selectedTheme == theme,
                            onClick = {
                                selectedTheme = theme
                                Pulse.currentTheme = theme
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- Performance Monitor Section ---
            SectionHeader("Performance Monitor")
            SectionDescription("Real-time performance metrics overlay")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPerfOverlay = !showPerfOverlay
                            Pulse.showPerformanceOverlay = showPerfOverlay
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Performance Overlay",
                            color = PulseColors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Real-time CPU, RAM, and FPS metrics",
                            color = PulseColors.onSurfaceDim,
                            fontSize = 11.sp,
                        )
                    }
                    Switch(
                        checked = showPerfOverlay,
                        onCheckedChange = { checked ->
                            showPerfOverlay = checked
                            Pulse.showPerformanceOverlay = checked
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PulseColors.success,
                            checkedTrackColor = PulseColors.success.copy(alpha = 0.3f),
                            uncheckedThumbColor = PulseColors.onSurfaceDim,
                            uncheckedTrackColor = PulseColors.divider,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- Security Section ---
            SectionHeader("Security")
            SectionDescription("Control sensitive data visibility")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            redactHeaders = !redactHeaders
                            SecurityManager.redactSensitiveHeaders = redactHeaders
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Redact Sensitive Headers",
                            color = PulseColors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Mask Authorization, Cookie, API key values",
                            color = PulseColors.onSurfaceDim,
                            fontSize = 11.sp,
                        )
                    }
                    Switch(
                        checked = redactHeaders,
                        onCheckedChange = { checked ->
                            redactHeaders = checked
                            SecurityManager.redactSensitiveHeaders = checked
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PulseColors.success,
                            checkedTrackColor = PulseColors.success.copy(alpha = 0.3f),
                            uncheckedThumbColor = PulseColors.onSurfaceDim,
                            uncheckedTrackColor = PulseColors.divider,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- Storage Section ---
            SectionHeader("Storage")
            SectionDescription("Manage transaction retention limits")
            SettingsCard {
                Text(
                    text = "Max transactions",
                    color = PulseColors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StorageOptions.forEach { option ->
                        StorageChip(
                            value = option,
                            selected = maxTransactions == option,
                            onClick = {
                                maxTransactions = option
                                Pulse.configure { this.maxTransactions = option }
                            },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                // Clear All Data button with confirmation
                if (showClearConfirmation) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "This will delete all transactions, logs, and crash data.",
                            color = PulseColors.serverError,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        PulseColors.divider,
                                        RoundedCornerShape(8.dp),
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showClearConfirmation = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = PulseColors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        PulseColors.serverError.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        Pulse.clear()
                                        showClearConfirmation = false
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Delete All",
                                    color = PulseColors.serverError,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                PulseColors.serverError.copy(alpha = 0.08f),
                                RoundedCornerShape(8.dp),
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showClearConfirmation = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Clear All Data",
                            color = PulseColors.serverError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- Debug Section ---
            SectionHeader("Debug")
            SectionDescription("Library and build information")
            SettingsCard {
                DebugRow(label = "Build type", value = "debug")
                DebugRow(label = "Library", value = "Pulse / Pulse")
            }

            // --- Version info ---
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pulse v1.0.0",
                color = PulseColors.onSurfaceDim.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        }
    }
}

private val StorageOptions = listOf(500, 1000, 2000, 5000)

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = PulseColors.onSurfaceDim,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 0.dp),
    )
}

@Composable
private fun SectionDescription(text: String) {
    Text(
        text = text,
        color = PulseColors.onSurfaceDim.copy(alpha = 0.7f),
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PulseColors.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(14.dp),
    ) {
        content()
    }
}

@Composable
private fun ThemeChip(
    theme: PulseTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = paletteFor(theme)
    val borderModifier = if (selected) {
        Modifier.border(2.dp, PulseColors.success, RoundedCornerShape(8.dp))
    } else {
        Modifier.border(1.dp, PulseColors.divider, RoundedCornerShape(8.dp))
    }
    val backgroundColor = if (selected) {
        PulseColors.success.copy(alpha = 0.08f)
    } else {
        PulseColors.surface
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 72.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(borderModifier)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(palette.surface, CircleShape)
                .border(2.dp, palette.success, CircleShape),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = theme.label,
            color = if (selected) PulseColors.success else PulseColors.onSurfaceDim,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AccessModeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PulseColors.success,
                unselectedColor = PulseColors.onSurfaceDim,
            ),
        )
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = label,
                color = PulseColors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                color = PulseColors.onSurfaceDim,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun StorageChip(
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        PulseColors.success.copy(alpha = 0.15f)
    } else {
        PulseColors.surface
    }
    val textColor = if (selected) {
        PulseColors.success
    } else {
        PulseColors.onSurfaceDim
    }
    val borderModifier = if (selected) {
        Modifier.border(1.5.dp, PulseColors.success, RoundedCornerShape(8.dp))
    } else {
        Modifier.border(1.dp, PulseColors.divider, RoundedCornerShape(8.dp))
    }
    Box(
        modifier = Modifier
            .widthIn(min = 56.dp)
            .then(borderModifier)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = PulseColors.onSurfaceDim,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = PulseColors.onSurface,
            fontSize = 12.sp,
        )
    }
}
