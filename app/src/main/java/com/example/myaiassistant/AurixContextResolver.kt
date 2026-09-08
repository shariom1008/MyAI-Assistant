package com.example.myaiassistant

object AurixContextResolver {

    private const val DIRECT_RESPONSE_PREFIX =
        "AURIX_DIRECT_RESPONSE:"

    private const val MEMORY_STATEMENT_PREFIX =
        "AURIX_MEMORY_STATEMENT:"

    fun resolve(command: String): String {

        val current =
            command
                .lowercase()
                .trim()

        if (current.isBlank()) {
            return current
        }

        // -----------------------------------------------------
        // NAME MEMORY
        // -----------------------------------------------------

        if (
            current.startsWith("my name is ") ||
            current.startsWith("mera naam ") ||
            current.startsWith("mera name ")
        ) {

            return MEMORY_STATEMENT_PREFIX +
                    command.trim()
        }

        // -----------------------------------------------------
        // LOCATION MEMORY
        // -----------------------------------------------------

        if (
            current.startsWith("i live in ") ||
            current.startsWith("i am from ") ||
            current.startsWith("i'm from ") ||
            current.startsWith("main ") &&
            current.contains("mein rehta") ||
            current.startsWith("main ") &&
            current.contains("mein rehti")
        ) {

            return MEMORY_STATEMENT_PREFIX +
                    command.trim()
        }

        // -----------------------------------------------------
        // NO MEMORY
        // -----------------------------------------------------

        if (!ConversationMemoryEngine.hasMemory()) {
            return current
        }

        val recent =
            ConversationMemoryEngine
                .getRecentTurns()
                .takeLast(12)

        // -----------------------------------------------------
        // NAME QUESTION
        // -----------------------------------------------------

        if (
            current.contains("what is my name") ||
            current.contains("what's my name") ||
            current.contains("do you know my name") ||
            current.contains("mera naam kya hai") ||
            current.contains("mera name kya hai")
        ) {

            return DIRECT_RESPONSE_PREFIX +
                    buildMemoryAnswer(
                        recent,
                        "name"
                    )
        }

        // -----------------------------------------------------
        // LOCATION QUESTION
        // -----------------------------------------------------

        if (
            current.contains("where do i live") ||
            current.contains("where am i from") ||
            current.contains("do you know where i live") ||
            current.contains("main kahan rehta hoon") ||
            current.contains("main kahan rehti hoon")
        ) {

            return DIRECT_RESPONSE_PREFIX +
                    buildMemoryAnswer(
                        recent,
                        "location"
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

            return DIRECT_RESPONSE_PREFIX +
                    buildConversationAnswer(
                        recent
                    )
        }

        return current
    }

    // ---------------------------------------------------------
    // MEMORY ANSWER
    // ---------------------------------------------------------

    private fun buildMemoryAnswer(
        turns: List<ConversationMemoryEngine.Turn>,
        type: String
    ): String {

        if (turns.isEmpty()) {
            return "I don't have that information yet."
        }

        for (turn in turns.asReversed()) {

            val user =
                turn.user.lowercase()

            if (type == "name") {

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

                        val name =
                            cleanValue(
                                turn.user.substring(
                                    index + pattern.length
                                )
                            )

                        if (
                            name.isNotBlank() &&
                            name.length <= 40
                        ) {

                            return "Your name is $name."
                        }
                    }
                }
            }

            if (type == "location") {

                val patterns =
                    listOf(
                        "i live in ",
                        "i am from ",
                        "i'm from ",
                        "mein rehta hoon",
                        "mein rehti hoon"
                    )

                for (pattern in patterns) {

                    val index =
                        user.indexOf(pattern)

                    if (index >= 0) {

                        val location =
                            cleanValue(
                                turn.user.substring(
                                    index + pattern.length
                                )
                            )

                        if (
                            location.isNotBlank() &&
                            location.length <= 60
                        ) {

                            return "You live in $location."
                        }
                    }
                }
            }
        }

        return when (type) {

            "name" ->
                "I don't remember your name yet."

            "location" ->
                "I don't remember where you live yet."

            else ->
                "I don't have that information yet."
        }
    }

    // ---------------------------------------------------------
    // CLEAN MEMORY VALUE
    // ---------------------------------------------------------

    private fun cleanValue(
        value: String
    ): String {

        return value
            .trim()
            .removeSuffix(".")
            .removeSuffix("!")
            .removeSuffix("?")
            .trim()
    }

    // ---------------------------------------------------------
    // CONVERSATION SUMMARY
    // ---------------------------------------------------------

    private fun buildConversationAnswer(
        turns: List<ConversationMemoryEngine.Turn>
    ): String {

        if (turns.isEmpty()) {
            return "I don't have any previous conversation yet."
        }

        val recent =
            turns
                .takeLast(3)

        return recent.joinToString(". ") {
            "You said ${it.user}"
        }
    }
}
