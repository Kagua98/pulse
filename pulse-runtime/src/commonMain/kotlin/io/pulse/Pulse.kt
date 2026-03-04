package io.pulse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.pulse.internal.epochMillis
import io.pulse.internal.installCrashHandler
import io.pulse.log.LogEntry
import io.pulse.log.LogLevel
import io.pulse.log.PulseLog
import io.pulse.model.CrashEntry
import io.pulse.model.HttpTransaction
import io.pulse.store.CrashStore
import io.pulse.store.InMemoryTransactionStore
import io.pulse.store.TransactionStore
import io.pulse.ui.theme.PulseTheme
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object Pulse {

    init {
        installCrashHandler { threadName, throwable ->
            recordCrash(threadName, throwable)
        }
    }

    private var _store: TransactionStore = InMemoryTransactionStore()
    val store: TransactionStore get() = _store

    val crashStore = CrashStore()

    var enabled: Boolean = true
    var accessMode: PulseAccessMode by mutableStateOf(PulseAccessMode.Fab)
    var currentTheme: PulseTheme by mutableStateOf(PulseTheme.Purple)
    var notificationContentType: NotificationContentType by mutableStateOf(NotificationContentType.NetworkActivity)
    var showPerformanceOverlay: Boolean by mutableStateOf(false)
    var maxTransactions: Int = 500
        private set

    val transactions: StateFlow<List<HttpTransaction>> get() = _store.transactions
    val logs: StateFlow<List<LogEntry>> get() = PulseLog.store.logs
    val crashes: StateFlow<List<CrashEntry>> get() = crashStore.crashes

    fun configure(block: PulseConfig.() -> Unit) {
        val config = PulseConfig().apply(block)
        enabled = config.enabled
        maxTransactions = config.maxTransactions
        _store = InMemoryTransactionStore(config.maxTransactions)
    }

    fun clear() {
        _store.clear()
        PulseLog.clear()
        crashStore.clear()
    }

    fun clearNetwork() = _store.clear()
    fun clearLogs() = PulseLog.clear()
    fun clearCrashes() = crashStore.clear()

    // --- Logging API (delegates to PulseLog) ---

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) =
        PulseLog.log(level, tag, message, throwable)

    fun v(tag: String, message: String) = PulseLog.v(tag, message)
    fun d(tag: String, message: String) = PulseLog.d(tag, message)
    fun i(tag: String, message: String) = PulseLog.i(tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) = PulseLog.w(tag, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) = PulseLog.e(tag, message, throwable)

    // --- Crash API ---

    @OptIn(ExperimentalUuidApi::class)
    fun recordCrash(thread: String, throwable: Throwable) {
        crashStore.add(
            CrashEntry(
                id = Uuid.random().toString(),
                threadName = thread,
                exceptionClass = throwable::class.simpleName ?: "Unknown",
                message = throwable.message ?: "",
                stackTrace = throwable.stackTraceToString(),
                timestamp = epochMillis(),
            ),
        )
    }
}
