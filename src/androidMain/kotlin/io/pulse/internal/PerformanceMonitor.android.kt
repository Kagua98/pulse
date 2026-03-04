package io.pulse.internal

import android.app.ActivityManager
import android.content.Context
import android.view.Choreographer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.io.RandomAccessFile

@Composable
internal actual fun rememberPerformanceSnapshots(): State<PerformanceSnapshot> {
    val context = LocalContext.current
    val snapshot = remember {
        mutableStateOf(
            PerformanceSnapshot(
                cpuUsagePercent = 0f,
                memoryUsedMb = 0,
                memoryTotalMb = 0,
                memoryUsagePercent = 0f,
                fps = 0,
            ),
        )
    }

    // FPS counter using Choreographer
    val frameCount = remember { mutableIntStateOf(0) }
    val lastFrameTime = remember { mutableLongStateOf(System.nanoTime()) }
    val currentFps = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val choreographer = Choreographer.getInstance()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount.intValue++
                val elapsed = frameTimeNanos - lastFrameTime.longValue
                if (elapsed >= 1_000_000_000L) {
                    currentFps.intValue = frameCount.intValue
                    frameCount.intValue = 0
                    lastFrameTime.longValue = frameTimeNanos
                }
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
    }

    // CPU tracking state
    val prevProcessCpu = remember { mutableLongStateOf(0L) }
    val prevTotalCpu = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val cpuPercent = readCpuUsage(
                prevProcessCpu.longValue,
                prevTotalCpu.longValue,
            ) { procCpu, totalCpu ->
                prevProcessCpu.longValue = procCpu
                prevTotalCpu.longValue = totalCpu
            }

            val rt = Runtime.getRuntime()
            val heapUsed = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L)

            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val totalMb = mi.totalMem / (1024L * 1024L)

            val memPercent = if (totalMb > 0) {
                (heapUsed.toFloat() / totalMb.toFloat()) * 100f
            } else {
                0f
            }

            snapshot.value = PerformanceSnapshot(
                cpuUsagePercent = cpuPercent,
                memoryUsedMb = heapUsed,
                memoryTotalMb = totalMb,
                memoryUsagePercent = memPercent,
                fps = currentFps.intValue,
            )

            delay(500)
        }
    }

    return snapshot
}

/**
 * Reads CPU usage by parsing /proc/self/stat for process ticks and /proc/stat
 * for total system ticks, then computing the delta since the last reading.
 */
private fun readCpuUsage(
    prevProcessCpu: Long,
    prevTotalCpu: Long,
    onUpdate: (processCpu: Long, totalCpu: Long) -> Unit,
): Float {
    return try {
        // Process CPU ticks from /proc/self/stat (fields 14 + 15 = utime + stime)
        val processStat = RandomAccessFile("/proc/self/stat", "r").use { it.readLine() }
        val processFields = processStat.split(" ")
        val utime = processFields[13].toLong()
        val stime = processFields[14].toLong()
        val processCpuNow = utime + stime

        // Total CPU ticks from /proc/stat (first "cpu" line)
        val totalStat = RandomAccessFile("/proc/stat", "r").use { it.readLine() }
        val totalFields = totalStat.trim().split("\\s+".toRegex())
        // Fields 1..n are: user, nice, system, idle, iowait, irq, softirq, steal
        val totalCpuNow = totalFields.drop(1).sumOf { it.toLongOrNull() ?: 0L }

        if (prevTotalCpu == 0L || prevProcessCpu == 0L) {
            onUpdate(processCpuNow, totalCpuNow)
            0f
        } else {
            val processDelta = processCpuNow - prevProcessCpu
            val totalDelta = totalCpuNow - prevTotalCpu
            onUpdate(processCpuNow, totalCpuNow)
            if (totalDelta > 0) {
                (processDelta.toFloat() / totalDelta.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }
        }
    } catch (_: Exception) {
        onUpdate(prevProcessCpu, prevTotalCpu)
        0f
    }
}
