package com.example.myaiassistant

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * AURIX Notification Listener
 *
 * Receives notifications from the Android system
 * and forwards useful notification text to
 * AurixNotificationEngine.
 *
 * The user must manually enable Notification Access
 * for AURIX in Android Settings.
 */
class AurixNotificationListenerService :
    NotificationListenerService() {

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {

        try {

            val packageName =
                sbn.packageName ?: return

            val notification =
                sbn.notification ?: return

            val extras =
                notification.extras ?: return

            val title =
                extras.getCharSequence(
                    Notification.EXTRA_TITLE
                )?.toString()
                    ?: ""

            val text =
                extras.getCharSequence(
                    Notification.EXTRA_TEXT
                )?.toString()
                    ?: ""

            // Ignore completely empty notifications.
            if (
                title.isBlank() &&
                text.isBlank()
            ) {
                return
            }

            val appName =
                getApplicationName(packageName)

            AurixNotificationEngine.addNotification(
                appName = appName,
                title = title,
                text = text
            )

        } catch (
            e: Exception
        ) {
            // Never crash the notification listener.
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification
    ) {
        // Nothing required here for now.
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        // Listener connected successfully.
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        // Android may reconnect the listener automatically.
    }

    /**
     * Convert package name into a readable application name.
     */
    private fun getApplicationName(
        packageName: String
    ): String {

        return try {

            val applicationInfo =
                packageManager.getApplicationInfo(
                    packageName,
                    0
                )

            packageManager
                .getApplicationLabel(
                    applicationInfo
                )
                .toString()

        } catch (
            e: Exception
        ) {

            packageName
        }
    }
}
