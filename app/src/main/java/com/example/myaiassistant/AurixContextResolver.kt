package com.example.myaiassistant

import android.content.Context

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
            val key: String? = null
        ) : Resolution()
    }

    fun resolve(
        context: Context,
        command: String
    ): Resolution {

        val lower =
            command.trim().lowercase()

        // -------------------------------------------------
        // MEMORY STATEMENTS
        // -------------------------------------------------

        if (
            lower.startsWith("my name is ") ||
            lower.startsWith("mera naam ") ||
            lower.startsWith("mera name ")
        ) {
            return Resolution.MemoryStatement(command)
        }

        if (
            lower.startsWith("i live in ") ||
            lower.startsWith("i am from ") ||
            lower.startsWith("i'm from ") ||
            lower.startsWith("main rehta hoon ") ||
            lower.startsWith("main rehti hoon ")
        ) {
            return Resolution.MemoryStatement(command)
        }

        if (
            lower.startsWith("i work as ") ||
            lower.startsWith("i am working as ") ||
            lower.startsWith("my job is ")
        ) {
            return Resolution.MemoryStatement(command)
        }

        if (
            lower.startsWith("i like ") ||
            lower.startsWith("i love ") ||
            lower.startsWith("i don't like ") ||
            lower.startsWith("i hate ")
        ) {
            return Resolution.MemoryStatement(command)
        }

        if (
            lower.startsWith("my favourite ") ||
            lower.startsWith("my favorite ")
        ) {
            return Resolution.MemoryStatement(command)
        }

        if (
            lower.startsWith("remember that ") ||
            lower.startsWith("remember this ") ||
            lower.startsWith("yaad rakhna ") ||
            lower.startsWith("ye yaad rakhna ")
        ) {
            return Resolution.MemoryStatement(command)
        }

        // -------------------------------------------------
        // PERSONAL MEMORY QUESTIONS
        // -------------------------------------------------

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

        // -------------------------------------------------
        // CLEAR MEMORY
        // -------------------------------------------------

        if (
            lower == "forget everything" ||
            lower == "forget all" ||
            lower == "clear memory" ||
            lower == "clear my memory" ||
            lower == "sab bhool jao"
        ) {

            return Resolution.ClearMemory(
                all = true
            )
        }

        // -------------------------------------------------
        // CONTEXTUAL PLAY
        // -------------------------------------------------

        if (
            lower == "play it" ||
            lower == "play that" ||
            lower == "play this" ||
            lower == "isko chalao" ||
            lower == "ise chalao" ||
            lower == "usko chalao"
        ) {

            val history =
                AurixContextEngine
                    .getRecentHistory(10)

            val previous =
                history
                    .asReversed()
                    .firstOrNull {

                        if (it.role != "user") {
                            false
                        } else {

                            val text =
                                it.text
                                    .trim()
                                    .lowercase()

                            text.startsWith(
                                "search youtube "
                            ) ||
                            text.startsWith(
                                "youtube search "
                            ) ||
                            text.startsWith(
                                "youtube par "
                            ) ||
                            text.startsWith(
                                "search "
                            )
                        }
                    }
                    ?.text
                    ?.trim()

            if (!previous.isNullOrBlank()) {

                val previousLower =
                    previous.lowercase()

                val target =
                    when {

                        previousLower.startsWith(
                            "search youtube "
                        ) ->
                            previous.substring(
                                "search youtube ".length
                            ).trim()

                        previousLower.startsWith(
                            "youtube search "
                        ) ->
                            previous.substring(
                                "youtube search ".length
                            ).trim()

                        previousLower.startsWith(
                            "youtube par "
                        ) ->
                            previous.substring(
                                "youtube par ".length
                            ).trim()

                        previousLower.startsWith(
                            "search "
                        ) ->
                            previous.substring(
                                "search ".length
                            ).trim()

                        else -> ""
                    }

                if (target.isNotBlank()) {

                    return Resolution.Command(
                        "play $target"
                    )
                }
            }
        }

        // -------------------------------------------------
        // CONTEXTUAL OPEN
        // -------------------------------------------------

        if (
            lower == "open it" ||
            lower == "open that" ||
            lower == "open this" ||
            lower == "isko kholo" ||
            lower == "ise kholo"
        ) {

            val history =
                AurixContextEngine
                    .getRecentHistory(10)

            val previous =
                history
                    .asReversed()
                    .firstOrNull {
                        it.role == "user" &&
                        it.text
                            .trim()
                            .lowercase() != lower
                    }
                    ?.text
                    ?.trim()

            if (!previous.isNullOrBlank()) {

                val target =
                    extractPreviousTarget(
                        previous
                    )

                if (!target.isNullOrBlank()) {

                    return Resolution.Command(
                        "open $target"
                    )
                }
            }
        }

        return Resolution.Command(
            command
        )
    }

    // -----------------------------------------------------
    // PREVIOUS TARGET
    // -----------------------------------------------------

    private fun extractPreviousTarget(
        previous: String
    ): String? {

        val lower =
            previous.trim().lowercase()

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

            if (
                lower.startsWith(prefix)
            ) {

                return previous
                    .substring(prefix.length)
                    .trim()
            }
        }

        if (
            lower.startsWith(
                "search youtube "
            )
        ) {

            return previous
                .substring(
                    "search youtube ".length
                )
                .trim()
        }

        if (
            lower.startsWith(
                "youtube search "
            )
        ) {

            return previous
                .substring(
                    "youtube search ".length
                )
                .trim()
        }

        if (
            lower.startsWith(
                "youtube par "
            )
        ) {

            return previous
                .substring(
                    "youtube par ".length
                )
                .trim()
        }

        return null
    }
}
