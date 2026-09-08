package com.example.myaiassistant

object AurixContextResolver {

    sealed class Resolution {

        data class Command(
            val command: String
        ) : Resolution()

        data class MemoryStatement(
            val original: String
        ) : Resolution()

        data class DirectResponse(
            val response: String
        ) : Resolution()

        data class ClearMemory(
            val all: Boolean,
            val key: String?
        ) : Resolution()
    }

    fun resolve(
        context: android.content.Context,
        command: String
    ): Resolution {

        val current =
            command
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        if (current.isBlank()) {
            return Resolution.Command("")
        }

        val lower =
            current.lowercase()

        // =====================================================
        // MEMORY STATEMENTS
        // =====================================================

        if (
            lower.startsWith("my name is ") ||
            lower.startsWith("mera naam ") ||
            lower.startsWith("mera name ")
        ) {

            return Resolution.MemoryStatement(
                current
            )
        }

        if (
            lower.startsWith("i live in ") ||
            lower.startsWith("i am from ") ||
            lower.startsWith("i'm from ") ||
            lower.startsWith("main ") &&
            (
                lower.contains("rehta hoon") ||
                lower.contains("rehti hoon")
            )
        ) {

            return Resolution.MemoryStatement(
                current
            )
        }

        if (
            lower.startsWith("i work as ") ||
            lower.startsWith("i am working as ") ||
            lower.startsWith("my job is ")
        ) {

            return Resolution.MemoryStatement(
                current
            )
        }

        if (
            lower.startsWith("i like ") ||
            lower.startsWith("i love ") ||
            lower.startsWith("i don't like ") ||
            lower.startsWith("i hate ")
        ) {

            return Resolution.MemoryStatement(
                current
            )
        }

        if (
            lower.startsWith("my favourite ") ||
            lower.startsWith("my favorite ")
        ) {

            return Resolution.MemoryStatement(
                current
            )
        }

        if (
            lower.startsWith("remember that ") ||
            lower.startsWith("remember this ") ||
            lower.startsWith("yaad rakhna ") ||
            lower.startsWith("ye yaad rakhna ")
        ) {

            return Resolution.MemoryStatement(
                current
            )
        }

        // =====================================================
        // FORGET
        // =====================================================

        if (
            lower.contains("forget everything") ||
            lower.contains("forget all my memory") ||
            lower.contains("forget all memories") ||
            lower.contains("sab kuch bhool jao") ||
            lower.contains("meri saari memory delete")
        ) {

            return Resolution.ClearMemory(
                all = true,
                key = null
            )
        }

        if (
            lower.contains("forget my name") ||
            lower.contains("mera naam bhool jao")
        ) {

            return Resolution.ClearMemory(
                all = false,
                key = "name"
            )
        }

        if (
            lower.contains("forget where i live") ||
            lower.contains("meri location bhool jao")
        ) {

            return Resolution.ClearMemory(
                all = false,
                key = "location"
            )
        }

        // =====================================================
        // PERSONAL QUESTIONS
        // =====================================================

        if (
            lower.contains("what is my name") ||
            lower.contains("what's my name") ||
            lower.contains("do you know my name") ||
            lower.contains("mera naam kya hai") ||
            lower.contains("mera name kya hai")
        ) {

            val name =
                PersonalMemoryEngine.get(
                    context,
                    "name"
                )

            return Resolution.DirectResponse(
                if (name.isNullOrBlank()) {
                    "I don't remember your name yet."
                } else {
                    "Your name is $name."
                }
            )
        }

        if (
            lower.contains("where do i live") ||
            lower.contains("where am i from") ||
            lower.contains("do you know where i live") ||
            lower.contains("main kahan rehta hoon") ||
            lower.contains("main kahan rehti hoon")
        ) {

            val location =
                PersonalMemoryEngine.get(
                    context,
                    "location"
                )

            return Resolution.DirectResponse(
                if (location.isNullOrBlank()) {
                    "I don't remember your location yet."
                } else {
                    "You live in $location."
                }
            )
        }
        // =====================================================
// PERSONAL FAVOURITES
// =====================================================

val favouriteKey =
    when {

        lower.matches(
            Regex(".*what('?s| is) my favou?rite song.*")
        ) ->
            "favorite_song"

        lower.matches(
            Regex(".*what('?s| is) my favou?rite food.*")
        ) ->
            "favorite_food"

        lower.matches(
            Regex(".*what('?s| is) my favou?rite movie.*")
        ) ->
            "favorite_movie"

        lower.matches(
            Regex(".*what('?s| is) my favou?rite (color|colour).*")
        ) ->
            "favorite_color"

        else ->
            ""
    }

if (favouriteKey.isNotBlank()) {

    val value =
        PersonalMemoryEngine.get(
            context,
            favouriteKey
        )

    val response =
        if (value.isNullOrBlank()) {

            when (favouriteKey) {

                "favorite_song" ->
                    "I don't remember your favourite song yet."

                "favorite_food" ->
                    "I don't remember your favourite food yet."

                "favorite_movie" ->
                    "I don't remember your favourite movie yet."

                "favorite_color" ->
                    "I don't remember your favourite colour yet."

                else ->
                    "I don't remember that yet."
            }

        } else {

            when (favouriteKey) {

                "favorite_song" ->
                    "Your favourite song is $value."

                "favorite_food" ->
                    "Your favourite food is $value."

                "favorite_movie" ->
                    "Your favourite movie is $value."

                "favorite_color" ->
                    "Your favourite colour is $value."

                else ->
                    "You told me $value."
            }
        }

    return Resolution.DirectResponse(
        response
    )
}

        // =====================================================
        // WHAT DO YOU KNOW ABOUT ME
        // =====================================================

        if (
            lower.contains("what do you know about me") ||
            lower.contains("what all do you know about me") ||
            lower.contains("tell me about myself") ||
            lower.contains("mere baare mein kya jaante ho") ||
            lower.contains("mere bare mein kya pata hai")
        ) {

            return Resolution.DirectResponse(
                PersonalMemoryEngine
                    .buildSummary(context)
            )
        }

        // =====================================================
        // PREVIOUS CONVERSATION
        // =====================================================

        if (
            lower.contains("what did i tell you") ||
            lower.contains("what did i say") ||
            lower.contains("what have i told you") ||
            lower.contains("what we talked about") ||
            lower.contains("what were we talking about") ||
            lower.contains("maine kya bola") ||
            lower.contains("maine kya kaha") ||
            lower.contains("maine tumhe kya bataya") ||
            lower.contains("humne kya baat ki")
        ) {

            val turns =
                ConversationMemoryEngine
                    .getRecentTurns()
                    .takeLast(5)

            if (turns.isEmpty()) {

                return Resolution.DirectResponse(
                    "We don't have any previous conversation yet."
                )
            }

            val response =
                turns
                    .joinToString(". ") {
                        "You said ${it.user}"
                    }

            return Resolution.DirectResponse(
                response
            )
        }

        // =====================================================
        // CONTEXT REFERENCES
        // =====================================================

        if (
            lower == "open it" ||
            lower == "open that" ||
            lower == "open this" ||
            lower == "isko kholo" ||
            lower == "use kholo"
        ) {

            val previous =
                AurixContextEngine
                    .getLastUserMessage()

            if (!previous.isNullOrBlank()) {

                val resolved =
                    extractPreviousTarget(
                        previous
                    )

                if (resolved != null) {

                    return Resolution.Command(
                        "open $resolved"
                    )
                }
            }
        }

        if (
    lower == "play it" ||
    lower == "play that" ||
    lower == "play this" ||
    lower == "isko chalao" ||
    lower == "ise chalao" ||
    lower == "usko chalao"
) {

    val history =
        AurixContextEngine.getRecentHistory(8)

    val previousCommand =
        history
            .lastOrNull {
                it.role == "user" &&
                it.text.lowercase() != lower
            }
            ?.text
            ?.trim()

    if (!previousCommand.isNullOrBlank()) {

        val previousLower =
            previousCommand.lowercase()

        val target =
            when {

                previousLower.startsWith(
                    "search youtube "
                ) ->
                    previousCommand.substring(
                        "search youtube ".length
                    ).trim()

                previousLower.startsWith(
                    "youtube search "
                ) ->
                    previousCommand.substring(
                        "youtube search ".length
                    ).trim()

                previousLower.startsWith(
                    "youtube par "
                ) ->
                    previousCommand.substring(
                        "youtube par ".length
                    ).trim()

                previousLower.startsWith(
                    "search "
                ) ->
                    previousCommand.substring(
                        "search ".length
                    ).trim()

                else -> null
            }

        if (!target.isNullOrBlank()) {

            return Resolution.Command(
                "play $target"
            )
        }
    }
        }
        
        // =====================================================
        // AURIX SURPRISE MODE 🎁
        // =====================================================

if (
    lower == "aurix surprise me" ||
    lower == "surprise me" ||
    lower == "aurix surprise"
) {

    val surprises = listOf(
        "Surprise mode activated. 😎",
        "I was waiting for you to ask that. AURIX is ready.",
        "Here's your surprise: you're building something pretty cool.",
        "Surprise! I'm still listening. What's next?",
        "AURIX says: mission accepted. 🚀"
    )

    return Resolution.DirectResponse(
        surprises.random()
    )
}

        // =====================================================
        // OTHERWISE NORMAL COMMAND
        // =====================================================

        return Resolution.Command(
            current
        )
    }

    private fun extractPreviousTarget(
        previous: String
    ): String? {

        val lower =
            previous.lowercase().trim()

        val openPrefixes =
            listOf(
                "open ",
                "launch ",
                "start ",
                "run ",
                "use ",
                "show "
            )

        for (prefix in openPrefixes) {

            if (lower.startsWith(prefix)) {

                return previous
                    .substring(
                        prefix.length
                    )
                    .trim()
            }
        }

        if (
            lower.startsWith("search youtube ")
        ) {

            return previous
                .substring(
                    "search youtube ".length
                )
                .trim()
        }

        if (
            lower.startsWith("youtube search ")
        ) {

            return previous
                .substring(
                    "youtube search ".length
                )
                .trim()
        }
        if (
            lower.startsWith("search ")
) {

            return previous
              .substring(
                "search ".length
        )
        .trim()
        }

        return null
    }
}
