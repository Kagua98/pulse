package io.pulse.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LogStore(private val maxSize: Int = 2000) {

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun add(entry: LogEntry) {
        _logs.update { current -> (listOf(entry) + current).take(maxSize) }
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun removeOlderThan(cutoffTimestamp: Long) {
        _logs.update { current ->
            current.filter { it.timestamp >= cutoffTimestamp }
        }
    }
}
