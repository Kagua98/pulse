package io.pulse.store

import io.pulse.model.CrashEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CrashStore(private val maxSize: Int = 50) {

    private val _crashes = MutableStateFlow<List<CrashEntry>>(emptyList())
    val crashes: StateFlow<List<CrashEntry>> = _crashes.asStateFlow()

    fun add(entry: CrashEntry) {
        _crashes.update { current -> (listOf(entry) + current).take(maxSize) }
    }

    fun clear() {
        _crashes.value = emptyList()
    }

    fun removeOlderThan(cutoffTimestamp: Long) {
        _crashes.update { current ->
            current.filter { it.timestamp >= cutoffTimestamp }
        }
    }
}
