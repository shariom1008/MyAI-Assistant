package com.example.myaiassistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_TYPE =
            "com.example.myaiassistant.ALARM_TYPE"

        const val EXTRA_MESSAGE =
            "com.example.myaiassistant.ALARM_MESSAGE"

        private const val CHANNEL_ID =
            "aurix_alarm_channel"

        private const val NOTIFICATION_ID =
            9001
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val type =
            intent.getStringExtra(
                EXTRA_TYPE
            ) ?: "alarm"

        val message =
            intent.getStringExtra(
                EXTRA_MESSAGE
            ) ?: "Your AURIX alarm is ringing."

        createNotificationChannel(context)

        val openIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                9002,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.M
                        ) {
                            PendingIntent.FLAG_IMMUTABLE
                        } else {
                            0
                        }
            )

        val title =
            if (type == "timer") {
                "AURIX Timer"
            } else {
                "AURIX Alarm"
            }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_lock_idle_alarm
                )
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            if (type == "timer") {
                NOTIFICATION_ID
            } else {
                NOTIFICATION_ID + 1
            },
            notification
        )
    }

    private fun createNotificationChannel(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "AURIX Alarms and Timers",
                    NotificationManager.IMPORTANCE_HIGH
                )

            channel.description =
                "AURIX timer and alarm notifications"

            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                channel
            )
        }
    }
}
