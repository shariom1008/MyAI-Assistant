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

        // ---------------------------------------------------------
        // PERCENTAGE OF
        // Examples:
        // 25 percent of 800
        // what is 25 percent of 800
        // 25 percentage of 800
        // 25% of 800
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:percent|percentage|%)\s*(?:of|ka)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val percentage = it.groupValues[1].toDouble()
            val base = it.groupValues[2].toDouble()

            val result = percentage * base / 100.0

            return "$percentage percent of $base is ${formatNumber(result)}."
        }

        // Reverse Hindi style:
        // 800 ka 25 percent
        Regex(
            """(\d+(?:\.\d+)?)\s+ka\s+(\d+(?:\.\d+)?)\s*(?:percent|percentage|%)"""
        ).find(c)?.let {

            val base = it.groupValues[1].toDouble()
            val percentage = it.groupValues[2].toDouble()

            val result = percentage * base / 100.0

            return "$percentage percent of $base is ${formatNumber(result)}."
        }

        // ---------------------------------------------------------
        // HCF / GCD
        // Supports:
        // HCF of 24 and 36
        // what is HCF of 24 and 36
        // H C F of 24 and 36
        // GCD of 24 and 36
        // 24 and 36 HCF
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:h\s*c\s*f|g\s*c\s*d)(?:\s+of)?\s+(\d+)\s+(?:and|aur)\s+(\d+)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toLong()
            val b = it.groupValues[2].toLong()

            return "HCF of $a and $b is ${gcd(a, b)}."
        }

        Regex(
            """(\d+)\s+(?:and|aur)\s+(\d+)\s*(?:ka|ki)?\s*(?:h\s*c\s*f|g\s*c\s*d)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toLong()
            val b = it.groupValues[2].toLong()

            return "HCF of $a and $b is ${gcd(a, b)}."
        }

        // ---------------------------------------------------------
        // EVEN / ODD
        // ---------------------------------------------------------

        Regex(
            """(?:is\s+)?(\d+)\s+(?:even|odd)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toLong()

            return if (number % 2 == 0L) {
                "$number is even."
            } else {
                "$number is odd."
            }
        }

        Regex(
            """(?:is\s+)?(\d+)\s+(?:an\s+)?(?:even|odd)\s+number"""
        ).find(c)?.let {

            val number = it.groupValues[1].toLong()

            return if (number % 2 == 0L) {
                "$number is even."
            } else {
                "$number is odd."
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
                "$number is a prime number."
            } else {
                "$number is not a prime number."
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
                return "I can calculate factorial only up to 20."
            }

            return "$number factorial is ${factorial(number)}."
        }

        // ---------------------------------------------------------
        // LCM
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:lcm)(?:\s+of)?\s+(\d+)\s+(?:and|aur)\s+(\d+)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toLong()
            val b = it.groupValues[2].toLong()

            return "LCM of $a and $b is ${lcm(a, b)}."
        }

        // ---------------------------------------------------------
        // AVERAGE
        // ---------------------------------------------------------

        Regex(
            """average\s+(?:of\s+)?(\d+(?:\.\d+)?)\s+(?:and|aur)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            val result = (a + b) / 2.0

            return "The average is ${formatNumber(result)}."
        }

        // ---------------------------------------------------------
        // RATIO
        // ---------------------------------------------------------

        Regex(
            """ratio\s+(?:of\s+)?(\d+(?:\.\d+)?)\s+(?:and|to|aur)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            if (b == 0.0) {
                return "Ratio cannot have zero as the second value."
            }

            val g = gcd(a.toLong(), b.toLong())

            return "The ratio is ${formatNumber(a / g)}:${formatNumber(b / g)}."
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
                return "Percentage increase cannot be calculated from zero."
            }

            val result = ((newValue - oldValue) / oldValue) * 100.0

            return "The percentage increase is ${formatNumber(result)} percent."
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
                return "Percentage decrease cannot be calculated from zero."
            }

            val result = ((oldValue - newValue) / oldValue) * 100.0

            return "The percentage decrease is ${formatNumber(result)} percent."
        }

        // ---------------------------------------------------------
        // SQUARE ROOT
        // ---------------------------------------------------------

        Regex(
            """(?:square\s+root\s+of|sqrt)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toDouble()

            if (number < 0) {
                return "Square root of a negative number is not a real number."
            }

            return "The square root is ${formatNumber(sqrt(number))}."
        }

        // ---------------------------------------------------------
        // SQUARE
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:square\s+of)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toDouble()

            return "The square of $number is ${formatNumber(number.pow(2))}."
        }

        // ---------------------------------------------------------
        // CUBE
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(?:cube\s+of)\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val number = it.groupValues[1].toDouble()

            return "The cube of $number is ${formatNumber(number.pow(3))}."
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

            return "$base to the power of $exponent is ${formatNumber(result)}."
        }

        // ---------------------------------------------------------
        // BASIC ADDITION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:\+|plus)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            return "${formatNumber(a)} plus ${formatNumber(b)} is ${formatNumber(a + b)}."
        }

        // ---------------------------------------------------------
        // BASIC SUBTRACTION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:-|minus)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            return "${formatNumber(a)} minus ${formatNumber(b)} is ${formatNumber(a - b)}."
        }

        // ---------------------------------------------------------
        // BASIC MULTIPLICATION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:\*|x|×|times|multiply(?:\s+by)?)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            return "${formatNumber(a)} multiplied by ${formatNumber(b)} is ${formatNumber(a * b)}."
        }

        // ---------------------------------------------------------
        // BASIC DIVISION
        // ---------------------------------------------------------

        Regex(
            """(?:what is\s+)?(\d+(?:\.\d+)?)\s*(?:/|÷|divided\s+by)\s*(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val a = it.groupValues[1].toDouble()
            val b = it.groupValues[2].toDouble()

            if (b == 0.0) {
                return "Division by zero is not possible."
            }

            return "${formatNumber(a)} divided by ${formatNumber(b)} is ${formatNumber(a / b)}."
        }

        // ---------------------------------------------------------
        // CIRCLE AREA
        // ---------------------------------------------------------

        Regex(
            """(?:area\s+of\s+)?(?:a\s+)?circle\s+(?:with\s+)?radius\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val radius = it.groupValues[1].toDouble()

            val area = PI * radius * radius

            return "The area of the circle is ${formatNumber(area)} square units."
        }

        // ---------------------------------------------------------
        // CIRCLE CIRCUMFERENCE
        // ---------------------------------------------------------

        Regex(
            """(?:circumference\s+of\s+)?(?:a\s+)?circle\s+(?:with\s+)?radius\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val radius = it.groupValues[1].toDouble()

            val circumference = 2 * PI * radius

            return "The circumference of the circle is ${formatNumber(circumference)} units."
        }

        // ---------------------------------------------------------
        // RECTANGLE AREA
        // ---------------------------------------------------------

        Regex(
            """(?:area\s+of\s+)?rectangle\s+(?:with\s+)?length\s+(\d+(?:\.\d+)?)\s+(?:and|aur)\s+width\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val length = it.groupValues[1].toDouble()
            val width = it.groupValues[2].toDouble()

            val area = length * width

            return "The area of the rectangle is ${formatNumber(area)} square units."
        }

        // ---------------------------------------------------------
        // RECTANGLE PERIMETER
        // ---------------------------------------------------------

        Regex(
            """(?:perimeter\s+of\s+)?rectangle\s+(?:with\s+)?length\s+(\d+(?:\.\d+)?)\s+(?:and|aur)\s+width\s+(\d+(?:\.\d+)?)"""
        ).find(c)?.let {

            val length = it.groupValues[1].toDouble()
            val width = it.groupValues[2].toDouble()

            val perimeter = 2 * (length + width)

            return "The perimeter of the rectangle is ${formatNumber(perimeter)} units."
        }

        return null
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
