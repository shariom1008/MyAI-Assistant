package com.example.myaiassistant

object AurixLocalIntentEngine {

    fun answer(command: String): String? {

        val q = command
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()

        if (q.isBlank()) return null

        // Greetings
        if (
            q == "hello" ||
            q == "hi" ||
            q == "hey" ||
            q == "namaste" ||
            q == "good morning" ||
            q == "good evening"
        ) {
            return "Hello Boss, main ready hoon."
        }

        // AURIX identity
        if (
            q.contains("tumhara naam") ||
            q.contains("your name") ||
            q == "who are you"
        ) {
            return "Mera naam AURIX hai, Boss."
        }

        // Current date/time type questions
        if (
            q.contains("what time") ||
            q.contains("current time") ||
            q.contains("kitne baje") ||
            q.contains("abhi time")
        ) {
            return "TIME_LOCAL"
        }

        // Simple science
        if (
            q.contains("what is gravity") ||
            q.contains("gravity kya hai")
        ) {
            return "Gravity woh force hai jo objects ko ek doosre ki taraf attract karti hai."
        }

        if (
            q.contains("what is oxygen") ||
            q.contains("oxygen kya hai")
        ) {
            return "Oxygen ek gas hai jo human breathing aur combustion ke liye important hai."
        }

        if (
            q.contains("what is water") ||
            q.contains("paani kya hai")
        ) {
            return "Water ka chemical formula H2O hai."
        }

        if (
            q.contains("what is photosynthesis") ||
            q.contains("photosynthesis kya hai")
        ) {
            return "Photosynthesis woh process hai jisme plants sunlight ki help se food banate hain."
        }

        // Earth
        if (
            q.contains("how many continents") ||
            q.contains("kitne continents")
        ) {
            return "Earth par 7 continents hain."
        }

        if (
            q.contains("how many oceans") ||
            q.contains("kitne oceans")
        ) {
            return "Earth par 5 major oceans hain."
        }

        // Human body
        if (
            q.contains("how many bones") ||
            q.contains("human body mein kitni bones") ||
            q.contains("insaan ke sharir mein kitni haddiyan")
        ) {
            return "Ek adult human body mein normally 206 bones hoti hain."
        }

        // Basic calculations
        val mathAnswer =
            AurixKnowledgeEngine.answer(q)

        if (!mathAnswer.isNullOrBlank()) {
            return mathAnswer
        }

        return null
    }
}
