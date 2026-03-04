package io.pulse.store

import io.pulse.createTestCrashEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrashStoreTest {

    private fun createStore(maxSize: Int = 50): CrashStore {
        return CrashStore(maxSize)
    }

    // ---------------------------------------------------------------
    // Adding crashes
    // ---------------------------------------------------------------

    @Test
    fun add_stores_a_single_crash_entry() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "crash-1"))
        assertEquals(1, store.crashes.value.size)
        assertEquals("crash-1", store.crashes.value[0].id)
    }

    @Test
    fun add_stores_multiple_crash_entries() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "crash-1"))
        store.add(createTestCrashEntry(id = "crash-2"))
        store.add(createTestCrashEntry(id = "crash-3"))
        assertEquals(3, store.crashes.value.size)
    }

    @Test
    fun add_newest_entry_appears_first() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "old"))
        store.add(createTestCrashEntry(id = "new"))
        assertEquals("new", store.crashes.value[0].id, "Newest crash should be at index 0")
        assertEquals("old", store.crashes.value[1].id, "Older crash should be at index 1")
    }

    // ---------------------------------------------------------------
    // Max size enforcement
    // ---------------------------------------------------------------

    @Test
    fun add_enforces_max_size() {
        val store = createStore(maxSize = 3)
        store.add(createTestCrashEntry(id = "crash-1"))
        store.add(createTestCrashEntry(id = "crash-2"))
        store.add(createTestCrashEntry(id = "crash-3"))
        store.add(createTestCrashEntry(id = "crash-4"))

        assertEquals(3, store.crashes.value.size, "Store should not exceed maxSize")
        assertEquals("crash-4", store.crashes.value[0].id, "Newest should be first")
        assertEquals("crash-3", store.crashes.value[1].id)
        assertEquals("crash-2", store.crashes.value[2].id)
    }

    @Test
    fun add_with_max_size_one_keeps_only_latest() {
        val store = createStore(maxSize = 1)
        store.add(createTestCrashEntry(id = "crash-1"))
        store.add(createTestCrashEntry(id = "crash-2"))

        assertEquals(1, store.crashes.value.size)
        assertEquals("crash-2", store.crashes.value[0].id)
    }

    // ---------------------------------------------------------------
    // Clearing
    // ---------------------------------------------------------------

    @Test
    fun clear_removes_all_crashes() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "crash-1"))
        store.add(createTestCrashEntry(id = "crash-2"))
        store.clear()
        assertTrue(store.crashes.value.isEmpty(), "Store should be empty after clear")
    }

    @Test
    fun clear_on_empty_store_is_safe() {
        val store = createStore()
        store.clear()
        assertTrue(store.crashes.value.isEmpty())
    }

    // ---------------------------------------------------------------
    // removeOlderThan
    // ---------------------------------------------------------------

    @Test
    fun removeOlderThan_removes_entries_before_cutoff() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "old", timestamp = 1000L))
        store.add(createTestCrashEntry(id = "new", timestamp = 3000L))
        store.removeOlderThan(2000L)

        assertEquals(1, store.crashes.value.size)
        assertEquals("new", store.crashes.value[0].id)
    }

    @Test
    fun removeOlderThan_keeps_entries_at_exact_cutoff() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "exact", timestamp = 2000L))
        store.add(createTestCrashEntry(id = "after", timestamp = 3000L))
        store.removeOlderThan(2000L)

        assertEquals(2, store.crashes.value.size, "Entry at exact cutoff should be kept")
    }

    @Test
    fun removeOlderThan_removes_all_when_all_are_old() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "old-1", timestamp = 100L))
        store.add(createTestCrashEntry(id = "old-2", timestamp = 200L))
        store.removeOlderThan(1000L)

        assertTrue(store.crashes.value.isEmpty(), "All old entries should be removed")
    }

    @Test
    fun removeOlderThan_keeps_all_when_none_are_old() {
        val store = createStore()
        store.add(createTestCrashEntry(id = "new-1", timestamp = 5000L))
        store.add(createTestCrashEntry(id = "new-2", timestamp = 6000L))
        store.removeOlderThan(1000L)

        assertEquals(2, store.crashes.value.size, "No entries should be removed")
    }

    // ---------------------------------------------------------------
    // Initial state
    // ---------------------------------------------------------------

    @Test
    fun initial_store_is_empty() {
        val store = createStore()
        assertTrue(store.crashes.value.isEmpty(), "New store should have no crashes")
    }

    // ---------------------------------------------------------------
    // Crash entry data integrity
    // ---------------------------------------------------------------

    @Test
    fun add_preserves_crash_entry_fields() {
        val store = createStore()
        val entry = createTestCrashEntry(
            id = "crash-integrity",
            threadName = "worker-thread",
            exceptionClass = "IllegalStateException",
            message = "Invalid state",
            stackTrace = "java.lang.IllegalStateException: Invalid state\n\tat Test.run(Test.kt:5)",
            timestamp = 1_700_000_000_000L,
        )
        store.add(entry)

        val stored = store.crashes.value[0]
        assertEquals("crash-integrity", stored.id)
        assertEquals("worker-thread", stored.threadName)
        assertEquals("IllegalStateException", stored.exceptionClass)
        assertEquals("Invalid state", stored.message)
        assertEquals(
            "java.lang.IllegalStateException: Invalid state\n\tat Test.run(Test.kt:5)",
            stored.stackTrace,
        )
        assertEquals(1_700_000_000_000L, stored.timestamp)
    }
}
