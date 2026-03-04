package io.pulse.store

import io.pulse.model.HttpTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface TransactionStore {
    val transactions: StateFlow<List<HttpTransaction>>
    fun addTransaction(transaction: HttpTransaction)
    fun updateTransaction(id: String, update: (HttpTransaction) -> HttpTransaction)
    fun clear()
    fun removeOlderThan(cutoffTimestamp: Long)
}

internal class InMemoryTransactionStore(
    private val maxSize: Int = 500,
) : TransactionStore {

    private val _transactions = MutableStateFlow<List<HttpTransaction>>(emptyList())
    override val transactions: StateFlow<List<HttpTransaction>> = _transactions.asStateFlow()

    override fun addTransaction(transaction: HttpTransaction) {
        _transactions.update { current ->
            (listOf(transaction) + current).take(maxSize)
        }
    }

    override fun updateTransaction(id: String, update: (HttpTransaction) -> HttpTransaction) {
        _transactions.update { current ->
            current.map { if (it.id == id) update(it) else it }
        }
    }

    override fun clear() {
        _transactions.value = emptyList()
    }

    override fun removeOlderThan(cutoffTimestamp: Long) {
        _transactions.update { current ->
            current.filter { it.timestamp >= cutoffTimestamp }
        }
    }
}
