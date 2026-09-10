package com.example.myaiassistant

import android.content.Context

/**
 * AURIX Notification Engine
 *
 * Handles local notification information received
 * through AurixNotificationListenerService.
 */
object AurixNotificationEngine {

    private val notifications =
        mutableListOf<String>()

    private const val MAX_NOTIFICATIONS = 50

    /**
     * Add a notification received from the listener.
     */
    @Synchronized
    fun addNotification(
        appName: String,
        title: String,
        text: String
    ) {

        val cleanApp =
            appName.trim()

        val cleanTitle =
            title.trim()

        val cleanText =
            text.trim()

        if (
            cleanApp.isBlank() &&
            cleanTitle.isBlank() &&
            cleanText.isBlank()
        ) {
            return
        }

        val notificationText =
            buildString {

                if (cleanApp.isNotBlank()) {
                    append(cleanApp)
                }

                if (cleanTitle.isNotBlank()) {

                    if (isNotEmpty()) {
                        append(": ")
                    }

                    append(cleanTitle)
                }

                if (cleanText.isNotBlank()) {

                    if (isNotEmpty()) {
                        append(" - ")
                    }

                    append(cleanText)
                }
            }

        if (notificationText.isBlank()) {
            return
        }

        // Avoid exact duplicates.
        if (
            notifications.any {
                it.equals(
                    notificationText,
                    ignoreCase = true
                )
            }
        ) {
            return
        }

        notifications.add(
            0,
            notificationText
        )

        // Keep memory small.
        while (
            notifications.size >
            MAX_NOTIFICATIONS
        ) {
            notifications.removeAt(
                notifications.lastIndex
            )
        }
    }

    /**
     * Remove all stored notification information.
     */
    @Synchronized
    fun clearNotifications() {

        notifications.clear()
    }

    /**
     * Get all notifications.
     */
    @Synchronized
    fun getNotifications(): List<String> {

        return notifications.toList()
    }

    /**
     * Number of stored notifications.
     */
    @Synchronized
    fun getNotificationCount(): Int {

        return notifications.size
    }

    /**
     * Main command processor.
     */
    fun answer(
        context: Context,
        command: String
    ): String? {

        val c =
            command.trim().lowercase()

        if (c.isBlank()) {
            return null
        }

        // =========================================================
        // SHOW NOTIFICATIONS
        // =========================================================

        if (
            c.contains("read notifications") ||
            c.contains("read notification") ||
            c.contains("show notifications") ||
            c.contains("show notification") ||
            c.contains("check notifications") ||
            c.contains("check notification") ||
            c.contains("notification dikhao") ||
            c.contains("notifications dikhao") ||
            c.contains("notification padho") ||
            c.contains("notifications padho") ||
            c.contains("notification batao")
        ) {

            val list =
                getNotifications()

            if (list.isEmpty()) {

                return "Boss, abhi koi notification available nahi hai."
            }

            return buildString {

                append(
                    "Boss, recent notifications: "
                )

                list.take(5)
                    .forEachIndexed { index, item ->

                        if (index > 0) {
                            append(". ")
                        }

                        append(
                            "${index + 1}. $item"
                        )
                    }
            }
        }

        // =========================================================
        // NOTIFICATION COUNT
        // =========================================================

        if (
            c.contains("notification count") ||
            c.contains("how many notifications") ||
            c.contains("kitni notifications") ||
            c.contains("kitne notifications")
        ) {

            val count =
                getNotificationCount()

            return if (count == 0) {

                "Boss, koi notification saved nahi hai."

            } else {

                "Boss, mere paas $count recent notifications hain."
            }
        }

        // =========================================================
        // CLEAR NOTIFICATIONS
        // =========================================================

        if (
            c.contains("clear notifications") ||
            c.contains("delete notifications") ||
            c.contains("remove notifications") ||
            c.contains("notifications clear karo") ||
            c.contains("notifications delete karo") ||
            c.contains("notification clear karo")
        ) {

            clearNotifications()

            return "Theek hai Boss, notification list clear kar di."
        }

        // =========================================================
        // LATEST NOTIFICATION
        // =========================================================

        if (
            c.contains("latest notification") ||
            c.contains("last notification") ||
            c.contains("latest notification batao") ||
            c.contains("last notification batao") ||
            c.contains("latest notification padho")
        ) {

            val list =
                getNotifications()

            if (list.isEmpty()) {

                return "Boss, koi notification available nahi hai."
            }

            return "Boss, latest notification: ${list.first()}"
        }

        return null
    }
}
