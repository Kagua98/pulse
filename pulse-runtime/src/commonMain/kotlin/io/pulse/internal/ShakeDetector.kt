package io.pulse.internal

import androidx.compose.runtime.Composable

/**
 * Composable effect that detects a "shake" gesture and invokes [onShake].
 * Only functional on Android; no-op on desktop and native targets.
 */
@Composable
internal expect fun ShakeDetectorEffect(onShake: () -> Unit)
