package io.pulse.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormattersTest {

    // ---------------------------------------------------------------
    // formatBytes
    // ---------------------------------------------------------------

    @Test
    fun formatBytes_zero_bytes() {
        assertEquals("0 B", formatBytes(0))
    }

    @Test
    fun formatBytes_negative_bytes_returns_zero() {
        assertEquals("0 B", formatBytes(-1))
    }

    @Test
    fun formatBytes_small_byte_values() {
        assertEquals("1 B", formatBytes(1))
        assertEquals("512 B", formatBytes(512))
        assertEquals("1023 B", formatBytes(1023))
    }

    @Test
    fun formatBytes_kilobyte_range() {
        val result = formatBytes(1024)
        assertTrue(result.contains("KB"), "1024 bytes should format to KB, got: $result")
    }

    @Test
    fun formatBytes_megabyte_range() {
        val result = formatBytes(1024L * 1024)
        assertTrue(result.contains("MB"), "1 MB should format with MB, got: $result")
    }

    @Test
    fun formatBytes_gigabyte_range() {
        val result = formatBytes(1024L * 1024 * 1024)
        assertTrue(result.contains("GB"), "1 GB should format with GB, got: $result")
    }

    @Test
    fun formatBytes_fractional_kilobytes() {
        // 1536 bytes = 1.5 KB
        val result = formatBytes(1536)
        assertTrue(result.contains("KB"), "1536 bytes should be in KB range, got: $result")
        assertTrue(result.contains("1.5"), "1536 bytes should show 1.5, got: $result")
    }

    // ---------------------------------------------------------------
    // formatDuration
    // ---------------------------------------------------------------

    @Test
    fun formatDuration_zero_ms() {
        assertEquals("0 ms", formatDuration(0))
    }

    @Test
    fun formatDuration_negative_ms_returns_zero() {
        assertEquals("0 ms", formatDuration(-5))
    }

    @Test
    fun formatDuration_milliseconds_below_one_second() {
        assertEquals("1 ms", formatDuration(1))
        assertEquals("500 ms", formatDuration(500))
        assertEquals("999 ms", formatDuration(999))
    }

    @Test
    fun formatDuration_seconds_range() {
        val result = formatDuration(1000)
        assertTrue(result.contains("s"), "1000ms should be in seconds, got: $result")
        assertFalse(result.contains("ms"), "Should not contain 'ms' for seconds, got: $result")
    }

    @Test
    fun formatDuration_seconds_with_decimals() {
        val result = formatDuration(2500)
        assertTrue(result.contains("2.5"), "2500ms should be 2.5s, got: $result")
    }

    @Test
    fun formatDuration_minutes_range() {
        val result = formatDuration(60_000)
        assertTrue(result.contains("m"), "60000ms should contain minutes, got: $result")
    }

    @Test
    fun formatDuration_minutes_and_seconds() {
        val result = formatDuration(90_000) // 1m 30s
        assertTrue(result.contains("1m"), "Should contain 1m, got: $result")
        assertTrue(result.contains("30s"), "Should contain 30s, got: $result")
    }

    @Test
    fun formatDuration_multiple_minutes() {
        val result = formatDuration(150_000) // 2m 30s
        assertTrue(result.contains("2m"), "Should contain 2m, got: $result")
        assertTrue(result.contains("30s"), "Should contain 30s, got: $result")
    }

    // ---------------------------------------------------------------
    // formatTimestamp
    // ---------------------------------------------------------------

    @Test
    fun formatTimestamp_epoch_zero_returns_midnight() {
        assertEquals("00:00:00", formatTimestamp(0))
    }

    @Test
    fun formatTimestamp_produces_colon_separated_time() {
        val result = formatTimestamp(1_700_000_000_000L)
        // Should be in HH:MM:SS format
        val parts = result.split(":")
        assertEquals(3, parts.size, "Timestamp should have 3 colon-separated parts")
        parts.forEach { part ->
            assertEquals(2, part.length, "Each time part should be zero-padded to 2 digits")
        }
    }

    @Test
    fun formatTimestamp_pads_single_digits() {
        // 3661 seconds = 01:01:01
        val result = formatTimestamp(3_661_000L)
        assertEquals("01:01:01", result)
    }

    @Test
    fun formatTimestamp_handles_exact_hour() {
        // 3600 seconds = 01:00:00
        val result = formatTimestamp(3_600_000L)
        assertEquals("01:00:00", result)
    }

    // ---------------------------------------------------------------
    // prettyPrintJson
    // ---------------------------------------------------------------

    @Test
    fun prettyPrintJson_formats_simple_object() {
        val input = """{"name":"Alice","age":30}"""
        val result = prettyPrintJson(input)
        assertTrue(result.contains("\"name\""), "Should contain 'name' key")
        assertTrue(result.contains("\"Alice\""), "Should contain 'Alice' value")
        assertTrue(result.contains("\n"), "Pretty-printed JSON should contain newlines")
    }

    @Test
    fun prettyPrintJson_formats_nested_object() {
        val input = """{"user":{"name":"Bob"}}"""
        val result = prettyPrintJson(input)
        // Should have indentation with multiple levels
        val lines = result.lines()
        assertTrue(lines.size > 3, "Nested object should produce multiple lines")
    }

    @Test
    fun prettyPrintJson_formats_array() {
        val input = """[1,2,3]"""
        val result = prettyPrintJson(input)
        assertTrue(result.contains("["), "Should contain opening bracket")
        assertTrue(result.contains("]"), "Should contain closing bracket")
        assertTrue(result.contains("\n"), "Should have newlines")
    }

    @Test
    fun prettyPrintJson_returns_non_json_string_as_is() {
        val input = "just plain text"
        val result = prettyPrintJson(input)
        assertEquals(input, result, "Non-JSON input should be returned unchanged")
    }

    @Test
    fun prettyPrintJson_returns_blank_string_as_is() {
        assertEquals("", prettyPrintJson(""))
        assertEquals("  ", prettyPrintJson("  "))
    }

    @Test
    fun prettyPrintJson_handles_strings_with_escaped_characters() {
        val input = """{"msg":"hello \"world\""}"""
        val result = prettyPrintJson(input)
        assertTrue(result.contains("hello \\\"world\\\""), "Escaped quotes should be preserved")
    }

    @Test
    fun prettyPrintJson_handles_colons_inside_string_values() {
        val input = """{"url":"https://example.com"}"""
        val result = prettyPrintJson(input)
        assertTrue(result.contains("https://example.com"), "URL inside string should be preserved")
    }
}
