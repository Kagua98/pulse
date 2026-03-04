package io.pulse.internal

import androidx.compose.runtime.Composable

@Composable
internal expect fun PulseBackHandler(enabled: Boolean = true, onBack: () -> Unit)
