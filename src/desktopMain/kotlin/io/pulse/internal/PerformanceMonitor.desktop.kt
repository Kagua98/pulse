package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.delay
import java.lang.management.ManagementFactory

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
        val osBean = ManagementFactory.getOperatingSystemMXBean() as? OperatingSystemMXBean

        while (true) {
            val rt = Runtime.getRuntime()
            val heapUsed = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L)
            val heapMax = rt.maxMemory() / (1024L * 1024L)

            val cpuLoad = osBean?.processCpuLoad ?: 0.0
            val cpuPercent = (cpuLoad * 100.0).toFloat().coerceIn(0f, 100f)

            val totalPhysical = osBean?.totalMemorySize?.let { it / (1024L * 1024L) } ?: heapMax

            val memPercent = if (totalPhysical > 0) {
                (heapUsed.toFloat() / totalPhysical.toFloat()) * 100f
            } else {
                0f
            }

            snapshot.value = PerformanceSnapshot(
                cpuUsagePercent = cpuPercent,
                memoryUsedMb = heapUsed,
                memoryTotalMb = totalPhysical,
                memoryUsagePercent = memPercent,
                fps = 60,
            )

            delay(500)
        }
    }

    return snapshot
}
