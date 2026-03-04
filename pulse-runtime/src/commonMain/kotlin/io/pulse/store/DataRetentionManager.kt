package io.pulse.store

import io.pulse.internal.SecurityManager
import io.pulse.internal.epochMillis
import io.pulse.log.LogStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Periodically removes transactions, logs, and crashes that are older than the
 * retention period configured in [SecurityManager.dataRetentionMs].
 *
 * When the retention period is `0` (the default), no automatic cleanup occurs.
 */
internal object DataRetentionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    /** Check interval -- how often we scan for expired data. */
    private const val CHECK_INTERVAL_MS = 60_000L // 1 minute

    /**
     * Starts the retention policy loop. Safe to call multiple times; restarts the
     * existing job if one is already running.
     */
    fun startRetentionPolicy(
        transactionStore: TransactionStore,
        logStore: LogStore,
        crashStore: CrashStore,
    ) {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                clearExpiredData(transactionStore, logStore, crashStore)
            }
        }
    }

    /**
     * Stops the background retention loop.
     */
    fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * Immediately removes any entries whose timestamp is older than
     * [SecurityManager.dataRetentionMs] from the current epoch.
     *
     * Does nothing when `dataRetentionMs <= 0`.
     */
    fun clearExpiredData(
        transactionStore: TransactionStore,
        logStore: LogStore,
        crashStore: CrashStore,
    ) {
        val retentionMs = SecurityManager.dataRetentionMs
        if (retentionMs <= 0L) return

        val cutoff = epochMillis() - retentionMs

        transactionStore.removeOlderThan(cutoff)
        logStore.removeOlderThan(cutoff)
        crashStore.removeOlderThan(cutoff)
    }
}
