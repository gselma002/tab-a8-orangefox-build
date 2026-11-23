package org.recoverydev.probe

import org.junit.Assert.assertEquals
import org.junit.Test

class SnapshotFormatterTest {
    @Test
    fun blankValuesAreReportedAsUnavailable() {
        assertEquals("Unavailable", SnapshotFormatter.display(null))
        assertEquals("Unavailable", SnapshotFormatter.display(""))
        assertEquals("Unavailable", SnapshotFormatter.display("  "))
    }

    @Test
    fun byteValuesAreFormattedWithoutOverflow() {
        assertEquals("0 B", SnapshotFormatter.bytes(0L))
        assertEquals("1 B", SnapshotFormatter.bytes(1L))
        assertEquals("1.00 KiB", SnapshotFormatter.bytes(1024L))
        assertEquals("1.00 MiB", SnapshotFormatter.bytes(1024L * 1024L))
        assertEquals("1.00 TiB", SnapshotFormatter.bytes(1024L * 1024L * 1024L * 1024L))
        assertEquals("1.00 PiB", SnapshotFormatter.bytes(1L shl 50))
        assertEquals("8.00 EiB", SnapshotFormatter.bytes(Long.MAX_VALUE))
        assertEquals("Unavailable", SnapshotFormatter.bytes(null))
        assertEquals("Unavailable", SnapshotFormatter.bytes(-1L))
    }

    @Test
    fun thermalValuesAreMappedConservatively() {
        assertEquals("None", SnapshotFormatter.thermalLabel(0))
        assertEquals("Moderate", SnapshotFormatter.thermalLabel(2))
        assertEquals("Shutdown", SnapshotFormatter.thermalLabel(6))
        assertEquals("Unavailable", SnapshotFormatter.thermalLabel(null))
        assertEquals("Unknown (99)", SnapshotFormatter.thermalLabel(99))
    }
}
