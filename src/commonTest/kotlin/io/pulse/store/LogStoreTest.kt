package io.pulse.store

import io.pulse.createTestLogEntry
import io.pulse.model.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogStoreTest {

    private fun createStore(maxSize: Int = 2000): LogStore {
        return LogStore(maxSize)
    }

    // ---------------------------------------------------------------
    // Adding logs
    // ---------------------------------------------------------------

    @Test
    fun add_stores_a_single_log_entry() {
        val store = createStore()
        store.add(createTestLogEntry(id = "log-1"))
        assertEquals(1, store.logs.value.size)
        assertEquals("log-1", store.logs.value[0].id)
    }

    @Test
    fun add_stores_multiple_log_entries() {
        val store = createStore()
        store.add(createTestLogEntry(id = "log-1"))
        store.add(createTestLogEntry(id = "log-2"))
        store.add(createTestLogEntry(id = "log-3"))
        assertEquals(3, store.logs.value.size)
    }

    @Test
    fun add_newest_entry_appears_first() {
        val store = createStore()
        store.add(createTestLogEntry(id = "old"))
        store.add(createTestLogEntry(id = "new"))
        assertEquals("new", store.logs.value[0].id, "Newest log should be at index 0")
        assertEquals("old", store.logs.value[1].id, "Older log should be at index 1")
    }

    // ---------------------------------------------------------------
    // Max size enforcement
    // ---------------------------------------------------------------

    @Test
    fun add_enforces_max_size() {
        val store = createStore(maxSize = 3)
        store.add(createTestLogEntry(id = "log-1"))
        store.add(createTestLogEntry(id = "log-2"))
        store.add(createTestLogEntry(id = "log-3"))
        store.add(createTestLogEntry(id = "log-4"))

        assertEquals(3, store.logs.value.size, "Store should not exceed maxSize")
        assertEquals("log-4", store.logs.value[0].id, "Newest should be first")
        assertEquals("log-3", store.logs.value[1].id)
        assertEquals("log-2", store.logs.value[2].id)
    }

    @Test
    fun add_with_max_size_one_keeps_only_latest() {
        val store = createStore(maxSize = 1)
        store.add(createTestLogEntry(id = "log-1"))
        store.add(createTestLogEntry(id = "log-2"))

        assertEquals(1, store.logs.value.size)
        assertEquals("log-2", store.logs.value[0].id)
    }

    // ---------------------------------------------------------------
    // Clearing
    // ---------------------------------------------------------------

    @Test
    fun clear_removes_all_logs() {
        val store = createStore()
        store.add(createTestLogEntry(id = "log-1"))
        store.add(createTestLogEntry(id = "log-2"))
        store.clear()
        assertTrue(store.logs.value.isEmpty(), "Store should be empty after clear")
    }

    @Test
    fun clear_on_empty_store_is_safe() {
        val store = createStore()
        store.clear()
        assertTrue(store.logs.value.isEmpty())
    }

    // ---------------------------------------------------------------
    // removeOlderThan
    // ---------------------------------------------------------------

    @Test
    fun removeOlderThan_removes_entries_before_cutoff() {
        val store = createStore()
        store.add(createTestLogEntry(id = "old", timestamp = 1000L))
        store.add(createTestLogEntry(id = "new", timestamp = 3000L))
        store.removeOlderThan(2000L)

        assertEquals(1, store.logs.value.size)
        assertEquals("new", store.logs.value[0].id)
    }

    @Test
    fun removeOlderThan_keeps_entries_at_exact_cutoff() {
        val store = createStore()
        store.add(createTestLogEntry(id = "exact", timestamp = 2000L))
        store.add(createTestLogEntry(id = "after", timestamp = 3000L))
        store.removeOlderThan(2000L)

        assertEquals(2, store.logs.value.size, "Entry at exact cutoff should be kept")
    }

    @Test
    fun removeOlderThan_removes_all_when_all_are_old() {
        val store = createStore()
        store.add(createTestLogEntry(id = "old-1", timestamp = 100L))
        store.add(createTestLogEntry(id = "old-2", timestamp = 200L))
        store.removeOlderThan(1000L)

        assertTrue(store.logs.value.isEmpty(), "All old entries should be removed")
    }

    @Test
    fun removeOlderThan_keeps_all_when_none_are_old() {
        val store = createStore()
        store.add(createTestLogEntry(id = "new-1", timestamp = 5000L))
        store.add(createTestLogEntry(id = "new-2", timestamp = 6000L))
        store.removeOlderThan(1000L)

        assertEquals(2, store.logs.value.size, "No entries should be removed")
    }

    // ---------------------------------------------------------------
    // Initial state
    // ---------------------------------------------------------------

    @Test
    fun initial_store_is_empty() {
        val store = createStore()
        assertTrue(store.logs.value.isEmpty(), "New store should have no logs")
    }

    // ---------------------------------------------------------------
    // Log entry data integrity
    // ---------------------------------------------------------------

    @Test
    fun add_preserves_log_entry_fields() {
        val store = createStore()
        val entry = createTestLogEntry(
            id = "log-integrity",
            level = LogLevel.WARN,
            tag = "MyTag",
            message = "Warning message",
            throwable = "java.lang.Exception: oops",
            timestamp = 1_700_000_000_000L,
        )
        store.add(entry)

        val stored = store.logs.value[0]
        assertEquals("log-integrity", stored.id)
        assertEquals(LogLevel.WARN, stored.level)
        assertEquals("MyTag", stored.tag)
        assertEquals("Warning message", stored.message)
        assertEquals("java.lang.Exception: oops", stored.throwable)
        assertEquals(1_700_000_000_000L, stored.timestamp)
    }
}
