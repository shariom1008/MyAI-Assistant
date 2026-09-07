package com.example.myaiassistant

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * AURIX 2.0
 *
 * Notification Listener Service
 *
 * Gives AURIX access to active media notifications.
 * This allows AURIX to work with media sessions exposed
 * by supported music/video applications.
 */
class AurixMediaSessionService : NotificationListenerService() {

    companion object {

        @Volatile
        var instance: AurixMediaSessionService? = null
            private set

        fun isConnected(): Boolean {
            return instance != null
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        instance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {
        // Media notifications are monitored here.
        // MediaSession control will be connected in the next step.
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification
    ) {
        // Notification removed.
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
