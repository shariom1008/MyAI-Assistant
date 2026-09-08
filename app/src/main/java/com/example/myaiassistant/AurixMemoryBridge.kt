package com.example.myaiassistant

import android.content.Context

object AurixMemoryBridge {

    fun initialize(
        context: Context
    ) {

        ConversationMemoryEngine
            .initialize(context)

        PersonalMemoryEngine
            .initialize(context)
    }

    fun saveTurn(
        context: Context,
        user: String,
        aurix: String
    ) {

        if (user.isBlank()) return

        ConversationMemoryEngine.addTurn(
            context,
            user,
            aurix
        )

        AurixContextEngine.addUserMessage(
            user
        )

        if (aurix.isNotBlank()) {

            AurixContextEngine
                .addAurixMessage(
                    aurix
                )
        }
    }

    fun remember(
        context: Context,
        key: String,
        value: String
    ) {

        PersonalMemoryEngine.remember(
            context,
            key,
            value
        )
    }

    fun rememberThat(
        context: Context,
        statement: String
    ) {

        PersonalMemoryEngine.rememberThat(
            context,
            statement
        )
    }

    fun getPersonalMemory(
        context: Context
    ): String {

        return PersonalMemoryEngine
            .buildSummary(context)
    }

    fun clearAll(
        context: Context
    ) {

        PersonalMemoryEngine
            .forgetAll(context)

        ConversationMemoryEngine
            .clear(context)

        AurixContextEngine
            .clear()
    }

    fun clearMemoryKey(
        context: Context,
        key: String
    ) {

        PersonalMemoryEngine
            .forget(
                context,
                key
            )
    }

    fun getContext(): String {

        return ConversationMemoryEngine
            .getContext()
    }

    fun getRecentContext(
        limit: Int = 8
    ): String {

        return ConversationMemoryEngine
            .getRecentTurns()
            .takeLast(limit)
            .joinToString("\n") {
                "User: ${it.user}\nAURIX: ${it.assistant}"
            }
    }

    fun hasMemory(): Boolean {

        return ConversationMemoryEngine
            .hasMemory()
    }
}
