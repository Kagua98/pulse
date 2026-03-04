package io.pulse

import io.pulse.log.LogLevel
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PulseTest {

    /**
     * Clear all stores after each test to prevent cross-test pollution.
     * The Pulse singleton persists across tests within the same process.
     */
    @AfterTest
    fun tearDown() {
        Pulse.clear()
    }

    // ---------------------------------------------------------------
    // Logging API — level correctness
    // ---------------------------------------------------------------

    @Test
    fun v_logs_with_verbose_level() {
        Pulse.v("verbose message", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertEquals(LogLevel.VERBOSE, entry.level)
        assertEquals("Tag", entry.tag)
        assertEquals("verbose message", entry.message)
    }

    @Test
    fun d_logs_with_debug_level() {
        Pulse.d("debug message", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertEquals(LogLevel.DEBUG, entry.level)
        assertEquals("debug message", entry.message)
    }

    @Test
    fun i_logs_with_info_level() {
        Pulse.i("info message", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertEquals(LogLevel.INFO, entry.level)
        assertEquals("info message", entry.message)
    }

    @Test
    fun w_logs_with_warn_level() {
        Pulse.w("warn message", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertEquals(LogLevel.WARN, entry.level)
        assertEquals("warn message", entry.message)
    }

    @Test
    fun e_logs_with_error_level() {
        Pulse.e("error message", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertEquals(LogLevel.ERROR, entry.level)
        assertEquals("error message", entry.message)
    }

    // ---------------------------------------------------------------
    // Logging API — tag and message
    // ---------------------------------------------------------------

    @Test
    fun log_preserves_tag() {
        Pulse.d("test", tag = "MyTag")
        assertEquals("MyTag", Pulse.logs.value.first().tag)
    }

    @Test
    fun log_preserves_message() {
        Pulse.i("Hello, world!", tag = "Tag")
        assertEquals("Hello, world!", Pulse.logs.value.first().message)
    }

    @Test
    fun log_entries_have_unique_ids() {
        Pulse.d("first", tag = "Tag")
        Pulse.d("second", tag = "Tag")
        val ids = Pulse.logs.value.map { it.id }.toSet()
        assertEquals(2, ids.size, "Each log entry should have a unique ID")
    }

    @Test
    fun log_entries_have_timestamps() {
        Pulse.d("timestamped", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertTrue(entry.timestamp > 0, "Timestamp should be a positive value")
    }

    // ---------------------------------------------------------------
    // Logging API — throwable
    // ---------------------------------------------------------------

    @Test
    fun w_stores_throwable_stack_trace() {
        val exception = RuntimeException("test warning")
        Pulse.w("warning with throwable", tag = "Tag", throwable = exception)
        val entry = Pulse.logs.value.first()
        assertTrue(
            entry.throwable != null && entry.throwable.contains("RuntimeException"),
            "Throwable stack trace should be stored",
        )
    }

    @Test
    fun e_stores_throwable_stack_trace() {
        val exception = IllegalStateException("bad state")
        Pulse.e("error with throwable", tag = "Tag", throwable = exception)
        val entry = Pulse.logs.value.first()
        assertTrue(
            entry.throwable != null && entry.throwable.contains("IllegalStateException"),
            "Throwable stack trace should be stored",
        )
    }

    @Test
    fun d_without_throwable_has_null_throwable_field() {
        Pulse.d("no throwable", tag = "Tag")
        val entry = Pulse.logs.value.first()
        assertEquals(null, entry.throwable, "Log without throwable should have null throwable field")
    }

    // ---------------------------------------------------------------
    // recordCrash
    // ---------------------------------------------------------------

    @Test
    fun recordCrash_stores_crash_entry() {
        val exception = NullPointerException("oops")
        Pulse.recordCrash("main", exception)
        val crashes = Pulse.crashes.value
        assertEquals(1, crashes.size, "Should have one crash recorded")
    }

    @Test
    fun recordCrash_captures_thread_name() {
        Pulse.recordCrash("worker-thread", RuntimeException("fail"))
        val crash = Pulse.crashes.value.first()
        assertEquals("worker-thread", crash.threadName)
    }

    @Test
    fun recordCrash_captures_exception_class() {
        Pulse.recordCrash("main", IllegalArgumentException("bad arg"))
        val crash = Pulse.crashes.value.first()
        assertEquals("IllegalArgumentException", crash.exceptionClass)
    }

    @Test
    fun recordCrash_captures_exception_message() {
        Pulse.recordCrash("main", RuntimeException("specific error message"))
        val crash = Pulse.crashes.value.first()
        assertEquals("specific error message", crash.message)
    }

    @Test
    fun recordCrash_captures_stack_trace() {
        val exception = RuntimeException("with stack")
        Pulse.recordCrash("main", exception)
        val crash = Pulse.crashes.value.first()
        assertTrue(crash.stackTrace.isNotEmpty(), "Stack trace should not be empty")
        assertTrue(
            crash.stackTrace.contains("RuntimeException"),
            "Stack trace should contain the exception class",
        )
    }

    @Test
    fun recordCrash_entries_have_timestamps() {
        Pulse.recordCrash("main", RuntimeException("timed"))
        val crash = Pulse.crashes.value.first()
        assertTrue(crash.timestamp > 0, "Crash timestamp should be positive")
    }

    // ---------------------------------------------------------------
    // clear() — clears all stores
    // ---------------------------------------------------------------

    @Test
    fun clear_removes_all_logs_transactions_and_crashes() {
        Pulse.d("log message", tag = "Tag")
        Pulse.recordCrash("main", RuntimeException("crash"))
        Pulse.store.addTransaction(createTestTransaction(id = "txn-clear"))

        Pulse.clear()

        assertTrue(Pulse.logs.value.isEmpty(), "Logs should be empty after clear")
        assertTrue(Pulse.crashes.value.isEmpty(), "Crashes should be empty after clear")
        assertTrue(Pulse.transactions.value.isEmpty(), "Transactions should be empty after clear")
    }

    // ---------------------------------------------------------------
    // clearNetwork() — only clears transactions
    // ---------------------------------------------------------------

    @Test
    fun clearNetwork_clears_only_transactions() {
        Pulse.d("log message", tag = "Tag")
        Pulse.recordCrash("main", RuntimeException("crash"))
        Pulse.store.addTransaction(createTestTransaction(id = "txn-clear-net"))

        Pulse.clearNetwork()

        assertTrue(Pulse.transactions.value.isEmpty(), "Transactions should be empty after clearNetwork")
        assertTrue(Pulse.logs.value.isNotEmpty(), "Logs should NOT be cleared by clearNetwork")
        assertTrue(Pulse.crashes.value.isNotEmpty(), "Crashes should NOT be cleared by clearNetwork")
    }

    // ---------------------------------------------------------------
    // clearLogs() — only clears logs
    // ---------------------------------------------------------------

    @Test
    fun clearLogs_clears_only_logs() {
        Pulse.d("log message", tag = "Tag")
        Pulse.recordCrash("main", RuntimeException("crash"))
        Pulse.store.addTransaction(createTestTransaction(id = "txn-clear-logs"))

        Pulse.clearLogs()

        assertTrue(Pulse.logs.value.isEmpty(), "Logs should be empty after clearLogs")
        assertTrue(Pulse.crashes.value.isNotEmpty(), "Crashes should NOT be cleared by clearLogs")
        assertTrue(Pulse.transactions.value.isNotEmpty(), "Transactions should NOT be cleared by clearLogs")
    }

    // ---------------------------------------------------------------
    // clearCrashes() — only clears crashes
    // ---------------------------------------------------------------

    @Test
    fun clearCrashes_clears_only_crashes() {
        Pulse.d("log message", tag = "Tag")
        Pulse.recordCrash("main", RuntimeException("crash"))
        Pulse.store.addTransaction(createTestTransaction(id = "txn-clear-crashes"))

        Pulse.clearCrashes()

        assertTrue(Pulse.crashes.value.isEmpty(), "Crashes should be empty after clearCrashes")
        assertTrue(Pulse.logs.value.isNotEmpty(), "Logs should NOT be cleared by clearCrashes")
        assertTrue(Pulse.transactions.value.isNotEmpty(), "Transactions should NOT be cleared by clearCrashes")
    }

    // ---------------------------------------------------------------
    // configure
    // ---------------------------------------------------------------

    @Test
    fun configure_updates_maxTransactions() {
        Pulse.configure {
            maxTransactions = 100
        }
        assertEquals(100, Pulse.maxTransactions, "maxTransactions should be updated by configure")

        // Restore default to not pollute other tests
        Pulse.configure {
            maxTransactions = 500
        }
    }

    @Test
    fun configure_updates_enabled_flag() {
        Pulse.configure {
            enabled = false
        }
        assertEquals(false, Pulse.enabled, "enabled should be updated by configure")

        // Restore
        Pulse.configure {
            enabled = true
        }
    }

    @Test
    fun configure_replaces_transaction_store_clearing_old_data() {
        Pulse.store.addTransaction(createTestTransaction(id = "before-configure"))
        assertTrue(Pulse.transactions.value.isNotEmpty(), "Should have a transaction before configure")

        Pulse.configure {
            maxTransactions = 200
        }

        assertTrue(
            Pulse.transactions.value.isEmpty(),
            "configure should replace the store, clearing old transactions",
        )

        // Restore default
        Pulse.configure {
            maxTransactions = 500
        }
    }

    // ---------------------------------------------------------------
    // Multiple logs accumulate
    // ---------------------------------------------------------------

    @Test
    fun multiple_logs_accumulate_in_order() {
        Pulse.d("first", tag = "Tag")
        Pulse.i("second", tag = "Tag")
        Pulse.e("third", tag = "Tag")

        val logs = Pulse.logs.value
        assertEquals(3, logs.size, "Should have three log entries")
        // Newest first (prepended)
        assertEquals("third", logs[0].message)
        assertEquals("second", logs[1].message)
        assertEquals("first", logs[2].message)
    }
}
