package com.example.myaiassistant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * AURIX 2.0
 * Bluetooth + Media Control Skill
 *
 * Handles:
 * - Paired Bluetooth devices
 * - Bluetooth settings
 * - Play / Pause / Stop
 * - Next / Previous
 * - Volume Up / Down
 * - Mute / Unmute
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
                cmd.contains("pause music") ||
                cmd.contains("stop music") ||
                cmd.contains("resume music") ||
                cmd.contains("next song") ||
                cmd.contains("next track") ||
                cmd.contains("previous song") ||
                cmd.contains("previous track") ||
                cmd.contains("music chalao") ||
                cmd.contains("music play karo") ||
                cmd.contains("volume up") ||
                cmd.contains("volume down") ||
                cmd.contains("increase volume") ||
                cmd.contains("decrease volume") ||
                cmd.contains("mute") ||
                cmd.contains("unmute")
    }

    override fun execute(command: String): String? {

        val cmd = command.lowercase().trim()

        return when {

            // -------------------------------------------------
            // PLAY
            // -------------------------------------------------

            cmd.contains("play music") ||
                    cmd.contains("resume music") ||
                    cmd.contains("music chalao") ||
                    cmd.contains("music play karo") -> {

                sendMediaCommand(
                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY
                )
            }

            // -------------------------------------------------
            // PAUSE
            // -------------------------------------------------

            cmd.contains("pause music") ||
                    cmd == "pause" ||
                    cmd.contains("music pause") -> {

                sendMediaCommand(
                    android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            }

            // -------------------------------------------------
            // STOP
            // -------------------------------------------------

            cmd.contains("stop music") ||
                    cmd.contains("music stop") -> {

                sendMediaCommand(
                    android.view.KeyEvent.KEYCODE_MEDIA_STOP
                )
            }

            // -------------------------------------------------
            // NEXT
            // -------------------------------------------------

            cmd.contains("next song") ||
                    cmd.contains("next track") ||
                    cmd.contains("skip song") ||
                    cmd.contains("next music") -> {

                sendMediaCommand(
                    android.view.KeyEvent.KEYCODE_MEDIA_NEXT
                )
            }

            // -------------------------------------------------
            // PREVIOUS
            // -------------------------------------------------

            cmd.contains("previous song") ||
                    cmd.contains("previous track") ||
                    cmd.contains("last song") -> {

                sendMediaCommand(
                    android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            }

            // -------------------------------------------------
            // VOLUME UP
            // -------------------------------------------------

            cmd.contains("volume up") ||
                    cmd.contains("increase volume") ||
                    cmd.contains("raise volume") ||
                    cmd.contains("volume increase") -> {

                changeVolume(
                    AudioManager.ADJUST_RAISE
                )
            }

            // -------------------------------------------------
            // VOLUME DOWN
            // -------------------------------------------------

            cmd.contains("volume down") ||
                    cmd.contains("decrease volume") ||
                    cmd.contains("lower volume") ||
                    cmd.contains("volume decrease") -> {

                changeVolume(
                    AudioManager.ADJUST_LOWER
                )
            }

            // -------------------------------------------------
            // MUTE
            // -------------------------------------------------

            cmd == "mute" ||
                    cmd.contains("mute speaker") ||
                    cmd.contains("mute volume") -> {

                changeVolume(
                    AudioManager.ADJUST_MUTE
                )
            }

            // -------------------------------------------------
            // UNMUTE
            // -------------------------------------------------

            cmd == "unmute" ||
                    cmd.contains("unmute speaker") ||
                    cmd.contains("unmute volume") -> {

                changeVolume(
                    AudioManager.ADJUST_UNMUTE
                )
            }

            // -------------------------------------------------
            // BLUETOOTH DEVICE LIST
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

    // =========================================================
    // MEDIA CONTROL
    // =========================================================

    private fun sendMediaCommand(
        keyCode: Int
    ): String {

        return try {

            val audioManager =
                context.getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            val downEvent =
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN,
                    keyCode
                )

            val upEvent =
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP,
                    keyCode
                )

            audioManager.dispatchMediaKeyEvent(
                downEvent
            )

            audioManager.dispatchMediaKeyEvent(
                upEvent
            )

            when (keyCode) {

                android.view.KeyEvent.KEYCODE_MEDIA_PLAY ->
                    "Playing music."

                android.view.KeyEvent.KEYCODE_MEDIA_PAUSE ->
                    "Pausing music."

                android.view.KeyEvent.KEYCODE_MEDIA_STOP ->
                    "Stopping music."

                android.view.KeyEvent.KEYCODE_MEDIA_NEXT ->
                    "Skipping to the next track."

                android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS ->
                    "Going to the previous track."

                else ->
                    "Media command executed."
            }

        } catch (_: Exception) {

            "I couldn't control media playback."
        }
    }

    // =========================================================
    // VOLUME CONTROL
    // =========================================================

    private fun changeVolume(
        direction: Int
    ): String {

        return try {

            val audioManager =
                context.getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                AudioManager.FLAG_SHOW_UI
            )

            when (direction) {

                AudioManager.ADJUST_RAISE ->
                    "Volume increased."

                AudioManager.ADJUST_LOWER ->
                    "Volume decreased."

                AudioManager.ADJUST_MUTE ->
                    "Volume muted."

                AudioManager.ADJUST_UNMUTE ->
                    "Volume unmuted."

                else ->
                    "Volume changed."
            }

        } catch (_: Exception) {

            "I couldn't control the volume."
        }
    }

    // =========================================================
    // PAIRED BLUETOOTH DEVICES
    // =========================================================

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

    // =========================================================
    // BLUETOOTH SETTINGS
    // =========================================================

    private fun openBluetoothSettings(): String {

        return try {

            val intent = Intent(
                Settings.ACTION_BLUETOOTH_SETTINGS
            ).apply {

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

            context.startActivity(intent)

            "Opening Bluetooth settings."

        } catch (_: Exception) {

            "I couldn't open Bluetooth settings."
        }
    }
}
