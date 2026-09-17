package com.example.myaiassistant

import java.util.Locale

/**
 * Central language controller for AURIX.
 *
 * Current behavior:
 * - Hindi/Hinglish question -> HINGLISH
 * - English question -> ENGLISH
 * - Mixed question -> HINGLISH
 *
 * This layer does NOT call Gemini or any external service.
 * It only decides the preferred response language.
 */
object AurixLanguageLayer {

    enum class Language {
        HINGLISH,
        ENGLISH
    }

    private val hinglishWords = setOf(
        "kya",
        "hai",
        "hain",
        "ka",
        "ki",
        "ke",
        "ko",
        "se",
        "mein",
        "me",
        "par",
        "aur",
        "ya",
        "kyun",
        "kyon",
        "kaise",
        "kab",
        "kahan",
        "kitna",
        "kitni",
        "kitne",
        "batao",
        "bata",
        "samjhao",
        "samjha",
        "matlab",
        "chahiye",
        "mujhe",
        "mera",
        "meri",
        "mere",
        "aap",
        "tum",
        "haan",
        "nahi",
        "bol",
        "bolo",
        "kholo",
        "band",
        "karo",
        "kya",
        "hota",
        "hoti",
        "hote"
    )

    fun detectLanguage(
        question: String
    ): Language {

        val clean =
            question
                .lowercase(Locale.ENGLISH)
                .trim()

        if (clean.isBlank()) {
            return Language.HINGLISH
        }

        val words =
            clean
                .replace(
                    Regex("[^a-z0-9\\s]"),
                    " "
                )
                .split(
                    Regex("\\s+")
                )
                .filter {
                    it.isNotBlank()
                }

        val hinglishCount =
            words.count {
                hinglishWords.contains(it)
            }

        /*
         * Even one strong Hindi/Hinglish marker
         * makes the response Hinglish.
         */
        if (hinglishCount > 0) {
            return Language.HINGLISH
        }

        return Language.ENGLISH
    }

    fun preferredLanguage(
        question: String
    ): Language {
        return detectLanguage(question)
    }

    /**
     * Keeps the current answer intact.
     *
     * Translation is intentionally not performed here because
     * AURIX must not silently call Gemini or another online
     * service just to translate a response.
     */
    fun format(
        question: String,
        answer: String
    ): String {

        if (answer.isBlank()) {
            return answer
        }

        return when (
            detectLanguage(question)
        ) {

            Language.HINGLISH -> {
                answer
            }

            Language.ENGLISH -> {
                answer
            }
        }
    }
}
