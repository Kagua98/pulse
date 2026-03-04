package io.pulse.internal

import androidx.compose.runtime.Composable

internal actual val isLeakDetectionAvailable: Boolean = false
internal actual fun getRetainedObjectCount(): Int = 0
internal actual fun triggerHeapDump() {}

@Composable
internal actual fun LeakCanaryLauncher() {}
