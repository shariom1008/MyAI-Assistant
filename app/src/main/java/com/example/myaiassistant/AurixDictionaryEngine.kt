package com.example.myaiassistant

object AurixDictionaryEngine {

    private data class WordMeaning(
        val meaning: String,
        val hindi: String,
        val synonyms: String,
        val antonyms: String
    )

    private val dictionary = mapOf(

        "intelligent" to WordMeaning(
            "having a good ability to learn, understand and think",
            "बुद्धिमान",
            "smart, clever, bright",
            "stupid, foolish"
        ),

        "smart" to WordMeaning(
            "clever and quick to understand things",
            "होशियार",
            "intelligent, clever, bright",
            "stupid, foolish"
        ),

        "clever" to WordMeaning(
            "quick to learn, understand or solve problems",
            "चतुर, होशियार",
            "smart, intelligent, bright",
            "stupid, foolish"
        ),

        "happy" to WordMeaning(
            "feeling pleasure, joy or satisfaction",
            "खुश",
            "joyful, cheerful, glad",
            "sad, unhappy"
        ),

        "sad" to WordMeaning(
            "feeling unhappy or sorrowful",
            "दुखी",
            "unhappy, sorrowful",
            "happy, joyful"
        ),

        "brave" to WordMeaning(
            "having courage and not being afraid of danger",
            "बहादुर",
            "courageous, fearless, bold",
            "cowardly, timid"
        ),

        "honest" to WordMeaning(
            "truthful and sincere",
            "ईमानदार",
            "truthful, sincere, trustworthy",
            "dishonest, deceitful"
        ),

        "beautiful" to WordMeaning(
            "pleasing and attractive in appearance",
            "सुंदर",
            "pretty, attractive, lovely",
            "ugly"
        ),

        "difficult" to WordMeaning(
            "not easy to do, understand or solve",
            "कठिन",
            "hard, challenging, tough",
            "easy, simple"
        ),

        "easy" to WordMeaning(
            "not difficult; requiring little effort",
            "आसान",
            "simple, effortless",
            "difficult, hard"
        ),

        "love" to WordMeaning(
            "a strong feeling of affection or deep care",
            "प्यार, प्रेम",
            "affection, care, fondness",
            "hate, dislike"
        ),

        "friend" to WordMeaning(
            "a person whom you know and like",
            "दोस्त, मित्र",
            "companion, buddy, pal",
            "enemy"
        ),

        "enemy" to WordMeaning(
            "a person who is hostile or opposed to someone",
            "दुश्मन, शत्रु",
            "opponent, rival, adversary",
            "friend, ally"
        ),

        "success" to WordMeaning(
            "the achievement of a desired goal",
            "सफलता",
            "achievement, victory, accomplishment",
            "failure, defeat"
        ),

        "failure" to WordMeaning(
            "lack of success in achieving something",
            "असफलता",
            "defeat, unsuccessful result",
            "success, achievement"
        ),

        "strong" to WordMeaning(
            "having great physical or mental power",
            "मजबूत",
            "powerful, tough, sturdy",
            "weak, fragile"
        ),

        "weak" to WordMeaning(
            "lacking physical or mental strength",
            "कमजोर",
            "feeble, fragile, powerless",
            "strong, powerful"
        ),

        "fast" to WordMeaning(
            "moving or happening quickly",
            "तेज",
            "quick, rapid, speedy",
            "slow"
        ),

        "slow" to WordMeaning(
            "moving or happening at a low speed",
            "धीमा",
            "unhurried, sluggish",
            "fast, quick"
        ),

        "angry" to WordMeaning(
            "feeling strong annoyance or displeasure",
            "गुस्सा, क्रोधित",
            "furious, annoyed, irritated",
            "calm, pleased"
        ),

        "calm" to WordMeaning(
            "peaceful and without strong emotions",
            "शांत",
            "peaceful, relaxed, quiet",
            "angry, agitated"
        ),

        "important" to WordMeaning(
            "having great value, meaning or influence",
            "महत्वपूर्ण",
            "significant, valuable, essential",
            "unimportant, insignificant"
        ),

        "possible" to WordMeaning(
            "able to happen or be done",
            "संभव",
            "achievable, feasible",
            "impossible"
        ),

        "impossible" to WordMeaning(
            "not able to happen or be done",
            "असंभव",
            "unachievable, infeasible",
            "possible"
        ),

        "ugly" to WordMeaning(
            "unpleasant or unattractive in appearance",
            "बदसूरत",
            "unattractive, unpleasant",
            "beautiful, attractive"
        ),

        "rich" to WordMeaning(
            "having a lot of money or valuable possessions",
            "अमीर",
            "wealthy, prosperous",
            "poor"
        ),

        "poor" to WordMeaning(
            "having little money or resources",
            "गरीब",
            "needy, impoverished",
            "rich, wealthy"
        ),

        "large" to WordMeaning(
            "big in size or amount",
            "बड़ा",
            "big, huge, enormous",
            "small, tiny"
        ),

        "small" to WordMeaning(
            "little in size or amount",
            "छोटा",
            "little, tiny, compact",
            "large, huge"
        ),

        "begin" to WordMeaning(
            "to start doing something",
            "शुरू करना",
            "start, commence",
            "end, finish"
        ),

        "end" to WordMeaning(
            "to finish or stop something",
            "समाप्त करना",
            "finish, conclude, stop",
            "begin, start"
        ),

        "help" to WordMeaning(
            "to make something easier for someone",
            "मदद करना",
            "assist, support, aid",
            "hinder, obstruct"
        ),

        "problem" to WordMeaning(
            "a difficult situation or question that needs a solution",
            "समस्या",
            "difficulty, issue, trouble",
            "solution"
        ),

        "solution" to WordMeaning(
            "an answer to a problem",
            "समाधान",
            "answer, resolution, remedy",
            "problem"
        ),

        "knowledge" to WordMeaning(
            "information and understanding gained through learning",
            "ज्ञान",
            "understanding, awareness, information",
            "ignorance"
        ),

        "power" to WordMeaning(
            "the ability or capacity to do something",
            "शक्ति, ताकत",
            "strength, force, authority",
            "weakness"
        ),

        "dangerous" to WordMeaning(
            "likely to cause harm or injury",
            "खतरनाक",
            "risky, hazardous, unsafe",
            "safe, harmless"
        ),

        "safe" to WordMeaning(
            "protected from danger or harm",
            "सुरक्षित",
            "secure, protected, harmless",
            "dangerous, unsafe"
        ),

        "careful" to WordMeaning(
            "giving attention to avoid mistakes or danger",
            "सावधान",
            "cautious, attentive, alert",
            "careless, reckless"
        ),

        "careless" to WordMeaning(
            "not giving enough attention",
            "लापरवाह",
            "negligent, inattentive",
            "careful, cautious"
        ),

        "quick" to WordMeaning(
            "moving or doing something with speed",
            "तेज, शीघ्र",
            "fast, rapid, speedy",
            "slow"
        ),

        "quiet" to WordMeaning(
            "making little or no noise",
            "शांत",
            "silent, peaceful, calm",
            "loud, noisy"
        ),

        "loud" to WordMeaning(
            "making a lot of noise",
            "तेज आवाज वाला",
            "noisy, booming",
            "quiet, silent"
        ),

        "clean" to WordMeaning(
            "free from dirt or unwanted matter",
            "साफ",
            "pure, spotless, tidy",
            "dirty, unclean"
        ),

        "dirty" to WordMeaning(
            "covered with dirt or not clean",
            "गंदा",
            "unclean, filthy",
            "clean"
        ),

        "new" to WordMeaning(
            "recently made, created or acquired",
            "नया",
            "recent, fresh, modern",
            "old"
        ),

        "old" to WordMeaning(
            "having existed for a long time",
            "पुराना",
            "aged, ancient",
            "new, young"
        ),

        "young" to WordMeaning(
            "having lived for a relatively short time",
            "युवा, जवान",
            "youthful, juvenile",
            "old, aged"
        ),

        "kind" to WordMeaning(
            "having a friendly and caring nature",
            "दयालु",
            "caring, gentle, helpful",
            "cruel, unkind"
        ),

        "cruel" to WordMeaning(
            "causing pain or suffering without concern",
            "क्रूर",
            "harsh, brutal, heartless",
            "kind, compassionate"
        ),

        "simple" to WordMeaning(
            "easy to understand or do",
            "सरल",
            "easy, basic, straightforward",
            "complex, difficult"
        ),

        "complex" to WordMeaning(
            "complicated and not easy to understand",
            "जटिल",
            "complicated, difficult",
            "simple, easy"
        ),

        "correct" to WordMeaning(
            "free from mistakes or errors",
            "सही",
            "right, accurate, proper",
            "wrong, incorrect"
        ),

        "wrong" to WordMeaning(
            "not correct or accurate",
            "गलत",
            "incorrect, mistaken",
            "correct, right"
        ),

        "true" to WordMeaning(
            "in accordance with fact or reality",
            "सच्चा, सत्य",
            "correct, factual, genuine",
            "false, untrue"
        ),

        "false" to WordMeaning(
            "not true or correct",
            "झूठा, असत्य",
            "untrue, incorrect, fake",
            "true, genuine"
        ),

        "future" to WordMeaning(
            "the time that is yet to come",
            "भविष्य",
            "tomorrow, coming time",
            "past"
        ),

        "past" to WordMeaning(
            "the time that has already happened",
            "अतीत, बीता हुआ समय",
            "history, previous time",
            "future"
        ),

        "present" to WordMeaning(
            "the time happening now",
            "वर्तमान",
            "now, current time",
            "past, future"
        ),

        "freedom" to WordMeaning(
            "the state of being free",
            "स्वतंत्रता",
            "liberty, independence",
            "slavery, restriction"
        ),

        "respect" to WordMeaning(
            "a feeling of admiration or regard",
            "सम्मान",
            "admiration, regard, honor",
            "disrespect, contempt"
        ),

        "trust" to WordMeaning(
            "firm belief in someone's reliability or truth",
            "विश्वास",
            "confidence, faith, belief",
            "doubt, distrust"
        ),

        "dream" to WordMeaning(
            "a strongly desired goal or aspiration",
            "सपना",
            "ambition, aspiration, vision",
            "reality"
        ),

        "life" to WordMeaning(
            "the condition of being alive",
            "जीवन",
            "existence, living",
            "death"
        ),

        "death" to WordMeaning(
            "the end of life",
            "मृत्यु",
            "passing, demise",
            "life"
        ),

        "health" to WordMeaning(
            "the condition of physical and mental well-being",
            "स्वास्थ्य",
            "well-being, fitness",
            "illness, disease"
        ),

        "famous" to WordMeaning(
            "known by many people",
            "प्रसिद्ध",
            "well-known, renowned, popular",
            "unknown, obscure"
        ),

        "popular" to WordMeaning(
            "liked or admired by many people",
            "लोकप्रिय",
            "famous, liked, well-liked",
            "unpopular"
        ),

        "honor" to WordMeaning(
            "high respect or great esteem",
            "सम्मान",
            "respect, dignity, esteem",
            "disgrace, dishonor"
        )
    )

    fun answer(command: String): String? {

        val c = command
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) {
            return null
        }

        val hindi = isHindi(c)

        // ---------------------------------------------------------
        // PURE HINDI WORDS
        // ---------------------------------------------------------

        val hindiWords = mapOf(
            "इंटेलिजेंट" to "intelligent",
            "स्मार्ट" to "smart",
            "क्लेवर" to "clever",
            "हैप्पी" to "happy",
            "सैड" to "sad",
            "बहादुर" to "brave",
            "ईमानदार" to "honest",
            "सुंदर" to "beautiful",
            "कठिन" to "difficult",
            "आसान" to "easy",
            "प्यार" to "love",
            "प्रेम" to "love",
            "दोस्त" to "friend",
            "दुश्मन" to "enemy",
            "सफलता" to "success",
            "असफलता" to "failure",
            "मजबूत" to "strong",
            "कमजोर" to "weak",
            "तेज" to "fast",
            "धीमा" to "slow",
            "गुस्सा" to "angry",
            "शांत" to "calm",
            "महत्वपूर्ण" to "important",
            "संभव" to "possible",
            "असंभव" to "impossible",
            "बदसूरत" to "ugly",
            "अमीर" to "rich",
            "गरीब" to "poor",
            "बड़ा" to "large",
            "छोटा" to "small",
            "शुरू" to "begin",
            "समाप्त" to "end",
            "मदद" to "help",
            "समस्या" to "problem",
            "समाधान" to "solution",
            "ज्ञान" to "knowledge",
            "ताकत" to "power",
            "शक्ति" to "power",
            "खतरनाक" to "dangerous",
            "सुरक्षित" to "safe",
            "सावधान" to "careful",
            "लापरवाह" to "careless",
            "शीघ्र" to "quick",
            "साफ" to "clean",
            "गंदा" to "dirty",
            "नया" to "new",
            "पुराना" to "old",
            "युवा" to "young",
            "जवान" to "young",
            "दयालु" to "kind",
            "क्रूर" to "cruel",
            "सरल" to "simple",
            "जटिल" to "complex",
            "सही" to "correct",
            "गलत" to "wrong",
            "सच्चा" to "true",
            "सत्य" to "true",
            "झूठा" to "false",
            "भविष्य" to "future",
            "अतीत" to "past",
            "वर्तमान" to "present",
            "स्वतंत्रता" to "freedom",
            "सम्मान" to "respect",
            "विश्वास" to "trust",
            "सपना" to "dream",
            "जीवन" to "life",
            "मृत्यु" to "death",
            "स्वास्थ्य" to "health",
            "प्रसिद्ध" to "famous",
            "लोकप्रिय" to "popular"
        )

        // ---------------------------------------------------------
        // HINDI MEANING
        // Examples:
        // इंटेलिजेंट का मतलब क्या है
        // इंटेलिजेंट का अर्थ क्या है
        // इंटेलिजेंट का meaning क्या है
        // ---------------------------------------------------------

        Regex(
            """([^\s]+)\s+(?:का|की|के)\s+(?:मतलब|अर्थ|meaning)"""
        ).find(c)?.let {

            val hindiWord = it.groupValues[1]
            val englishWord = hindiWords[hindiWord]

            if (englishWord != null) {
                dictionary[englishWord]?.let { entry ->
                    return "$hindiWord का मतलब: ${entry.hindi}।"
                }
            }
        }

        // ---------------------------------------------------------
        // HINDI "WORD MEANING"
        // ---------------------------------------------------------

        Regex(
            """(?:meaning|मतलब|अर्थ)\s+(?:of|का|का\s+)?([^\s]+)"""
        ).find(c)?.let {

            val word = it.groupValues[1]
            val englishWord = hindiWords[word] ?: word

            dictionary[englishWord]?.let { entry ->
                return if (hindi) {
                    "$word का मतलब: ${entry.hindi}।"
                } else {
                    "The meaning of $englishWord is: ${entry.meaning}. Hindi meaning: ${entry.hindi}."
                }
            }
        }

        // ---------------------------------------------------------
        // ENGLISH MEANING / MATLAB / ARTH
        // ---------------------------------------------------------

        val meaningPatterns = listOf(
            Regex("""meaning\s+of\s+([a-z]+)"""),
            Regex("""meaning\s+of\s+the\s+word\s+([a-z]+)"""),
            Regex("""matlab\s+(?:of\s+)?([a-z]+)"""),
            Regex("""([a-z]+)\s+ka\s+meaning"""),
            Regex("""([a-z]+)\s+ka\s+matlab"""),
            Regex("""([a-z]+)\s+ka\s+arth"""),
            Regex("""what\s+does\s+([a-z]+)\s+mean"""),
            Regex("""define\s+([a-z]+)"""),
            Regex("""definition\s+of\s+([a-z]+)""")
        )

        for (pattern in meaningPatterns) {

            pattern.find(c)?.let {

                val word = it.groupValues[1]

                dictionary[word]?.let { entry ->

                    return if (hindi) {
                        "$word का मतलब: ${entry.hindi}।"
                    } else {
                        "The meaning of $word is: ${entry.meaning}. Hindi meaning: ${entry.hindi}."
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // SYNONYMS
        // ---------------------------------------------------------

        Regex(
            """(?:synonym|synonyms)\s+(?:of\s+)?([a-z]+)"""
        ).find(c)?.let {

            val word = it.groupValues[1]

            dictionary[word]?.let { entry ->
                return if (hindi) {
                    "$word के synonyms हैं: ${entry.synonyms}।"
                } else {
                    "Synonyms of $word are ${entry.synonyms}."
                }
            }
        }

        Regex(
            """([a-z]+)\s+(?:ke\s+)?synonyms"""
        ).find(c)?.let {

            val word = it.groupValues[1]

            dictionary[word]?.let { entry ->
                return if (hindi) {
                    "$word के synonyms हैं: ${entry.synonyms}।"
                } else {
                    "Synonyms of $word are ${entry.synonyms}."
                }
            }
        }

        // ---------------------------------------------------------
        // ANTONYMS
        // ---------------------------------------------------------

        Regex(
            """(?:antonym|antonyms)\s+(?:of\s+)?([a-z]+)"""
        ).find(c)?.let {

            val word = it.groupValues[1]

            dictionary[word]?.let { entry ->
                return if (hindi) {
                    "$word के antonyms हैं: ${entry.antonyms}।"
                } else {
                    "Antonyms of $word are ${entry.antonyms}."
                }
            }
        }

        Regex(
            """([a-z]+)\s+(?:ke\s+)?antonyms"""
        ).find(c)?.let {

            val word = it.groupValues[1]

            dictionary[word]?.let { entry ->
                return if (hindi) {
                    "$word के antonyms हैं: ${entry.antonyms}।"
                } else {
                    "Antonyms of $word are ${entry.antonyms}."
                }
            }
        }

        // ---------------------------------------------------------
        // SIMPLE "WORD MEANING"
        // ---------------------------------------------------------

        Regex(
            """([a-z]+)\s+meaning"""
        ).find(c)?.let {

            val word = it.groupValues[1]

            dictionary[word]?.let { entry ->
                return if (hindi) {
                    "$word का मतलब: ${entry.hindi}।"
                } else {
                    "The meaning of $word is: ${entry.meaning}. Hindi meaning: ${entry.hindi}."
                }
            }
        }

        return null
    }

    // -------------------------------------------------------------
    // HINDI LANGUAGE DETECTION
    // -------------------------------------------------------------

    private fun isHindi(command: String): Boolean {
        return command.any { it in '\u0900'..'\u097F' }
    }
}
