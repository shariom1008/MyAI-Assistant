package com.example.myaiassistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AurixAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: AurixAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        // AURIX does not need to process accessibility events.
    }

    override fun onInterrupt() {
        // Nothing to interrupt.
    }

    fun goHome(): Boolean {
        return performGlobalAction(
            GLOBAL_ACTION_HOME
        )
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }
}
