package com.example.myaiassistant

import android.content.Context

object AurixMemoryBridge {

    fun initialize(context: Context) {
        ConversationMemoryEngine.initialize(context)
    }

    fun saveTurn(
        context: Context,
        user: String,
        aurix: String
    ) {
        if (user.isBlank() || aurix.isBlank()) {
            return
        }

        ConversationMemoryEngine.addTurn(
            context,
            user,
            aurix
        )

        AurixContextEngine.addUserMessage(user)
        AurixContextEngine.addAurixMessage(aurix)
    }

    fun getContext(): String {

        val currentContext =
            AurixContextEngine.getConversationText()

        val savedContext =
            ConversationMemoryEngine.getContext()

        return buildString {

            if (savedContext.isNotBlank()) {
                append(savedContext)
            }

            if (
                currentContext.isNotBlank() &&
                savedContext.isNotBlank()
            ) {
                append("\n")
            }

            if (currentContext.isNotBlank()) {
                append(currentContext)
            }
        }
    }
    fun getRecentContext(limit: Int = 6): String {

    val items =
        AurixContextEngine.getLastMessages(limit)

    return items.joinToString("\n") {
        "${it.role}: ${it.text}"
    }
    }

    fun clear(context: Context) {

        AurixContextEngine.clear()

        ConversationMemoryEngine.clear(
            context
        )
    }
}
