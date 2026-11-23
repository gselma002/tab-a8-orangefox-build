package org.recoverydev.probe

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var report: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "Read-only device environment"
            textSize = 22f
        }
        content.addView(title)

        val refresh = Button(this).apply {
            text = "Refresh device information"
            setOnClickListener { refreshReport() }
        }
        content.addView(
            refresh,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        report = TextView(this).apply {
            textSize = 14f
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply {
            addView(report)
        }
        content.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(content)
        refreshReport()
    }

    private fun refreshReport() {
        val snapshot = runCatching { DeviceProbe.collect(this) }.getOrNull()
        report.text = snapshot?.let(::formatReport)
            ?: "Device inspection failed. No device settings or partitions were changed."
    }

    private fun formatReport(s: DeviceSnapshot): String = buildString {
        appendLine("IDENTITY")
        appendLine("Manufacturer: ${SnapshotFormatter.display(s.manufacturer)}")
        appendLine("Model: ${SnapshotFormatter.display(s.model)}")
        appendLine("Device: ${SnapshotFormatter.display(s.device)}")
        appendLine("Product: ${SnapshotFormatter.display(s.product)}")
        appendLine("Board: ${SnapshotFormatter.display(s.board)}")
        appendLine("SoC manufacturer: ${SnapshotFormatter.display(s.socManufacturer)}")
        appendLine("SoC model: ${SnapshotFormatter.display(s.socModel)}")
        appendLine()
        appendLine("ANDROID BUILD")
        appendLine("Release: ${SnapshotFormatter.display(s.androidRelease)}")
        appendLine("API level: ${s.sdkInt}")
        appendLine("Security patch: ${SnapshotFormatter.display(s.securityPatch)}")
        appendLine("Build ID: ${SnapshotFormatter.display(s.buildId)}")
        appendLine("Fingerprint: ${SnapshotFormatter.display(s.fingerprint)}")
        appendLine("Kernel: ${SnapshotFormatter.display(s.kernelRelease)}")
        appendLine("Supported ABIs: ${s.supportedAbis.joinToString().ifBlank { "Unavailable" }}")
        appendLine()
        appendLine("AVAILABLE RESOURCES")
        appendLine("Free space on /data: ${SnapshotFormatter.bytes(s.dataAvailableBytes)}")
        appendLine("App memory class: ${s.memoryClassMb?.let { "$it MiB" } ?: "Unavailable"}")
        appendLine("Large app memory class: ${s.largeMemoryClassMb?.let { "$it MiB" } ?: "Unavailable"}")
        appendLine("Available RAM: ${SnapshotFormatter.bytes(s.availableMemoryBytes)}")
        appendLine("Total RAM: ${SnapshotFormatter.bytes(s.totalMemoryBytes)}")
        appendLine("System reports low memory: ${s.lowMemory?.toString() ?: "Unavailable"}")
        appendLine("Thermal status: ${s.thermalStatus}")
        appendLine()
        appendLine("LIMITATIONS")
        appendLine("This probe does not inspect partition maps, bootloader state, root access, AVB configuration, encryption, or touchscreen hardware.")
        appendLine("It does not execute shell commands, request special permissions, or write to device partitions.")
        appendLine("Build fingerprint and device properties are shown locally and are not uploaded.")
    }
}
