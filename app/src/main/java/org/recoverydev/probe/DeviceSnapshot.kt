package org.recoverydev.probe

data class DeviceSnapshot(
    val manufacturer: String,
    val model: String,
    val device: String,
    val product: String,
    val board: String,
    val socManufacturer: String,
    val socModel: String,
    val androidRelease: String,
    val sdkInt: Int,
    val securityPatch: String,
    val buildId: String,
    val fingerprint: String,
    val supportedAbis: List<String>,
    val kernelRelease: String,
    val dataAvailableBytes: Long?,
    val memoryClassMb: Int?,
    val largeMemoryClassMb: Int?,
    val availableMemoryBytes: Long?,
    val totalMemoryBytes: Long?,
    val lowMemory: Boolean?,
    val thermalStatus: String
)

object SnapshotFormatter {
    fun display(value: String?): String =
        value?.takeIf { it.isNotBlank() } ?: "Unavailable"

    fun bytes(value: Long?): String {
        if (value == null || value < 0L) return "Unavailable"
        if (value == 0L) return "0 B"

        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        var amount = value.toDouble()
        var unit = 0
        while (amount >= 1024.0 && unit < units.lastIndex) {
            amount /= 1024.0
            unit++
        }
        return if (unit == 0) "${amount.toLong()} ${units[unit]}"
        else String.format(java.util.Locale.ROOT, "%.2f %s", amount, units[unit])
    }

    fun thermalLabel(status: Int?): String = when (status) {
        null -> "Unavailable"
        0 -> "None"
        1 -> "Light"
        2 -> "Moderate"
        3 -> "Severe"
        4 -> "Critical"
        5 -> "Emergency"
        6 -> "Shutdown"
        else -> "Unknown ($status)"
    }
}
