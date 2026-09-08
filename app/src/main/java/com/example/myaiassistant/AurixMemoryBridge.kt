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
        if (
            user.isBlank() ||
            aurix.isBlank()
        ) {
            return
        }

        ConversationMemoryEngine.addTurn(
            context,
            user,
            aurix
        )

        AurixContextEngine.addUserMessage(
            user
        )

        AurixContextEngine.addAurixMessage(
            aurix
        )
    }

    fun getContext(): String {
        return ConversationMemoryEngine.getContext()
    }

    fun getRecentContext(
        limit: Int = 6
    ): String {

        return ConversationMemoryEngine
            .getRecentTurns()
            .takeLast(limit)
            .joinToString("\n") {
                "User: ${it.user}\nAURIX: ${it.assistant}"
            }
    }

    fun hasMemory(): Boolean {
        return ConversationMemoryEngine.hasMemory()
    }

    fun clear(context: Context) {

        AurixContextEngine.clear()

        ConversationMemoryEngine.clear(
            context
        )
    }

    // -----------------------------------------------------
    // DIRECT MEMORY SAVE
    // -----------------------------------------------------

    fun remember(
        context: Context,
        userStatement: String,
        aurixResponse: String
    ) {

        if (
            userStatement.isBlank() ||
            aurixResponse.isBlank()
        ) {
            return
        }

        saveTurn(
            context,
            userStatement,
            aurixResponse
        )
    }

    // -----------------------------------------------------
    // MEMORY CONTEXT
    // -----------------------------------------------------

    fun getMemoryContext(): String {

        if (
            !ConversationMemoryEngine.hasMemory()
        ) {
            return ""
        }

        return ConversationMemoryEngine
            .getRecentTurns()
            .takeLast(12)
            .joinToString("\n") {
                "User: ${it.user}\nAURIX: ${it.assistant}"
            }
    }
}
