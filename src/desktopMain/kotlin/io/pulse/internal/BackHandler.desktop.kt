package io.pulse.internal

import androidx.compose.runtime.Composable

@Composable
internal actual fun PulseBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop has no system back button
}
