package com.example.myaiassistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_TYPE =
            "alarm_type"

        const val EXTRA_MESSAGE =
            "alarm_message"

        private const val CHANNEL_ID =
            "aurix_alarm_channel"

        private const val NOTIFICATION_ID =
            9001
    }

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val message =
            intent?.getStringExtra(
                EXTRA_MESSAGE
            ) ?: "Your AURIX alarm is ringing."

        showNotification(
            context,
            message
        )

        playAlarmSound(
            context
        )

        speak(
            context,
            message
        )
    }

    private fun showNotification(
        context: Context,
        message: String
    ) {

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "AURIX Timer and Alarm",
                    NotificationManager.IMPORTANCE_HIGH
                )

            manager.createNotificationChannel(
                channel
            )
        }

        val notification =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                android.app.Notification.Builder(
                    context,
                    CHANNEL_ID
                )
                    .setContentTitle("AURIX")
                    .setContentText(message)
                    .setSmallIcon(
                        android.R.drawable.ic_lock_idle_alarm
                    )
                    .setAutoCancel(true)
                    .setPriority(
                        android.app.Notification.PRIORITY_HIGH
                    )
                    .build()

            } else {

                android.app.Notification.Builder(
                    context
                )
                    .setContentTitle("AURIX")
                    .setContentText(message)
                    .setSmallIcon(
                        android.R.drawable.ic_lock_idle_alarm
                    )
                    .setAutoCancel(true)
                    .setPriority(
                        android.app.Notification.PRIORITY_HIGH
                    )
                    .build()
            }

        manager.notify(
            NOTIFICATION_ID,
            notification
        )
    }

    private fun playAlarmSound(
        context: Context
    ) {

        try {

            val uri =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

            val ringtone =
                RingtoneManager.getRingtone(
                    context,
                    uri
                )

            ringtone.play()

            Handler(
                Looper.getMainLooper()
            ).postDelayed({

                try {
                    if (ringtone.isPlaying) {
                        ringtone.stop()
                    }
                } catch (_: Exception) {}

            }, 10000L)

        } catch (_: Exception) {}
    }

    private fun speak(
        context: Context,
        message: String
    ) {

        try {

            val tts =
                TextToSpeech(
                    context
                ) { status ->

                    if (
                        status ==
                        TextToSpeech.SUCCESS
                    ) {

                        ttsLanguage(
                            tts,
                            message
                        )
                    }
                }

        } catch (_: Exception) {}
    }

    private fun ttsLanguage(
        tts: TextToSpeech,
        message: String
    ) {

        try {

            tts.language =
                Locale.US

            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AURIX_ALARM"
            )

        } catch (_: Exception) {}
    }
}
