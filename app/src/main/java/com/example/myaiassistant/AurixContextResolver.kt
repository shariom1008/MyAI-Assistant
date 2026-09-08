package com.example.myaiassistant

object AurixContextResolver {

    fun resolve(command: String): String {

        val current =
            command
                .lowercase()
                .trim()

        if (current.isBlank()) {
            return current
        }

        if (!ConversationMemoryEngine.hasMemory()) {
            return current
        }

        val recent =
            ConversationMemoryEngine
                .getRecentTurns()
                .takeLast(6)

        // -----------------------------------------------------
        // MEMORY QUESTIONS
        // -----------------------------------------------------

        if (
            current.contains("what is my name") ||
            current.contains("what's my name") ||
            current.contains("do you know my name") ||
            current.contains("mera naam kya hai") ||
            current.contains("mera name kya hai")
        ) {

            return buildMemoryAnswer(
                recent,
                "name"
            )
        }

        // -----------------------------------------------------
        // PREVIOUS CONVERSATION
        // -----------------------------------------------------

        if (
            current.contains("what did i say") ||
            current.contains("what did i tell you") ||
            current.contains("what we talked about") ||
            current.contains("maine kya bola") ||
            current.contains("maine kya kaha") ||
            current.contains("humne kya baat ki")
        ) {

            return buildConversationAnswer(
                recent
            )
        }

        return current
    }

    private fun buildMemoryAnswer(
        turns: List<ConversationMemoryEngine.Turn>,
        type: String
    ): String {

        if (turns.isEmpty()) {
            return "I don't have that information yet."
        }

        if (type == "name") {

            for (turn in turns.asReversed()) {

                val user =
                    turn.user.lowercase()

                val patterns =
                    listOf(
                        "my name is ",
                        "mera naam ",
                        "mera name "
                    )

                for (pattern in patterns) {

                    val index =
                        user.indexOf(pattern)

                    if (index >= 0) {

                        var name =
                            turn.user
                                .substring(
                                    index + pattern.length
                                )
                                .trim()

                        name =
                            name
                                .removeSuffix(".")
                                .removeSuffix("!")
                                .removeSuffix("?")
                                .trim()

                        if (
                            name.isNotBlank() &&
                            name.length <= 40
                        ) {

                            return "Your name is $name."
                        }
                    }
                }
            }
        }

        return "I don't remember your name yet."
    }

    private fun buildConversationAnswer(
        turns: List<ConversationMemoryEngine.Turn>
    ): String {

        if (turns.isEmpty()) {
            return "I don't have any previous conversation yet."
        }

        val recent =
            turns
                .takeLast(3)

        val summary =
            recent.joinToString(". ") {
                "You said ${it.user}"
            }

        return summary
    }
}
