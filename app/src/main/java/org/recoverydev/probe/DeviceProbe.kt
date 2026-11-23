package org.recoverydev.probe

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs

object DeviceProbe {
    fun collect(context: Context): DeviceSnapshot {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val availableBytes = runCatching {
            StatFs(Environment.getDataDirectory().absolutePath).availableBytes
        }.getOrNull()

        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val powerManager =
                    context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.currentThermalStatus
            }.getOrNull()
        } else {
            null
        }

        return DeviceSnapshot(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            board = Build.BOARD,
            socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MANUFACTURER
            } else {
                ""
            },
            socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL
            } else {
                ""
            },
            androidRelease = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH,
            buildId = Build.ID,
            fingerprint = Build.FINGERPRINT,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            kernelRelease = System.getProperty("os.version").orEmpty(),
            dataAvailableBytes = availableBytes,
            memoryClassMb = activityManager?.memoryClass,
            largeMemoryClassMb = activityManager?.largeMemoryClass,
            availableMemoryBytes = if (memoryInfo.totalMem > 0L) memoryInfo.availMem else null,
            totalMemoryBytes = memoryInfo.totalMem.takeIf { it > 0L },
            lowMemory = activityManager?.let { memoryInfo.lowMemory },
            thermalStatus = SnapshotFormatter.thermalLabel(thermal)
        )
    }
}
