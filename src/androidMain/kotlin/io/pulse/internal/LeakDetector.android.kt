package io.pulse.internal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.pulse.ui.theme.PulseColors

private val leakCanaryAvailable: Boolean by lazy {
    try {
        Class.forName("leakcanary.AppWatcher")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}

internal actual val isLeakDetectionAvailable: Boolean
    get() = leakCanaryAvailable

internal actual fun getRetainedObjectCount(): Int {
    if (!leakCanaryAvailable) return 0
    return try {
        leakcanary.AppWatcher.objectWatcher.retainedObjectCount
    } catch (_: Throwable) {
        0
    }
}

internal actual fun triggerHeapDump() {
    if (!leakCanaryAvailable) return
    try {
        leakcanary.LeakCanary.dumpHeap()
    } catch (_: Throwable) {
        // LeakCanary not available
    }
}

@Composable
internal actual fun LeakCanaryLauncher() {
    if (!leakCanaryAvailable) return
    val context = LocalContext.current
    TextButton(
        onClick = {
            try {
                context.startActivity(leakcanary.LeakCanary.newLeakDisplayActivityIntent())
            } catch (_: Throwable) {
                // LeakCanary not available
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Open LeakCanary", color = PulseColors.redirect)
    }
}
