package com.example.myaiassistant

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

object AurixMathEngine {

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
        // HINDI / HINGLISH - PERCENTAGE OF
        // ---------------------------------------------------------

        Regex(
            """(\d+(?:\.\d+)?)\s*(?:का|का\s+|ki|ka)\s*(\d+(?:\.\d+)?)\s*(?:प्रतिशत|प्रतिशत\s+|percent|percentage|%)"""
        ).find(c)?.let {

            val base = it.groupValues[1].toDouble()
            val percentage = it.groupValues[2].toDouble()
            val result = percentage * base / 100.0

            return if (hindi) {
                "$base का $percentage प्रतिशत ${formatNumber(result)} है।"
            } else {
                "$percentage percent of $base is ${formatNumber(result)}."
            }
        }

        Regex(
            """(\d+(?:\.\d+)?)\s*(?:percent|percentage|%)\s*(?:of|ka)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val percentage = it.groupValues[1].toDouble()
            val base = it.groupValues[2].toDouble()
            val result = percentage * base / 100.0

            return if (hindi) {
                "$base का $percentage प्रतिशत ${formatNumber(result)} है।"
            } else {
                "$percentage percent of $base is ${formatNumber(result)}."
            }
        }

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:percent|percentage|%)\s*(?:of|ka)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val percentage = it.groupValues[1].toDouble()
            val base = it.groupValues[2].toDouble()
            val result = percentage * base / 100.0

            return if (hindi) {
                "$base का $percentage प्रतिशत ${formatNumber(result)} है।"
            } else {
                "$percentage percent of $base is ${formatNumber(result)}."
            }
        }

        // ---------------------------------------------------------
        // PURE HINDI PERCENTAGE
        // ---------------------------------------------------------

        Regex(
            """(\d+(?:\.\d+)?)\s*का\s*(\d+(?:\.\d+)?)\s*प्रतिशत"""
        ).find(c)?.let {

            val base = it.groupValues[1].toDouble()
            val percentage = it.groupValues[2].toDouble()
            val result = percentage * base / 100.0

            return "$base का $percentage प्रतिशत ${formatNumber(result)} है।"
        }

        // ---------------------------------------------------------
        // HCF / GCD
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:h\s*c\s*f|g\s*c\s*d)(?:\s+of)?\s+(\d+)\s+(?:and|aur|और)\s+(\d+)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toLong()
            val b = it.groupValues[2].toLong()

            return if (hindi) {
                "$a और $b का HCF ${gcd(a, b)} है।"
            } else {
                "HCF of $a and $b is ${gcd(a, b)}."
            }
        }

        Regex(
            """(\d+)\s+(?:and|aur|और)\s+(\d+)\s*(?:का|का\s+|ka|ki)?\s*(?:h\s*c\s*f|g\s*c\s*d)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toLong()
            val b = it.groupValues[2].toLong()

            return if (hindi) {
                "$a और $b का HCF ${gcd(a, b)} है।"
            } else {
                "HCF of $a and $b is ${gcd(a, b)}."
            }
        }

        // ---------------------------------------------------------
        // EVEN / ODD
        // ---------------------------------------------------------

        Regex(
            """(?:is\s+)?(\d+)\s+(?:even|odd)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toLong()

            return if (number % 2 == 0L) {
                if (hindi) "$number सम संख्या है।"
                else "$number is even."
            } else {
                if (hindi) "$number विषम संख्या है।"
                else "$number is odd."
            }
        }

        // ---------------------------------------------------------
        // PRIME NUMBER
        // ---------------------------------------------------------

        Regex(
            """(?:is\s+)?(\d+)\s+(?:a\s+)?prime(?:\s+number)?"""
        ).find(c)?.let {

            val number = it.groupValues[1].toLong()

            return if (isPrime(number)) {
                if (hindi) "$number एक अभाज्य संख्या है।"
                else "$number is a prime number."
            } else {
                if (hindi) "$number अभाज्य संख्या नहीं है।"
                else "$number is not a prime number."
            }
        }

        // ---------------------------------------------------------
        // FACTORIAL
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+)\s*(?:factorial|!)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toInt()

            if (number > 20) {
                return if (hindi) {
                    "मैं केवल 20 तक का factorial calculate कर सकता हूँ।"
                } else {
                    "I can calculate factorial only up to 20."
                }
            }

            return if (hindi) {
                "$number का factorial ${factorial(number)} है।"
            } else {
                "$number factorial is ${factorial(number)}."
            }
        }

        // ---------------------------------------------------------
        // LCM
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:lcm)(?:\s+of)?\s+(\d+)\s+(?:and|aur|और)\s+(\d+)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toLong()
            val b = it.groupValues[2].toLong()

            return if (hindi) {
                "$a और $b का LCM ${lcm(a, b)} है।"
            } else {
                "LCM of $a and $b is ${lcm(a, b)}."
            }
        }

        // ---------------------------------------------------------
        // AVERAGE
        // ---------------------------------------------------------

        Regex(
            """average\s+(?:of\s+)?(\d+(?:\.\d+)?)\s+(?:and|aur|और)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            val result = (a + b) / 2.0

            return if (hindi) {
                "औसत ${formatNumber(result)} है।"
            } else {
                "The average is ${formatNumber(result)}."
            }
        }

        // ---------------------------------------------------------
        // RATIO
        // ---------------------------------------------------------

        Regex(
            """ratio\s+(?:of\s+)?(\d+(?:\.\d+)?)\s+(?:and|to|aur|और)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            if (b == 0.0) {
                return if (hindi) {
                    "अनुपात में दूसरी संख्या zero नहीं हो सकती।"
                } else {
                    "Ratio cannot have zero as the second value."
                }
            }

            val g = gcd(a.toLong(), b.toLong())

            return if (hindi) {
                "अनुपात ${formatNumber(a / g)}:${formatNumber(b / g)} है।"
            } else {
                "The ratio is ${formatNumber(a / g)}:${formatNumber(b / g)}."
            }
        }

        // ---------------------------------------------------------
        // PERCENTAGE INCREASE
        // ---------------------------------------------------------

        Regex(
            """(?:percentage\s+)?increase\s+(?:from\s+)?(\d+(?:\.\d+)?)\s+(?:to|by)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val oldValue = it.groupValues[1].toDouble()
            val newValue = it.groupValues[2].toDouble()

            if (oldValue == 0.0) {
                return if (hindi) {
                    "Zero से percentage increase calculate नहीं किया जा सकता।"
                } else {
                    "Percentage increase cannot be calculated from zero."
                }
            }

            val result = ((newValue - oldValue) / oldValue) * 100.0

            return if (hindi) {
                "Percentage increase ${formatNumber(result)} प्रतिशत है।"
            } else {
                "The percentage increase is ${formatNumber(result)} percent."
            }
        }

        // ---------------------------------------------------------
        // PERCENTAGE DECREASE
        // ---------------------------------------------------------

        Regex(
            """(?:percentage\s+)?decrease\s+(?:from\s+)?(\d+(?:\.\d+)?)\s+(?:to|by)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val oldValue = it.groupValues[1].toDouble()
            val newValue = it.groupValues[2].toDouble()

            if (oldValue == 0.0) {
                return if (hindi) {
                    "Zero से percentage decrease calculate नहीं किया जा सकता।"
                } else {
                    "Percentage decrease cannot be calculated from zero."
                }
            }

            val result = ((oldValue - newValue) / oldValue) * 100.0

            return if (hindi) {
                "Percentage decrease ${formatNumber(result)} प्रतिशत है।"
            } else {
                "The percentage decrease is ${formatNumber(result)} percent."
            }
        }

        // ---------------------------------------------------------
        // SQUARE ROOT
        // ---------------------------------------------------------

        Regex(
            """(?:square\s+root\s+of|sqrt)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toDouble()

            if (number < 0) {
                return if (hindi) {
                    "Negative number का square root real number नहीं है।"
                } else {
                    "Square root of a negative number is not a real number."
                }
            }

            return if (hindi) {
                "Square root ${formatNumber(sqrt(number))} है।"
            } else {
                "The square root is ${formatNumber(sqrt(number))}."
            }
        }

        // ---------------------------------------------------------
        // SQUARE
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:square\s+of)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toDouble()

            return if (hindi) {
                "$number का square ${formatNumber(number.pow(2))} है।"
            } else {
                "The square of $number is ${formatNumber(number.pow(2))}."
            }
        }

        // ---------------------------------------------------------
        // CUBE
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:cube\s+of)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toDouble()

            return if (hindi) {
                "$number का cube ${formatNumber(number.pow(3))} है।"
            } else {
                "The cube of $number is ${formatNumber(number.pow(3))}."
            }
        }

        // ---------------------------------------------------------
        // POWER
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:power|raised\s+to)\s*(?:of|the)?\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val base = it.groupValues[1].toDouble()
            val exponent = it.groupValues[2].toDouble()

            val result = base.pow(exponent)

            return if (hindi) {
                "$base की power $exponent ${formatNumber(result)} है।"
            } else {
                "$base to the power of $exponent is ${formatNumber(result)}."
            }
        }

        // ---------------------------------------------------------
        // ADDITION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:\+|plus)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            return if (hindi) {
                "${formatNumber(a)} और ${formatNumber(b)} का जोड़ ${formatNumber(a + b)} है।"
            } else {
                "${formatNumber(a)} plus ${formatNumber(b)} is ${formatNumber(a + b)}."
            }
        }

        // ---------------------------------------------------------
        // SUBTRACTION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:-|minus)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            return if (hindi) {
                "${formatNumber(a)} में से ${formatNumber(b)} घटाने पर ${formatNumber(a - b)} आता है।"
            } else {
                "${formatNumber(a)} minus ${formatNumber(b)} is ${formatNumber(a - b)}."
            }
        }

        // ---------------------------------------------------------
        // MULTIPLICATION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:\*|x|×|times|multiply(?:\s+by)?)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            return if (hindi) {
                "${formatNumber(a)} और ${formatNumber(b)} का गुणा ${formatNumber(a * b)} है।"
            } else {
                "${formatNumber(a)} multiplied by ${formatNumber(b)} is ${formatNumber(a * b)}."
            }
        }

        // ---------------------------------------------------------
        // DIVISION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:/|÷|divided\s+by)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            if (b == 0.0) {
                return if (hindi) {
                    "Zero से division संभव नहीं है।"
                } else {
                    "Division by zero is not possible."
                }
            }

            return if (hindi) {
                "${formatNumber(a)} को ${formatNumber(b)} से divide करने पर ${formatNumber(a / b)} आता है।"
            } else {
                "${formatNumber(a)} divided by ${formatNumber(b)} is ${formatNumber(a / b)}."
            }
        }

        // ---------------------------------------------------------
        // CIRCLE AREA
        // ---------------------------------------------------------

        Regex(
            """(?:area\s+of\s+)?(?:a\s+)?circle\s+(?:with\s+)?radius\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val radius = it.groupValues[1].toDouble()
            val area = PI * radius * radius

            return if (hindi) {
                "Circle का area ${formatNumber(area)} square units है।"
            } else {
                "The area of the circle is ${formatNumber(area)} square units."
            }
        }

        // ---------------------------------------------------------
        // CIRCLE CIRCUMFERENCE
        // ---------------------------------------------------------

        Regex(
            """(?:circumference\s+of\s+)?(?:a\s+)?circle\s+(?:with\s+)?radius\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val radius = it.groupValues[1].toDouble()
            val circumference = 2 * PI * radius

            return if (hindi) {
                "Circle की circumference ${formatNumber(circumference)} units है।"
            } else {
                "The circumference of the circle is ${formatNumber(circumference)} units."
            }
        }

        // ---------------------------------------------------------
        // RECTANGLE AREA
        // ---------------------------------------------------------

        Regex(
            """(?:area\s+of\s+)?rectangle\s+(?:with\s+)?length\s+(\d+(?:\.\d+)?)\s+(?:and|aur|और)\s+width\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val length = it.groupValues[1].toDouble()
            val width = it.groupValues[2].toDouble()

            val area = length * width

            return if (hindi) {
                "Rectangle का area ${formatNumber(area)} square units है।"
            } else {
                "The area of the rectangle is ${formatNumber(area)} square units."
            }
        }

        // ---------------------------------------------------------
        // RECTANGLE PERIMETER
        // ---------------------------------------------------------

        Regex(
            """(?:perimeter\s+of\s+)?rectangle\s+(?:with\s+)?length\s+(\d+(?:\.\d+)?)\s+(?:and|aur|और)\s+width\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val length = it.groupValues[1].toDouble()
            val width = it.groupValues[2].toDouble()

            val perimeter = 2 * (length + width)

            return if (hindi) {
                "Rectangle का perimeter ${formatNumber(perimeter)} units है।"
            } else {
                "The perimeter of the rectangle is ${formatNumber(perimeter)} units."
            }
        }

        return null
    }

    // -------------------------------------------------------------
    // LANGUAGE DETECTION
    // -------------------------------------------------------------

    private fun isHindi(command: String): Boolean {

        // Hindi Devanagari script
        if (command.any { it in '\u0900'..'\u097F' }) {
            return true
        }

        val c = command.lowercase()

        // Common Hindi / Hinglish markers
        val hindiMarkers = listOf(
            "ka",
            "ki",
            "ke",
            "kya",
            "kitna",
            "kitne",
            "kitni",
            "hai",
            "hain",
            "mein",
            "me",
            "se",
            "ko",
            "par",
            "batao",
            "bata",
            "chahiye",
            "karo",
            "karna",
            "kare",
            "nikalo",
            "nikal",
            "kitna hai",
            "kitne hai",
            "kitni hai"
        )

        var markerCount = 0

        for (marker in hindiMarkers) {

            if (
                Regex(
                    """\b${Regex.escape(marker)}\b"""
                ).containsMatchIn(c)
            ) {
                markerCount++
            }
        }

        // Two or more Hindi markers = Hinglish
        if (markerCount >= 2) {
            return true
        }

        // Special numeric Hinglish patterns
        if (
            Regex(
                """\b\d+(?:\.\d+)?\s+ka\s+\d+(?:\.\d+)?\s*(?:percent|percentage|%)\b"""
            ).containsMatchIn(c)
        ) {
            return true
        }

        // Hindi-style mathematical questions
        if (
            Regex(
                """\b(?:hcf|gcd|lcm|average|ratio)\s+(?:of\s+)?\d+\s+(?:aur|ka|ki|ke)\s+\d+\b"""
            ).containsMatchIn(c)
        ) {
            return true
        }

        return false
    }

    // -------------------------------------------------------------
    // PRIME CHECK
    // -------------------------------------------------------------

    private fun isPrime(number: Long): Boolean {

        if (number < 2) {
            return false
        }

        if (number == 2L) {
            return true
        }

        if (number % 2L == 0L) {
            return false
        }

        var i = 3L

        while (i * i <= number) {

            if (number % i == 0L) {
                return false
            }

            i += 2
        }

        return true
    }

    // -------------------------------------------------------------
    // FACTORIAL
    // -------------------------------------------------------------

    private fun factorial(number: Int): Long {

        var result = 1L

        for (i in 2..number) {
            result *= i.toLong()
        }

        return result
    }

    // -------------------------------------------------------------
    // GCD / HCF
    // -------------------------------------------------------------

    private fun gcd(a: Long, b: Long): Long {

        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)

        while (y != 0L) {

            val temp = x % y
            x = y
            y = temp
        }

        return x
    }

    // -------------------------------------------------------------
    // LCM
    // -------------------------------------------------------------

    private fun lcm(a: Long, b: Long): Long {

        if (a == 0L || b == 0L) {
            return 0L
        }

        return kotlin.math.abs((a / gcd(a, b)) * b)
    }

    // -------------------------------------------------------------
    // NUMBER FORMAT
    // -------------------------------------------------------------

    private fun formatNumber(value: Double): String {

        if (value.isNaN() || value.isInfinite()) {
            return value.toString()
        }

        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.4f", value)
                .trimEnd('0')
                .trimEnd('.')
        }
    }
}
