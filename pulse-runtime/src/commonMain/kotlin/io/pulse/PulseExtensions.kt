@file:Suppress("unused")

package io.pulse

import io.pulse.model.CrashEntry
import io.pulse.model.HttpTransaction
import io.pulse.store.TransactionStore
import io.pulse.ui.theme.PulseTheme
import kotlinx.coroutines.flow.StateFlow

// ---------------------------------------------------------------------------
//  Public extension API on Pulse — available when using :pulse-runtime
// ---------------------------------------------------------------------------

val Pulse.transactions: StateFlow<List<HttpTransaction>> get() = PulseCore.transactions
val Pulse.crashes: StateFlow<List<CrashEntry>> get() = PulseCore.crashes
val Pulse.store: TransactionStore get() = PulseCore.store

var Pulse.enabled: Boolean
    get() = PulseCore.enabled
    set(value) { PulseCore.enabled = value }

var Pulse.accessMode: PulseAccessMode
    get() = PulseCore.accessMode
    set(value) { PulseCore.accessMode = value }

var Pulse.currentTheme: PulseTheme
    get() = PulseCore.currentTheme
    set(value) { PulseCore.currentTheme = value }

var Pulse.notificationContentType: NotificationContentType
    get() = PulseCore.notificationContentType
    set(value) { PulseCore.notificationContentType = value }

var Pulse.showPerformanceOverlay: Boolean
    get() = PulseCore.showPerformanceOverlay
    set(value) { PulseCore.showPerformanceOverlay = value }

val Pulse.maxTransactions: Int get() = PulseCore.maxTransactions

fun Pulse.configure(block: PulseConfig.() -> Unit) = PulseCore.configure(block)
fun Pulse.clear() = PulseCore.clear()
fun Pulse.clearNetwork() = PulseCore.clearNetwork()
fun Pulse.clearCrashes() = PulseCore.clearCrashes()
fun Pulse.recordCrash(thread: String, throwable: Throwable) =
    PulseCore.recordCrash(thread, throwable)
