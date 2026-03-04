package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import platform.Foundation.NSProcessInfo

@Composable
internal actual fun rememberPerformanceSnapshots(): State<PerformanceSnapshot> {
    val snapshot = remember {
        mutableStateOf(
            PerformanceSnapshot(
                cpuUsagePercent = 0f,
                memoryUsedMb = 0,
                memoryTotalMb = 0,
                memoryUsagePercent = 0f,
                fps = 60,
            ),
        )
    }

    LaunchedEffect(Unit) {
        val process = NSProcessInfo.processInfo
        val totalMb = (process.physicalMemory / (1024uL * 1024uL)).toLong()

        while (true) {
            // iOS does not expose per-process memory usage without private APIs,
            // so we report physical memory as total and leave used as an estimate.
            // CPU is unavailable without private APIs; report 0.
            snapshot.value = PerformanceSnapshot(
                cpuUsagePercent = 0f,
                memoryUsedMb = 0,
                memoryTotalMb = totalMb,
                memoryUsagePercent = 0f,
                fps = 60,
            )

            delay(500)
        }
    }

    return snapshot
}
