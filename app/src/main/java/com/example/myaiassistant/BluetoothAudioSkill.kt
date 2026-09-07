package com.example.myaiassistant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.view.KeyEvent
import androidx.core.content.ContextCompat

/**
 * AURIX 2.0
 * Bluetooth Audio Skill
 *
 * Bluetooth device information + basic media controls.
 */
class BluetoothAudioSkill(
    private val context: Context
) : AurixCore.Skill {

    override fun canHandle(command: String): Boolean {
        val cmd = command.lowercase().trim()

        return cmd.contains("bluetooth") ||
                cmd.contains("speaker") ||
                cmd.contains("headphone") ||
                cmd.contains("earbuds") ||
                cmd.contains("play music") ||
                cmd.contains("resume music") ||
                cmd.contains("pause music") ||
                cmd.contains("stop music") ||
                cmd.contains("next song") ||
                cmd.contains("next track") ||
                cmd.contains("previous song") ||
                cmd.contains("previous track") ||
                cmd.contains("music chalao") ||
                cmd.contains("music play karo")
    }

    override fun execute(command: String): String? {
        val cmd = command.lowercase().trim()

        return when {

            // -------------------------------------------------
            // PLAY / RESUME MUSIC
            // -------------------------------------------------

            cmd.contains("play music") ||
                    cmd.contains("resume music") ||
                    cmd.contains("music chalao") ||
                    cmd.contains("music play karo") -> {

                sendMediaKey(
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            }

            // -------------------------------------------------
            // PAUSE MUSIC
            // -------------------------------------------------

            cmd.contains("pause music") -> {

                sendMediaKey(
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            }

            // -------------------------------------------------
            // STOP MUSIC
            // -------------------------------------------------

            cmd.contains("stop music") -> {

                sendMediaKey(
                    KeyEvent.KEYCODE_MEDIA_STOP
                )
            }

            // -------------------------------------------------
            // NEXT TRACK
            // -------------------------------------------------

            cmd.contains("next song") ||
                    cmd.contains("next track") -> {

                sendMediaKey(
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            }

            // -------------------------------------------------
            // PREVIOUS TRACK
            // -------------------------------------------------

            cmd.contains("previous song") ||
                    cmd.contains("previous track") -> {

                sendMediaKey(
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            }

            // -------------------------------------------------
            // BLUETOOTH DEVICES
            // -------------------------------------------------

            cmd.contains("bluetooth") &&
                    (
                            cmd.contains("device") ||
                                    cmd.contains("devices") ||
                                    cmd.contains("connected") ||
                                    cmd.contains("paired")
                            ) -> {

                listPairedDevices()
            }

            // -------------------------------------------------
            // SPEAKER / HEADPHONE / EARBUDS
            // -------------------------------------------------

            cmd.contains("speaker") ||
                    cmd.contains("headphone") ||
                    cmd.contains("earbuds") -> {

                listPairedDevices()
            }

            // -------------------------------------------------
            // OPEN BLUETOOTH SETTINGS
            // -------------------------------------------------

            cmd.contains("bluetooth") -> {

                openBluetoothSettings()
            }

            else -> null
        }
    }

    // ---------------------------------------------------------
    // LIST PAIRED BLUETOOTH DEVICES
    // ---------------------------------------------------------

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
                    .filter {
                        it.isNotBlank()
                    }

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

    // ---------------------------------------------------------
    // OPEN BLUETOOTH SETTINGS
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // MEDIA CONTROL
    // ---------------------------------------------------------

    private fun sendMediaKey(keyCode: Int): String {

        return try {

            val audioManager =
                context.getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            audioManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    keyCode
                )
            )

            audioManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    keyCode
                )
            )

            when (keyCode) {

                KeyEvent.KEYCODE_MEDIA_PLAY ->
                    "Playing music."

                KeyEvent.KEYCODE_MEDIA_PAUSE ->
                    "Pausing music."

                KeyEvent.KEYCODE_MEDIA_STOP ->
                    "Stopping music."

                KeyEvent.KEYCODE_MEDIA_NEXT ->
                    "Skipping to the next track."

                KeyEvent.KEYCODE_MEDIA_PREVIOUS ->
                    "Going to the previous track."

                else ->
                    "Media command sent."
            }

        } catch (_: Exception) {

            "I couldn't control media playback."
        }
    }
}
