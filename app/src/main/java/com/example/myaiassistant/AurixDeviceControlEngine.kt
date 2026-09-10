package com.example.myaiassistant

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings

object AurixDeviceControlEngine {

    private var flashlightOn = false

    fun answer(context: Context, command: String): String? {

        val c = command
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) return null

        // =========================================================
        // VOLUME UP
        // =========================================================

        if (
            c == "volume up" ||
            c == "increase volume" ||
            c == "volume badhao" ||
            c == "awaz badhao" ||
            c == "sound badhao"
        ) {
            changeVolume(context, AudioManager.ADJUST_RAISE)
            return "Volume badha diya, Boss."
        }

        // =========================================================
        // VOLUME DOWN
        // =========================================================

        if (
            c == "volume down" ||
            c == "decrease volume" ||
            c == "volume kam karo" ||
            c == "awaz kam karo" ||
            c == "sound kam karo"
        ) {
            changeVolume(context, AudioManager.ADJUST_LOWER)
            return "Volume kam kar diya, Boss."
        }

        // =========================================================
        // MUTE
        // =========================================================

        if (
            c == "mute" ||
            c == "mute phone" ||
            c == "phone mute karo" ||
            c == "awaz band karo" ||
            c == "sound mute karo"
        ) {
            val audioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                0
            )

            return "Phone mute kar diya, Boss."
        }

        // =========================================================
        // UNMUTE
        // =========================================================

        if (
            c == "unmute" ||
            c == "unmute phone" ||
            c == "phone unmute karo" ||
            c == "awaz chalu karo"
        ) {
            val audioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_UNMUTE,
                0
            )

            return "Phone unmute kar diya, Boss."
        }

        // =========================================================
        // MEDIA PLAY / PAUSE
        // =========================================================

        if (
            c == "play music" ||
            c == "music play karo" ||
            c == "music chalao" ||
            c == "play"
        ) {
            sendMediaKey(
                context,
                android.view.KeyEvent.KEYCODE_MEDIA_PLAY
            )

            return "Music play karne ka command bhej diya, Boss."
        }

        if (
            c == "pause music" ||
            c == "music pause karo" ||
            c == "music rok do" ||
            c == "pause"
        ) {
            sendMediaKey(
                context,
                android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
            )

            return "Music pause karne ka command bhej diya, Boss."
        }

        // =========================================================
        // NEXT TRACK
        // =========================================================

        if (
            c == "next song" ||
            c == "next music" ||
            c == "agla gana" ||
            c == "next"
        ) {
            sendMediaKey(
                context,
                android.view.KeyEvent.KEYCODE_MEDIA_NEXT
            )

            return "Next track ka command bhej diya, Boss."
        }

        // =========================================================
        // PREVIOUS TRACK
        // =========================================================

        if (
            c == "previous song" ||
            c == "previous music" ||
            c == "pichla gana" ||
            c == "previous"
        ) {
            sendMediaKey(
                context,
                android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
            )

            return "Previous track ka command bhej diya, Boss."
        }

        // =========================================================
        // FLASHLIGHT ON
        // =========================================================

        if (
            c == "flashlight on" ||
            c == "torch on" ||
            c == "torch chalao" ||
            c == "flashlight chalao" ||
            c == "torch jalao"
        ) {
            return setFlashlight(context, true)
        }

        // =========================================================
        // FLASHLIGHT OFF
        // =========================================================

        if (
            c == "flashlight off" ||
            c == "torch off" ||
            c == "torch band karo" ||
            c == "flashlight band karo" ||
            c == "torch bujhao"
        ) {
            return setFlashlight(context, false)
        }

        // =========================================================
        // VIBRATION
        // =========================================================

        if (
            c == "vibrate" ||
            c == "vibration" ||
            c == "phone vibrate karo"
        ) {
            vibrate(context)
            return "Phone vibrate kar diya, Boss."
        }

        // =========================================================
        // SCREEN BRIGHTNESS UP
        // =========================================================

        if (
            c == "brightness up" ||
            c == "brightness badhao" ||
            c == "screen brightness badhao"
        ) {
            return changeBrightness(context, 0.2f)
        }

        // =========================================================
        // SCREEN BRIGHTNESS DOWN
        // =========================================================

        if (
            c == "brightness down" ||
            c == "brightness kam karo" ||
            c == "screen brightness kam karo"
        ) {
            return changeBrightness(context, -0.2f)
        }

        // =========================================================
        // BRIGHTNESS MAX
        // =========================================================

        if (
            c == "brightness full" ||
            c == "full brightness" ||
            c == "brightness maximum"
        ) {
            return setBrightness(context, 1.0f)
        }

        // =========================================================
        // BRIGHTNESS MIN
        // =========================================================

        if (
            c == "brightness minimum" ||
            c == "brightness lowest" ||
            c == "brightness bilkul kam"
        ) {
            return setBrightness(context, 0.05f)
        }

        // =========================================================
        // NOTHING FOUND
        // =========================================================

        return null
    }

    // =============================================================
    // VOLUME
    // =============================================================

    private fun changeVolume(
        context: Context,
        direction: Int
    ) {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    // =============================================================
    // MEDIA
    // =============================================================

    private fun sendMediaKey(
        context: Context,
        keyCode: Int
    ) {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.dispatchMediaKeyEvent(
            android.view.KeyEvent(
                android.view.KeyEvent.ACTION_DOWN,
                keyCode
            )
        )

        audioManager.dispatchMediaKeyEvent(
            android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP,
                keyCode
            )
        )
    }

    // =============================================================
    // FLASHLIGHT
    // =============================================================

    private fun setFlashlight(
        context: Context,
        enable: Boolean
    ): String {

        return try {

            val cameraManager =
                context.getSystemService(Context.CAMERA_SERVICE)
                        as android.hardware.camera2.CameraManager

            val cameraId =
                cameraManager.cameraIdList.firstOrNull { id ->

                    val characteristics =
                        cameraManager.getCameraCharacteristics(id)

                    characteristics.get(
                        android.hardware.camera2.CameraCharacteristics
                            .FLASH_INFO_AVAILABLE
                    ) == true
                }

            if (cameraId == null) {
                return "Is phone mein flashlight available nahi hai, Boss."
            }

            cameraManager.setTorchMode(
                cameraId,
                enable
            )

            flashlightOn = enable

            if (enable) {
                "Flashlight on kar di, Boss."
            } else {
                "Flashlight off kar di, Boss."
            }

        } catch (e: Exception) {

            "Flashlight control nahi ho paya, Boss."
        }
    }

    // =============================================================
    // VIBRATION
    // =============================================================

    private fun vibrate(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val vibratorManager =
                context.getSystemService(
                    Context.VIBRATOR_MANAGER_SERVICE
                ) as VibratorManager

            val vibrator =
                vibratorManager.defaultVibrator

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )

        } else {

            @Suppress("DEPRECATION")
            val vibrator =
                context.getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator

            @Suppress("DEPRECATION")
            vibrator.vibrate(300)
        }
    }

    // =============================================================
    // BRIGHTNESS
    // =============================================================

    private fun changeBrightness(
        context: Context,
        amount: Float
    ): String {

        return try {

            if (
                !Settings.System.canWrite(context)
            ) {
                return "Screen brightness control ke liye system permission required hai, Boss."
            }

            val current =
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
                )

            val currentNormalized =
                current / 255f

            val newValue =
                (currentNormalized + amount)
                    .coerceIn(0.05f, 1.0f)

            setBrightness(
                context,
                newValue
            )

        } catch (e: Exception) {

            "Brightness change nahi ho payi, Boss."
        }
    }

    private fun setBrightness(
        context: Context,
        value: Float
    ): String {

        return try {

            if (
                !Settings.System.canWrite(context)
            ) {
                return "Screen brightness control ke liye system permission required hai, Boss."
            }

            val brightness =
                (value.coerceIn(0.05f, 1.0f) * 255)
                    .toInt()

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness
            )

            "Screen brightness change kar di, Boss."

        } catch (e: Exception) {

            "Brightness change nahi ho payi, Boss."
        }
    }
}
