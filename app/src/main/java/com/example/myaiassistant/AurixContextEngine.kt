package com.example.myaiassistant

object AurixContextEngine {

    data class ContextItem(
        val role: String,
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val history = mutableListOf<ContextItem>()

    fun addUserMessage(text: String) {
        if (text.isNotBlank()) {
            history.add(
                ContextItem(
                    role = "user",
                    text = text.trim()
                )
            )
        }
    }

    fun addAurixMessage(text: String) {
        if (text.isNotBlank()) {
            history.add(
                ContextItem(
                    role = "aurix",
                    text = text.trim()
                )
            )
        }
    }

    fun getLastUserMessage(): String? {
        return history
            .lastOrNull { it.role == "user" }
            ?.text
    }

    fun getLastAurixMessage(): String? {
        return history
            .lastOrNull { it.role == "aurix" }
            ?.text
    }

    fun getRecentHistory(limit: Int = 10): List<ContextItem> {
        return history.takeLast(limit)
    }

    fun hasPreviousConversation(): Boolean {
        return history.isNotEmpty()
    }

    fun clear() {
        history.clear()
    }
    fun getConversationText(): String {

    return history.joinToString("\n") {
        "${it.role}: ${it.text}"
    }
}

fun getLastMessages(limit: Int = 6): List<ContextItem> {

    return history.takeLast(limit)
}

    fun size(): Int {
        return history.size
    }
}
