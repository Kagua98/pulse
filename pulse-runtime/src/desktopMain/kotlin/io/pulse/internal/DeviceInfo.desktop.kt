package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberDeviceInfoSections(): List<InfoSection> = remember {
    val rt = Runtime.getRuntime()
    listOf(
        InfoSection(
            title = "Runtime",
            entries = listOf(
                "JVM" to System.getProperty("java.vm.name", "Unknown"),
                "Java Version" to System.getProperty("java.version", "Unknown"),
                "Kotlin" to KotlinVersion.CURRENT.toString(),
                "Processors" to rt.availableProcessors().toString(),
            ),
        ),
        InfoSection(
            title = "Operating System",
            entries = listOf(
                "OS" to System.getProperty("os.name", "Unknown"),
                "Version" to System.getProperty("os.version", "Unknown"),
                "Architecture" to System.getProperty("os.arch", "Unknown"),
            ),
        ),
        InfoSection(
            title = "Memory",
            entries = listOf(
                "Heap Used" to "${(rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)} MB",
                "Heap Max" to "${rt.maxMemory() / (1024 * 1024)} MB",
                "Heap Free" to "${rt.freeMemory() / (1024 * 1024)} MB",
            ),
        ),
    )
}
