package io.pulse.internal

import androidx.compose.runtime.Composable

@Composable
internal actual fun PulseBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back via swipe gestures at the app level
}
