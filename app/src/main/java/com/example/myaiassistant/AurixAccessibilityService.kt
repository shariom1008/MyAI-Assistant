package com.example.myaiassistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * AURIX Accessibility Service
 *
 * Initial test version.
 *
 * Purpose:
 * - Verify that Android successfully binds AURIX
 * - Observe foreground app/window changes
 * - Provide a foundation for permitted global navigation
 */
class AurixAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()

        sendAurixStatus(
            "Accessibility service connected."
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        if (
            event.eventType ==
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {

            val packageName =
                event.packageName
                    ?.toString()
                    ?: return

            sendAurixStatus(
                "Foreground: $packageName"
            )
        }
    }

    override fun onInterrupt() {
        sendAurixStatus(
            "Accessibility service interrupted."
        )
    }

    private fun sendAurixStatus(
        message: String
    ) {

        try {

            sendBroadcast(
                android.content.Intent(
                    "com.example.myaiassistant.AURIX_ACCESSIBILITY_STATUS"
                ).apply {
                    putExtra(
                        "message",
                        message
                    )
                    setPackage(packageName)
                }
            )

        } catch (_: Exception) {
        }
    }
}
