package com.example.myaiassistant

object ConversationMemoryEngine {

    data class Turn(
        val user: String,
        val assistant: String,
        val time: Long = System.currentTimeMillis()
    )

    private val turns = ArrayDeque<Turn>()

    private const val MAX_TURNS = 12

    fun addTurn(
        user: String,
        assistant: String
    ) {
        if (user.isBlank()) return

        turns.addLast(
            Turn(
                user = user.trim(),
                assistant = assistant.trim()
            )
        )

        while (turns.size > MAX_TURNS) {
            turns.removeFirst()
        }
    }

    fun getRecentTurns(): List<Turn> {
        return turns.toList()
    }

    fun getContext(): String {
        return turns.joinToString("\n") {
            "User: ${it.user}\nAURIX: ${it.assistant}"
        }
    }

    fun clear() {
        turns.clear()
    }
}
