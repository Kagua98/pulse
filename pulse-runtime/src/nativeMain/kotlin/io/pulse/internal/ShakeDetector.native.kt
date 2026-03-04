package io.pulse.internal

import androidx.compose.runtime.Composable

@Composable
internal actual fun ShakeDetectorEffect(onShake: () -> Unit) {
    // No shake detection on native targets
}
