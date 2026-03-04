package io.pulse.internal

import androidx.compose.runtime.Composable

internal data class InfoSection(
    val title: String,
    val entries: List<Pair<String, String>>,
)

@Composable
internal expect fun rememberDeviceInfoSections(): List<InfoSection>
