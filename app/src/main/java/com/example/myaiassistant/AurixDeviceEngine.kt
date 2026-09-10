package com.example.myaiassistant

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.util.Locale

object AurixDeviceEngine {

    fun answer(
        context: Context,
        command: String
    ): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) {
            return null
        }

        // =========================================================
        // BATTERY PERCENTAGE
        // =========================================================

        if (
            c.contains("battery percentage") ||
            c.contains("battery percent") ||
            c.contains("battery kitna") ||
            c.contains("battery kitni") ||
            c.contains("battery level") ||
            c.contains("battery status") ||
            c == "battery"
        ) {
            val battery = getBatteryInfo(context)

            return "Boss, battery ${battery.percent}% hai."
        }

        // =========================================================
        // CHARGING STATUS
        // =========================================================

        if (
            c.contains("charging hai") ||
            c.contains("charging status") ||
            c.contains("phone charge ho") ||
            c.contains("phone charging") ||
            c.contains("charger laga") ||
            c.contains("charger connected")
        ) {
            val battery = getBatteryInfo(context)

            return if (battery.isCharging) {
                "Haan Boss, phone abhi charging par hai."
            } else {
                "Nahi Boss, phone abhi charging par nahi hai."
            }
        }

        // =========================================================
        // PHONE MODEL
        // =========================================================

        if (
            c.contains("phone model") ||
            c.contains("mobile model") ||
            c.contains("mera phone") ||
            c.contains("which phone") ||
            c.contains("kaunsa phone") ||
            c.contains("kaun sa phone") ||
            c.contains("device model")
        ) {
            return "Boss, aapka device ${Build.MODEL} hai."
        }

        // =========================================================
        // MANUFACTURER
        // =========================================================

        if (
            c.contains("phone company") ||
            c.contains("mobile company") ||
            c.contains("phone manufacturer") ||
            c.contains("manufacturer") ||
            c.contains("kis company ka phone")
        ) {
            return "Boss, aapke phone ka manufacturer ${Build.MANUFACTURER} hai."
        }

        // =========================================================
        // ANDROID VERSION
        // =========================================================

        if (
            c.contains("android version") ||
            c.contains("android kitna") ||
            c.contains("android ka version") ||
            c.contains("which android")
        ) {
            return "Boss, aapke phone mein Android ${Build.VERSION.RELEASE} hai."
        }

        // =========================================================
        // API LEVEL
        // =========================================================

        if (
            c.contains("android api") ||
            c.contains("api level") ||
            c.contains("sdk version")
        ) {
            return "Boss, Android API level ${Build.VERSION.SDK_INT} hai."
        }

        // =========================================================
        // RAM
        // =========================================================

        if (
            c.contains("ram kitni") ||
            c.contains("ram kitna") ||
            c.contains("ram") ||
            c.contains("memory kitni") ||
            c.contains("memory kitna")
        ) {

            val ram = getRamInfo(context)

            return "Boss, total RAM ${ram.totalGb} GB hai aur approximately ${ram.availableGb} GB available hai."
        }

        // =========================================================
        // STORAGE
        // =========================================================

        if (
            c.contains("storage kitna") ||
            c.contains("storage kitni") ||
            c.contains("internal storage") ||
            c.contains("phone storage") ||
            c.contains("memory storage") ||
            c.contains("storage")
        ) {

            val storage = getStorageInfo()

            return "Boss, total storage ${storage.totalGb} GB hai aur approximately ${storage.freeGb} GB free hai."
        }

        // =========================================================
        // FREE STORAGE
        // =========================================================

        if (
            c.contains("free storage") ||
            c.contains("kitni storage free") ||
            c.contains("storage free kitni") ||
            c.contains("kitna space free")
        ) {

            val storage = getStorageInfo()

            return "Boss, approximately ${storage.freeGb} GB storage free hai."
        }

        // =========================================================
        // USED STORAGE
        // =========================================================

        if (
            c.contains("storage used") ||
            c.contains("kitni storage use") ||
            c.contains("storage kitni used") ||
            c.contains("kitna space use")
        ) {

            val storage = getStorageInfo()

            return "Boss, approximately ${storage.usedGb} GB storage use ho rahi hai."
        }

        // =========================================================
        // BATTERY TEMPERATURE
        // =========================================================

        if (
            c.contains("battery temperature") ||
            c.contains("battery temp") ||
            c.contains("battery garam") ||
            c.contains("battery heat")
        ) {

            val battery = getBatteryInfo(context)

            val temperature =
                battery.temperature / 10.0

            return "Boss, battery temperature approximately ${formatNumber(temperature)} degree Celsius hai."
        }

        // =========================================================
        // BATTERY HEALTH
        // =========================================================

        if (
            c.contains("battery health") ||
            c.contains("battery ki health") ||
            c.contains("battery condition")
        ) {

            val battery = getBatteryInfo(context)

            val health = when (battery.health) {

                BatteryManager.BATTERY_HEALTH_GOOD ->
                    "Good"

                BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                    "Overheat"

                BatteryManager.BATTERY_HEALTH_DEAD ->
                    "Dead"

                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                    "Over voltage"

                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE ->
                    "Unspecified failure"

                BatteryManager.BATTERY_HEALTH_COLD ->
                    "Cold"

                else ->
                    "Unknown"
            }

            return "Boss, battery health status $health hai."
        }

        // =========================================================
        // CHARGING SOURCE
        // =========================================================

        if (
            c.contains("charging source") ||
            c.contains("kis se charge") ||
            c.contains("charger type") ||
            c.contains("charge kis se")
        ) {

            val battery = getBatteryInfo(context)

            val source = when (battery.plugged) {

                BatteryManager.BATTERY_PLUGGED_USB ->
                    "USB"

                BatteryManager.BATTERY_PLUGGED_AC ->
                    "AC charger"

                BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                    "Wireless charger"

                else ->
                    "Unknown"
            }

            return if (battery.isCharging) {
                "Boss, phone $source se charging ho raha hai."
            } else {
                "Boss, phone abhi charging par nahi hai."
            }
        }

        // =========================================================
        // SCREEN INFORMATION
        // =========================================================

        if (
            c.contains("screen resolution") ||
            c.contains("display resolution") ||
            c.contains("screen size") ||
            c.contains("display size")
        ) {

            val metrics =
                context.resources.displayMetrics

            return "Boss, screen resolution ${metrics.widthPixels} by ${metrics.heightPixels} pixels hai."
        }

        // =========================================================
        // DEVICE INFORMATION
        // =========================================================

        if (
            c.contains("device information") ||
            c.contains("device info") ||
            c.contains("phone information") ||
            c.contains("mobile information") ||
            c.contains("about my phone")
        ) {

            val battery = getBatteryInfo(context)
            val ram = getRamInfo(context)
            val storage = getStorageInfo()

            return """
                Boss, device information:
                Model ${Build.MODEL}.
                Manufacturer ${Build.MANUFACTURER}.
                Android ${Build.VERSION.RELEASE}.
                Battery ${battery.percent}%.
                RAM ${ram.totalGb} GB.
                Storage ${storage.totalGb} GB.
            """.trimIndent()
        }

        return null
    }

    // =========================================================
    // BATTERY INFO
    // =========================================================

    private data class BatteryInfo(
        val percent: Int,
        val isCharging: Boolean,
        val plugged: Int,
        val temperature: Int,
        val health: Int
    )

    private fun getBatteryInfo(
        context: Context
    ): BatteryInfo {

        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        if (intent == null) {
            return BatteryInfo(
                percent = 0,
                isCharging = false,
                plugged = 0,
                temperature = 0,
                health = BatteryManager.BATTERY_HEALTH_UNKNOWN
            )
        }

        val level =
            intent.getIntExtra(
                BatteryManager.EXTRA_LEVEL,
                0
            )

        val scale =
            intent.getIntExtra(
                BatteryManager.EXTRA_SCALE,
                100
            )

        val percent =
            if (scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100f).toInt()
            } else {
                0
            }

        val status =
            intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )

        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

        val plugged =
            intent.getIntExtra(
                BatteryManager.EXTRA_PLUGGED,
                0
            )

        val temperature =
            intent.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                0
            )

        val health =
            intent.getIntExtra(
                BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN
            )

        return BatteryInfo(
            percent = percent,
            isCharging = isCharging,
            plugged = plugged,
            temperature = temperature,
            health = health
        )
    }

    // =========================================================
    // RAM INFO
    // =========================================================

    private data class RamInfo(
        val totalGb: String,
        val availableGb: String
    )

    private fun getRamInfo(
        context: Context
    ): RamInfo {

        val activityManager =
            context.getSystemService(
                android.content.Context.ACTIVITY_SERVICE
            ) as android.app.ActivityManager

        val memoryInfo =
            android.app.ActivityManager.MemoryInfo()

        activityManager.getMemoryInfo(memoryInfo)

        val totalGb =
            bytesToGb(memoryInfo.totalMem)

        val availableGb =
            bytesToGb(memoryInfo.availMem)

        return RamInfo(
            totalGb = totalGb,
            availableGb = availableGb
        )
    }

    // =========================================================
    // STORAGE INFO
    // =========================================================

    private data class StorageInfo(
        val totalGb: String,
        val freeGb: String,
        val usedGb: String
    )

    private fun getStorageInfo(): StorageInfo {

        val path =
            Environment.getDataDirectory()

        val stat =
            StatFs(path.path)

        val blockSize =
            stat.blockSizeLong

        val totalBytes =
            stat.blockCountLong * blockSize

        val freeBytes =
            stat.availableBlocksLong * blockSize

        val usedBytes =
            totalBytes - freeBytes

        return StorageInfo(
            totalGb = bytesToGb(totalBytes),
            freeGb = bytesToGb(freeBytes),
            usedGb = bytesToGb(usedBytes)
        )
    }

    // =========================================================
    // BYTES TO GB
    // =========================================================

    private fun bytesToGb(
        bytes: Long
    ): String {

        val gb =
            bytes.toDouble() /
                    (1024.0 * 1024.0 * 1024.0)

        return formatNumber(gb)
    }

    // =========================================================
    // NUMBER FORMAT
    // =========================================================

    private fun formatNumber(
        value: Double
    ): String {

        return if (
            value == value.toLong().toDouble()
        ) {

            value.toLong().toString()

        } else {

            String.format(
                Locale.US,
                "%.2f",
                value
            )
        }
    }
}
