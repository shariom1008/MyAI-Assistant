package com.example.myaiassistant.knowledge

import android.content.Context

/**
 * Stage 1 bridge between successful AI answers and AURIX's existing local knowledge cache.
 *
 * This stores reusable answers on-device; it does not train or fine-tune Gemini.
 * Since Gemini answers currently arrive without verifiable source metadata, entries
 * are deliberately marked MEDIUM confidence and expire after a limited period.
 */
class AurixKnowledgeLearning(context: Context) {

    private val cache = AurixKnowledgeCache(context.applicationContext)

    /**
     * Returns true only when the answer passes conservative storage checks.
     */
    fun remember(question: String, answer: String): Boolean {
        val q = question.trim()
        val a = answer.trim()

        if (!isSuitableForStorage(q, a)) return false

        val ttl = if (isTimeSensitive(q)) {
            SIX_HOURS_MS
        } else {
            THIRTY_DAYS_MS
        }

        return try {
            cache.save(
                question = q,
                answer = a,
                confidence = ConfidenceLevel.MEDIUM,
                sources = emptyList(),
                ttlMillis = ttl
            )
            true
        } catch (_: Exception) {
            // Learning must never break voice response delivery.
            false
        }
    }

    private fun isSuitableForStorage(question: String, answer: String): Boolean {
        if (question.length < 4 || answer.length < 80 || answer.length > MAX_ANSWER_LENGTH) {
            return false
        }

        val q = question.lowercase()
        val a = answer.lowercase()

        // Do not cache greetings, creative drafts, or transformation requests as facts.
        if (NON_FACTUAL_REQUEST.containsMatchIn(q)) return false

        // Avoid storing sensitive personal data or high-stakes advice without verification.
        if (SENSITIVE_OR_HIGH_STAKES.containsMatchIn(q)) return false

        // Avoid caching API failures, empty fallbacks, or explicit uncertainty responses.
        if (FAILURE_OR_NO_ANSWER.containsMatchIn(a)) return false

        // Reject answers that look like a request for clarification rather than knowledge.
        if (CLARIFICATION_ONLY.matches(answer)) return false

        return true
    }

    private fun isTimeSensitive(question: String): Boolean =
        TIME_SENSITIVE.containsMatchIn(question.lowercase())

    private companion object {
        const val SIX_HOURS_MS = 6L * 60L * 60L * 1000L
        const val THIRTY_DAYS_MS = 30L * 24L * 60L * 60L * 1000L
        const val MAX_ANSWER_LENGTH = 12_000

        val NON_FACTUAL_REQUEST = Regex(
            """\b(write|create|generate|compose|draft|translate|rewrite|summari[sz]e|make a song|lyrics|shayari|poem|story|joke|caption|email|application|rap prompt)\b""",
            RegexOption.IGNORE_CASE
        )

        val SENSITIVE_OR_HIGH_STAKES = Regex(
            """\b(password|passcode|otp|aadhaar|pan number|bank details|credit card|medical advice|diagnos\w*|treatment|dosage|dose|medicine|medication|symptom\w*|disease|fever|pregnan\w*|suicide|self[- ]harm|poison|chemical handling|investment advice|legal advice|tax advice)\b|दवाई|दवा|इलाज|लक्षण|बुखार|खुराक|पासवर्ड|ओटीपी""",
            RegexOption.IGNORE_CASE
        )

        val FAILURE_OR_NO_ANSWER = Regex(
            """\b(i cannot answer|i can't answer|i don't know|i do not know|unable to process|an error occurred|something went wrong|api error|request failed|please try again later)\b""",
            RegexOption.IGNORE_CASE
        )

        val CLARIFICATION_ONLY = Regex(
            """^\s*(could you|can you|please)\s+(clarify|rephrase|provide more details)[^.!?]*[.!?]?\s*$""",
            RegexOption.IGNORE_CASE
        )

        val TIME_SENSITIVE = Regex(
            """\b(today|tonight|tomorrow|now|current|currently|latest|breaking|news|weather|price|rate|stock|exchange rate|live score|result|schedule|available now|open now|this week|this month|202[6-9])\b|आज|अभी|कल|मौसम|ताज़ा खबर|रेट|कीमत""",
            RegexOption.IGNORE_CASE
        )
    }
}
