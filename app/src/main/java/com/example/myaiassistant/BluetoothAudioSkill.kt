package com.example.myaiassistant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * AURIX 2.0
 * Bluetooth Audio Skill
 *
 * Provides basic information about paired Bluetooth devices.
 */
class BluetoothAudioSkill(
    private val context: Context
) : AurixCore.Skill {

    override fun canHandle(command: String): Boolean {
        val cmd = command.lowercase().trim()

        return cmd.contains("bluetooth") ||
                cmd.contains("speaker") ||
                cmd.contains("headphone") ||
                cmd.contains("earbuds")
    }

    override fun execute(command: String): String? {
        val cmd = command.lowercase().trim()

        return when {
            cmd.contains("bluetooth") &&
                    (cmd.contains("device") ||
                     cmd.contains("devices") ||
                     cmd.contains("connected") ||
                     cmd.contains("paired")) -> {
                listPairedDevices()
            }

            cmd.contains("speaker") ||
                    cmd.contains("headphone") ||
                    cmd.contains("earbuds") -> {
                listPairedDevices()
            }

            cmd.contains("bluetooth") -> {
                openBluetoothSettings()
            }

            else -> null
        }
    }

    private fun listPairedDevices(): String {

        val adapter =
            BluetoothAdapter.getDefaultAdapter()
                ?: return "Bluetooth is not available on this phone."

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S
        ) {
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Bluetooth permission is required to check your devices."
            }
        }

        return try {

            val devices: Set<BluetoothDevice> =
                adapter.bondedDevices

            if (devices.isEmpty()) {
                "I couldn't find any paired Bluetooth devices."
            } else {

                val names = devices
                    .mapNotNull { device ->
                        try {
                            device.name
                        } catch (_: SecurityException) {
                            null
                        }
                    }
                    .filter { it.isNotBlank() }

                if (names.isEmpty()) {
                    "Bluetooth devices are paired, but I couldn't read their names."
                } else {
                    "Your paired Bluetooth devices are: ${
                        names.joinToString(", ")
                    }."
                }
            }

        } catch (_: SecurityException) {
            "I don't have permission to access Bluetooth devices."
        } catch (_: Exception) {
            "I couldn't check your Bluetooth devices."
        }
    }

    private fun openBluetoothSettings(): String {

        return try {

            val intent = android.content.Intent(
                android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
            ).apply {
                addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

            context.startActivity(intent)

            "Opening Bluetooth settings."

        } catch (_: Exception) {
            "I couldn't open Bluetooth settings."
        }
    }
}
