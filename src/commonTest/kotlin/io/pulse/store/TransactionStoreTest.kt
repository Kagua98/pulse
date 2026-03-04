package io.pulse.store

import io.pulse.createTestTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionStoreTest {

    private fun createStore(maxSize: Int = 500): InMemoryTransactionStore {
        return InMemoryTransactionStore(maxSize)
    }

    // ---------------------------------------------------------------
    // Adding transactions
    // ---------------------------------------------------------------

    @Test
    fun addTransaction_stores_a_single_transaction() {
        val store = createStore()
        val txn = createTestTransaction(id = "txn-1")
        store.addTransaction(txn)
        assertEquals(1, store.transactions.value.size)
        assertEquals("txn-1", store.transactions.value[0].id)
    }

    @Test
    fun addTransaction_stores_multiple_transactions() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "txn-1"))
        store.addTransaction(createTestTransaction(id = "txn-2"))
        store.addTransaction(createTestTransaction(id = "txn-3"))
        assertEquals(3, store.transactions.value.size)
    }

    // ---------------------------------------------------------------
    // Ordering — newest first (reverse chronological)
    // ---------------------------------------------------------------

    @Test
    fun addTransaction_newest_transaction_appears_first() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "old", timestamp = 1000L))
        store.addTransaction(createTestTransaction(id = "new", timestamp = 2000L))
        assertEquals("new", store.transactions.value[0].id, "Newest transaction should be at index 0")
        assertEquals("old", store.transactions.value[1].id, "Older transaction should be at index 1")
    }

    @Test
    fun addTransaction_preserves_insertion_order_as_reverse_chronological() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "first"))
        store.addTransaction(createTestTransaction(id = "second"))
        store.addTransaction(createTestTransaction(id = "third"))
        // Last added appears first
        assertEquals("third", store.transactions.value[0].id)
        assertEquals("second", store.transactions.value[1].id)
        assertEquals("first", store.transactions.value[2].id)
    }

    // ---------------------------------------------------------------
    // Max size enforcement
    // ---------------------------------------------------------------

    @Test
    fun addTransaction_enforces_max_size() {
        val store = createStore(maxSize = 3)
        store.addTransaction(createTestTransaction(id = "txn-1"))
        store.addTransaction(createTestTransaction(id = "txn-2"))
        store.addTransaction(createTestTransaction(id = "txn-3"))
        store.addTransaction(createTestTransaction(id = "txn-4"))

        assertEquals(3, store.transactions.value.size, "Store should not exceed maxSize")
        // Newest should still be first; oldest should be evicted
        assertEquals("txn-4", store.transactions.value[0].id, "Newest should be first")
        assertEquals("txn-3", store.transactions.value[1].id)
        assertEquals("txn-2", store.transactions.value[2].id)
    }

    @Test
    fun addTransaction_with_max_size_one_keeps_only_latest() {
        val store = createStore(maxSize = 1)
        store.addTransaction(createTestTransaction(id = "txn-1"))
        store.addTransaction(createTestTransaction(id = "txn-2"))

        assertEquals(1, store.transactions.value.size)
        assertEquals("txn-2", store.transactions.value[0].id)
    }

    // ---------------------------------------------------------------
    // Updating transactions
    // ---------------------------------------------------------------

    @Test
    fun updateTransaction_modifies_existing_transaction_by_id() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "txn-1", responseCode = null))
        store.updateTransaction("txn-1") { it.copy(responseCode = 200, responseMessage = "OK") }

        val updated = store.transactions.value.first { it.id == "txn-1" }
        assertEquals(200, updated.responseCode)
        assertEquals("OK", updated.responseMessage)
    }

    @Test
    fun updateTransaction_does_not_affect_other_transactions() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "txn-1", responseCode = 200))
        store.addTransaction(createTestTransaction(id = "txn-2", responseCode = 404))
        store.updateTransaction("txn-1") { it.copy(responseCode = 500) }

        val txn1 = store.transactions.value.first { it.id == "txn-1" }
        val txn2 = store.transactions.value.first { it.id == "txn-2" }
        assertEquals(500, txn1.responseCode, "Updated transaction should have new code")
        assertEquals(404, txn2.responseCode, "Other transaction should be unchanged")
    }

    @Test
    fun updateTransaction_with_nonexistent_id_does_nothing() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "txn-1"))
        store.updateTransaction("nonexistent") { it.copy(responseCode = 999) }

        assertEquals(1, store.transactions.value.size)
        assertEquals("txn-1", store.transactions.value[0].id)
    }

    // ---------------------------------------------------------------
    // Clearing
    // ---------------------------------------------------------------

    @Test
    fun clear_removes_all_transactions() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "txn-1"))
        store.addTransaction(createTestTransaction(id = "txn-2"))
        store.clear()
        assertTrue(store.transactions.value.isEmpty(), "Store should be empty after clear")
    }

    @Test
    fun clear_on_empty_store_is_safe() {
        val store = createStore()
        store.clear()
        assertTrue(store.transactions.value.isEmpty())
    }

    // ---------------------------------------------------------------
    // removeOlderThan
    // ---------------------------------------------------------------

    @Test
    fun removeOlderThan_removes_transactions_before_cutoff() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "old", timestamp = 1000L))
        store.addTransaction(createTestTransaction(id = "new", timestamp = 3000L))
        store.removeOlderThan(2000L)

        assertEquals(1, store.transactions.value.size)
        assertEquals("new", store.transactions.value[0].id)
    }

    @Test
    fun removeOlderThan_keeps_transactions_at_exact_cutoff() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "exact", timestamp = 2000L))
        store.addTransaction(createTestTransaction(id = "after", timestamp = 3000L))
        store.removeOlderThan(2000L)

        assertEquals(2, store.transactions.value.size, "Transaction at exact cutoff should be kept")
    }

    @Test
    fun removeOlderThan_removes_all_when_all_are_old() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "old-1", timestamp = 100L))
        store.addTransaction(createTestTransaction(id = "old-2", timestamp = 200L))
        store.removeOlderThan(1000L)

        assertTrue(store.transactions.value.isEmpty(), "All old transactions should be removed")
    }

    @Test
    fun removeOlderThan_keeps_all_when_none_are_old() {
        val store = createStore()
        store.addTransaction(createTestTransaction(id = "new-1", timestamp = 5000L))
        store.addTransaction(createTestTransaction(id = "new-2", timestamp = 6000L))
        store.removeOlderThan(1000L)

        assertEquals(2, store.transactions.value.size, "No transactions should be removed")
    }

    // ---------------------------------------------------------------
    // Empty store behavior
    // ---------------------------------------------------------------

    @Test
    fun initial_store_is_empty() {
        val store = createStore()
        assertTrue(store.transactions.value.isEmpty(), "New store should have no transactions")
    }
}
