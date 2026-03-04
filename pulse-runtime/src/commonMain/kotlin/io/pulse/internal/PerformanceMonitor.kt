package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

internal data class PerformanceSnapshot(
    val cpuUsagePercent: Float,
    val memoryUsedMb: Long,
    val memoryTotalMb: Long,
    val memoryUsagePercent: Float,
    val fps: Int,
)

@Composable
internal expect fun rememberPerformanceSnapshots(): State<PerformanceSnapshot>
