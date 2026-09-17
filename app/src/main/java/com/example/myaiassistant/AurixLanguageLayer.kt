package com.example.myaiassistant

import java.util.Locale

object AurixLanguageLayer {
    enum class Language { HINGLISH, ENGLISH }

    private val hinglishWords = setOf(
        "kya","hai","hain","ka","ki","ke","ko","se","mein","me","par",
        "aur","ya","kyun","kyon","kaise","kab","kahan","kitna","kitni",
        "kitne","batao","bata","samjhao","samjha","matlab","chahiye",
        "mujhe","mera","meri","mere","aap","tum","haan","nahi","nahin",
        "bol","bolo","kholo","band","karo","hota","hoti","hote","pehla",
        "pehli","dusra","doosra","teesra","niyam"
    )

    fun detectLanguage(question: String): Language {
        val words = question.lowercase(Locale.ENGLISH)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        return if (words.any { it in hinglishWords }) Language.HINGLISH else Language.ENGLISH
    }

    fun preferredLanguage(question: String): Language = detectLanguage(question)

    fun format(question: String, answer: String): String =
        AurixBilingualAnswerEngine.format(question, answer)
}
