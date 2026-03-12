package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.pulse.model.HttpTransaction

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun dateNow(): Double = js("Date.now()")

internal actual fun epochMillis(): Long = dateNow().toLong()

internal actual fun assertDebugBuild() {}

internal actual fun installCrashHandler(onCrash: (threadName: String, throwable: Throwable) -> Unit) {}

internal actual val isLeakDetectionAvailable: Boolean = false
internal actual fun getRetainedObjectCount(): Int = 0
internal actual fun triggerHeapDump() {}

@Composable
internal actual fun LeakCanaryLauncher() {}

@Composable
internal actual fun PulseBackHandler(enabled: Boolean, onBack: () -> Unit) {}

@Composable
internal actual fun ShakeDetectorEffect(onShake: () -> Unit) {}

@Composable
internal actual fun DarkStatusBarEffect() {}

@Composable
internal actual fun NotificationAccessEffect(active: Boolean, onOpenRequested: () -> Unit) {}

internal actual fun showPersistentNotification(context: Any?) {}
internal actual fun dismissPersistentNotification(context: Any?) {}

@Composable
internal actual fun rememberDeviceInfoSections(): List<InfoSection> = remember {
    listOf(
        InfoSection(
            title = "Runtime",
            entries = listOf(
                "Platform" to "WasmJs",
                "Kotlin" to KotlinVersion.CURRENT.toString(),
            ),
        ),
    )
}

@Composable
internal actual fun rememberPerformanceSnapshots(): State<PerformanceSnapshot> {
    return remember {
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
}

internal actual class ShareContext

@Composable
internal actual fun rememberShareContext(): ShareContext = remember { ShareContext() }

internal actual fun shareText(context: ShareContext, title: String, text: String) {}
internal actual fun shareFile(context: ShareContext, title: String, filePath: String, mimeType: String) {}
internal actual fun writeTextToTempFile(context: ShareContext, text: String): String? = null

internal actual fun generateTransactionsPdf(
    context: ShareContext,
    transactions: List<HttpTransaction>,
): String? = null
