package io.pulse.internal

import android.content.pm.ApplicationInfo

internal actual fun assertDebugBuild() {
    val app = PulsePlatform.appContext
    val isDebuggable = (app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (!isDebuggable) {
        throw IllegalStateException(
            "Pulse must only be used in debug builds! " +
                "Use debugImplementation instead of implementation in your build.gradle.",
        )
    }
}
