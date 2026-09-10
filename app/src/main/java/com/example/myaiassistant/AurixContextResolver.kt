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

        val original =
            command.trim()

        val lower =
            original
                .lowercase()
                .replace(Regex("\\s+"), " ")
                .trim()

        if (lower.isBlank()) {
            return Resolution.Command(original)
        }

        // =================================================
        // MEMORY STATEMENTS
        // =================================================

        if (
            lower.startsWith("my name is ") ||
            lower.startsWith("my name's ") ||
            lower.startsWith("mera naam ") ||
            lower.startsWith("mera name ")
        ) {
            return Resolution.MemoryStatement(original)
        }

        if (
            lower.startsWith("i live in ") ||
            lower.startsWith("i am from ") ||
            lower.startsWith("i'm from ") ||
            lower.startsWith("main rehta hoon ") ||
            lower.startsWith("main rehti hoon ") ||
            lower.startsWith("main ") &&
            (
                lower.contains("rehta hoon") ||
                lower.contains("rehti hoon")
            )
        ) {
            return Resolution.MemoryStatement(original)
        }

        if (
            lower.startsWith("i work as ") ||
            lower.startsWith("i am working as ") ||
            lower.startsWith("i'm working as ") ||
            lower.startsWith("my job is ") ||
            lower.startsWith("main kaam karta hoon ") ||
            lower.startsWith("main kaam karti hoon ")
        ) {
            return Resolution.MemoryStatement(original)
        }

        if (
            lower.startsWith("i like ") ||
            lower.startsWith("i love ") ||
            lower.startsWith("i don't like ") ||
            lower.startsWith("i do not like ") ||
            lower.startsWith("i hate ") ||
            lower.startsWith("mujhe pasand hai ") ||
            lower.startsWith("mujhe pasand nahi hai ")
        ) {
            return Resolution.MemoryStatement(original)
        }

        if (
            lower.startsWith("my favourite ") ||
            lower.startsWith("my favorite ") ||
            lower.startsWith("mera favourite ") ||
            lower.startsWith("mera favorite ")
        ) {
            return Resolution.MemoryStatement(original)
        }

        if (
            lower.startsWith("remember that ") ||
            lower.startsWith("remember this ") ||
            lower.startsWith("remember ") ||
            lower.startsWith("yaad rakhna ") ||
            lower.startsWith("ye yaad rakhna ") ||
            lower.startsWith("isko yaad rakhna ")
        ) {
            return Resolution.MemoryStatement(original)
        }

        // =================================================
        // PERSONAL MEMORY QUESTIONS
        // =================================================

        if (
            lower == "what is my name" ||
            lower == "what's my name" ||
            lower == "do you know my name" ||
            lower == "who am i" ||
            lower == "mera naam kya hai" ||
            lower == "mera name kya hai" ||
            lower == "kya tumhe mera naam pata hai" ||
            lower == "kya tum mera naam jaante ho" ||
            lower == "kya tum mera naam jaanti ho"
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

        // =================================================
        // CLEAR MEMORY
        // =================================================

        if (
            lower == "forget everything" ||
            lower == "forget all" ||
            lower == "forget everything you know" ||
            lower == "clear memory" ||
            lower == "clear my memory" ||
            lower == "delete memory" ||
            lower == "erase memory" ||
            lower == "sab bhool jao" ||
            lower == "sab kuch bhool jao" ||
            lower == "meri memory clear karo"
        ) {

            return Resolution.ClearMemory(
                all = true
            )
        }

        // =================================================
        // CONTEXTUAL PLAY
        // =================================================

        if (
            isPlayFollowUp(lower)
        ) {

            val target =
                findPreviousPlayableTarget()

            if (!target.isNullOrBlank()) {

                return Resolution.Command(
                    "play $target"
                )
            }
        }

        // =================================================
        // CONTEXTUAL OPEN
        // =================================================

        if (
            isOpenFollowUp(lower)
        ) {

            val target =
                findPreviousOpenTarget()

            if (!target.isNullOrBlank()) {

                return Resolution.Command(
                    "open $target"
                )
            }
        }

        // =================================================
        // CONTEXTUAL SEARCH
        // =================================================

        if (
            isSearchFollowUp(lower)
        ) {

            val target =
                findPreviousSearchTarget()

            if (!target.isNullOrBlank()) {

                return Resolution.Command(
                    "search $target"
                )
            }
        }

        // =================================================
        // CONTEXTUAL REPEAT
        // =================================================

        if (
            lower == "repeat that" ||
            lower == "repeat it" ||
            lower == "repeat this" ||
            lower == "phir se bolo" ||
            lower == "dobara bolo" ||
            lower == "fir se bolo"
        ) {

            val history =
                AurixContextEngine
                    .getRecentHistory(10)

            val previous =
                history
                    .asReversed()
                    .firstOrNull {
                        it.role == "assistant" &&
                        it.text.isNotBlank()
                    }
                    ?.text
                    ?.trim()

            if (!previous.isNullOrBlank()) {

                return Resolution.DirectResponse(
                    previous
                )
            }
        }

        // =================================================
        // CONTEXTUAL YES / CONTINUE
        // =================================================

        if (
            lower == "yes" ||
            lower == "haan" ||
            lower == "ha" ||
            lower == "okay" ||
            lower == "ok" ||
            lower == "continue" ||
            lower == "aage karo" ||
            lower == "continue karo"
        ) {

            val history =
                AurixContextEngine
                    .getRecentHistory(10)

            val previousCommand =
                history
                    .asReversed()
                    .firstOrNull {
                        it.role == "user" &&
                        it.text.isNotBlank()
                    }
                    ?.text
                    ?.trim()

            if (!previousCommand.isNullOrBlank()) {

                return Resolution.Command(
                    previousCommand
                )
            }
        }

        // =================================================
        // CONTEXTUAL NO / CANCEL
        // =================================================

        if (
            lower == "no" ||
            lower == "nahi" ||
            lower == "cancel" ||
            lower == "cancel karo" ||
            lower == "rehne do" ||
            lower == "chhodo"
        ) {
            return Resolution.DirectResponse(
                "Theek hai Boss."
            )
        }

        // =================================================
        // DIRECT CONTEXT REFERENCES
        // =================================================

        if (
            lower.startsWith("same ") ||
            lower.startsWith("wahi ") ||
            lower.startsWith("usi ") ||
            lower.startsWith("phir ")
        ) {

            val previous =
                findPreviousUserCommand()

            if (!previous.isNullOrBlank()) {

                val target =
                    extractPreviousTarget(
                        previous
                    )

                if (!target.isNullOrBlank()) {

                    return Resolution.Command(
                        buildContextCommand(
                            lower,
                            target
                        )
                    )
                }
            }
        }

        // =================================================
        // NORMAL COMMAND
        // =================================================

        return Resolution.Command(
            original
        )
    }

    // =====================================================
    // PLAY FOLLOW-UP DETECTION
    // =====================================================

    private fun isPlayFollowUp(
        command: String
    ): Boolean {

        return command == "play it" ||
                command == "play that" ||
                command == "play this" ||
                command == "play it again" ||
                command == "play that again" ||
                command == "play this again" ||
                command == "isko chalao" ||
                command == "isko play karo" ||
                command == "ise chalao" ||
                command == "ise play karo" ||
                command == "usko chalao" ||
                command == "wahi chalao" ||
                command == "wahi play karo" ||
                command == "wahi song chalao" ||
                command == "same song chalao"
    }

    // =====================================================
    // OPEN FOLLOW-UP DETECTION
    // =====================================================

    private fun isOpenFollowUp(
        command: String
    ): Boolean {

        return command == "open it" ||
                command == "open that" ||
                command == "open this" ||
                command == "open it again" ||
                command == "open that again" ||
                command == "isko kholo" ||
                command == "isko open karo" ||
                command == "ise kholo" ||
                command == "ise open karo" ||
                command == "usko kholo" ||
                command == "wahi kholo" ||
                command == "wahi open karo"
    }

    // =====================================================
    // SEARCH FOLLOW-UP DETECTION
    // =====================================================

    private fun isSearchFollowUp(
        command: String
    ): Boolean {

        return command == "search it" ||
                command == "search that" ||
                command == "search this" ||
                command == "search it again" ||
                command == "isko search karo" ||
                command == "ise search karo" ||
                command == "wahi search karo" ||
                command == "wahi search kar"
    }

    // =====================================================
    // FIND PREVIOUS PLAYABLE TARGET
    // =====================================================

    private fun findPreviousPlayableTarget(): String? {

        val history =
            AurixContextEngine
                .getRecentHistory(15)

        val previous =
            history
                .asReversed()
                .firstOrNull {
                    it.role == "user" &&
                    isPlayableCommand(
                        it.text
                            .trim()
                            .lowercase()
                    )
                }
                ?.text
                ?.trim()

        if (previous.isNullOrBlank()) {
            return null
        }

        return extractPlayableTarget(
            previous
        )
    }

    // =====================================================
    // FIND PREVIOUS OPEN TARGET
    // =====================================================

    private fun findPreviousOpenTarget(): String? {

        val history =
            AurixContextEngine
                .getRecentHistory(15)

        val previous =
            history
                .asReversed()
                .firstOrNull {
                    it.role == "user" &&
                    isOpenCommand(
                        it.text
                            .trim()
                            .lowercase()
                    )
                }
                ?.text
                ?.trim()

        if (previous.isNullOrBlank()) {
            return null
        }

        return extractPreviousTarget(
            previous
        )
    }

    // =====================================================
    // FIND PREVIOUS SEARCH TARGET
    // =====================================================

    private fun findPreviousSearchTarget(): String? {

        val history =
            AurixContextEngine
                .getRecentHistory(15)

        val previous =
            history
                .asReversed()
                .firstOrNull {
                    it.role == "user" &&
                    isSearchCommand(
                        it.text
                            .trim()
                            .lowercase()
                    )
                }
                ?.text
                ?.trim()

        if (previous.isNullOrBlank()) {
            return null
        }

        return extractSearchTarget(
            previous
        )
    }

    // =====================================================
    // PLAYABLE COMMAND
    // =====================================================

    private fun isPlayableCommand(
        command: String
    ): Boolean {

        return command.startsWith("play ") ||
                command.startsWith("search youtube ") ||
                command.startsWith("youtube search ") ||
                command.startsWith("youtube par ")
    }

    // =====================================================
    // OPEN COMMAND
    // =====================================================

    private fun isOpenCommand(
        command: String
    ): Boolean {

        return command.startsWith("open ") ||
                command.startsWith("launch ") ||
                command.startsWith("start ") ||
                command.startsWith("run ") ||
                command.startsWith("use ") ||
                command.startsWith("show ")
    }

    // =====================================================
    // SEARCH COMMAND
    // =====================================================

    private fun isSearchCommand(
        command: String
    ): Boolean {

        return command.startsWith("search ") ||
                command.startsWith("search youtube ") ||
                command.startsWith("youtube search ") ||
                command.startsWith("youtube par ")
    }

    // =====================================================
    // PLAYABLE TARGET
    // =====================================================

    private fun extractPlayableTarget(
        previous: String
    ): String? {

        val lower =
            previous
                .trim()
                .lowercase()

        val prefixes =
            listOf(
                "play ",
                "search youtube ",
                "youtube search ",
                "youtube par "
            )

        for (prefix in prefixes) {

            if (
                lower.startsWith(prefix)
            ) {

                return previous
                    .substring(prefix.length)
                    .trim()
            }
        }

        return null
    }

    // =====================================================
    // SEARCH TARGET
    // =====================================================

    private fun extractSearchTarget(
        previous: String
    ): String? {

        val lower =
            previous
                .trim()
                .lowercase()

        val prefixes =
            listOf(
                "search youtube ",
                "youtube search ",
                "youtube par ",
                "search "
            )

        for (prefix in prefixes) {

            if (
                lower.startsWith(prefix)
            ) {

                return previous
                    .substring(prefix.length)
                    .trim()
            }
        }

        return null
    }

    // =====================================================
    // PREVIOUS USER COMMAND
    // =====================================================

    private fun findPreviousUserCommand(): String? {

        val history =
            AurixContextEngine
                .getRecentHistory(15)

        return history
            .asReversed()
            .firstOrNull {
                it.role == "user" &&
                it.text.isNotBlank()
            }
            ?.text
            ?.trim()
    }

    // =====================================================
    // PREVIOUS TARGET
    // =====================================================

    private fun extractPreviousTarget(
        previous: String
    ): String? {

        val lower =
            previous
                .trim()
                .lowercase()

        val prefixes =
            listOf(
                "open ",
                "launch ",
                "start ",
                "run ",
                "use ",
                "show "
            )

        for (prefix in prefixes) {

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
                "play "
            )
        ) {

            return previous
                .substring(
                    "play ".length
                )
                .trim()
        }

        return extractSearchTarget(
            previous
        )
    }

    // =====================================================
    // BUILD CONTEXT COMMAND
    // =====================================================

    private fun buildContextCommand(
        command: String,
        target: String
    ): String {

        return when {

            command.startsWith("same ") ->
                "play $target"

            command.startsWith("wahi ") ->
                "play $target"

            command.startsWith("usi ") ->
                "open $target"

            command.startsWith("phir ") ->
                "play $target"

            else ->
                target
        }
    }
}
