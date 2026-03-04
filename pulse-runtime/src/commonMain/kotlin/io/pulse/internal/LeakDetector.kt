package io.pulse.internal

import androidx.compose.runtime.Composable

internal expect val isLeakDetectionAvailable: Boolean

internal expect fun getRetainedObjectCount(): Int

internal expect fun triggerHeapDump()

@Composable
internal expect fun LeakCanaryLauncher()
