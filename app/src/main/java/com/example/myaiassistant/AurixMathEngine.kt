package com.example.myaiassistant

import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object AurixMathEngine {

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) {
            return null
        }

        // =========================================================
        // EVEN / ODD
        // =========================================================

        var match = Regex(
            """(?:is|check|tell me|batao)?\s*(?:number\s*)?(\d+)\s*(?:even|odd)\b"""
        ).find(c)

        if (match != null) {
            val number = match.groupValues[1].toLongOrNull()

            if (number != null) {
                return if (number % 2L == 0L) {
                    "$number even number hai, Boss."
                } else {
                    "$number odd number hai, Boss."
                }
            }
        }

        match = Regex(
            """(\d+)\s*(?:even hai|odd hai|even number|odd number)"""
        ).find(c)

        if (match != null) {
            val number = match.groupValues[1].toLongOrNull()

            if (number != null) {
                return if (number % 2L == 0L) {
                    "$number even number hai, Boss."
                } else {
                    "$number odd number hai, Boss."
                }
            }
        }

        // =========================================================
        // PRIME NUMBER
        // =========================================================

        match = Regex(
            """(?:is|check|tell me|batao)?\s*(?:number\s*)?(\d+)\s*(?:prime|prime number)"""
        ).find(c)

        if (match != null) {
            val number = match.groupValues[1].toLongOrNull()

            if (number != null) {
                return if (isPrime(number)) {
                    "$number prime number hai, Boss."
                } else {
                    "$number prime number nahi hai, Boss."
                }
            }
        }

        match = Regex(
            """(?:prime|prime number)\s*(?:hai kya|check karo|check|batao)?\s*(\d+)"""
        ).find(c)

        if (match != null) {
            val number = match.groupValues[1].toLongOrNull()

            if (number != null) {
                return if (isPrime(number)) {
                    "$number prime number hai, Boss."
                } else {
                    "$number prime number nahi hai, Boss."
                }
            }
        }

        // =========================================================
        // FACTORIAL
        // =========================================================

        match = Regex(
            """(\d+)\s*(?:ka|ki)?\s*factorial\b"""
        ).find(c)

        if (match != null) {

            val number = match.groupValues[1].toIntOrNull()

            if (number != null) {

                if (number < 0) {
                    return "Negative number ka factorial defined nahi hai, Boss."
                }

                if (number > 20) {
                    return "Boss, factorial calculation ke liye number 20 ya usse kam rakho."
                }

                val result = factorial(number)

                return "$number ka factorial ${result} hai, Boss."
            }
        }

        // =========================================================
        // HCF / GCD
        // =========================================================

        match = Regex(
            """(?:hcf|gcd)(?:\s+of)?\s+(\d+)\s+(?:and|aur)\s+(\d+)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toLongOrNull()
            val b = match.groupValues[2].toLongOrNull()

            if (a != null && b != null) {

                val result = gcd(a, b)

                return "${a} aur ${b} ka HCF ${result} hai, Boss."
            }
        }

        match = Regex(
            """(\d+)\s+(?:aur|and)\s+(\d+)\s*(?:ka|ki)?\s*(?:hcf|gcd)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toLongOrNull()
            val b = match.groupValues[2].toLongOrNull()

            if (a != null && b != null) {

                val result = gcd(a, b)

                return "${a} aur ${b} ka HCF ${result} hai, Boss."
            }
        }

        // =========================================================
        // LCM
        // =========================================================

        match = Regex(
            """(?:lcm)(?:\s+of)?\s+(\d+)\s+(?:and|aur)\s+(\d+)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toLongOrNull()
            val b = match.groupValues[2].toLongOrNull()

            if (a != null && b != null) {

                val result = lcm(a, b)

                return "${a} aur ${b} ka LCM ${result} hai, Boss."
            }
        }

        match = Regex(
            """(\d+)\s+(?:aur|and)\s+(\d+)\s*(?:ka|ki)?\s*lcm"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toLongOrNull()
            val b = match.groupValues[2].toLongOrNull()

            if (a != null && b != null) {

                val result = lcm(a, b)

                return "${a} aur ${b} ka LCM ${result} hai, Boss."
            }
        }

        // =========================================================
        // AVERAGE
        // =========================================================

        match = Regex(
            """average\s+(?:of)?\s*(.+)"""
        ).find(c)

        if (match != null) {

            val values = extractNumbers(
                match.groupValues[1]
            )

            if (values.isNotEmpty()) {

                val result =
                    values.sum() / values.size

                return "Average ${formatNumber(result)} hai, Boss."
            }
        }

        match = Regex(
            """(?:average|avg)\s+(\d+(?:\.\d+)?)\s+(?:and|aur)\s+(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()

            if (a != null && b != null) {

                val result = (a + b) / 2.0

                return "Average ${formatNumber(result)} hai, Boss."
            }
        }

        // =========================================================
        // RATIO
        // =========================================================

        match = Regex(
            """ratio\s+(?:of)?\s*(\d+(?:\.\d+)?)\s*(?:to|and|aur)\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()

            if (a != null && b != null && b != 0.0) {

                val divisor = gcd(
                    a.toLong(),
                    b.toLong()
                )

                val first =
                    if (divisor != 0L) {
                        a / divisor
                    } else {
                        a
                    }

                val second =
                    if (divisor != 0L) {
                        b / divisor
                    } else {
                        b
                    }

                return "Ratio ${formatNumber(first)} to ${formatNumber(second)} hai, Boss."
            }
        }

        // =========================================================
        // PERCENTAGE INCREASE
        // =========================================================

        match = Regex(
            """(?:percentage|percent)\s*(?:increase|increase hua|increase hai)\s*(?:from|of)?\s*(\d+(?:\.\d+)?)\s*(?:to|se)\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val oldValue =
                match.groupValues[1].toDoubleOrNull()

            val newValue =
                match.groupValues[2].toDoubleOrNull()

            if (
                oldValue != null &&
                newValue != null &&
                oldValue != 0.0
            ) {

                val result =
                    ((newValue - oldValue) / oldValue) * 100.0

                return "Percentage change ${formatNumber(result)}% hai, Boss."
            }
        }

        // =========================================================
        // PERCENTAGE DECREASE
        // =========================================================

        match = Regex(
            """(?:percentage|percent)\s*(?:decrease|decrease hua|decrease hai)\s*(?:from|of)?\s*(\d+(?:\.\d+)?)\s*(?:to|se)\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val oldValue =
                match.groupValues[1].toDoubleOrNull()

            val newValue =
                match.groupValues[2].toDoubleOrNull()

            if (
                oldValue != null &&
                newValue != null &&
                oldValue != 0.0
            ) {

                val result =
                    ((oldValue - newValue) / oldValue) * 100.0

                return "Percentage decrease ${formatNumber(result)}% hai, Boss."
            }
        }

        // =========================================================
        // SQUARE ROOT
        // =========================================================

        match = Regex(
            """(?:square root|sqrt)\s*(?:of)?\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val number =
                match.groupValues[1].toDoubleOrNull()

            if (number != null && number >= 0.0) {

                return "Square root ${formatNumber(sqrt(number))} hai, Boss."
            }
        }

        // =========================================================
        // SQUARE
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:ka|ki)?\s*square\b"""
        ).find(c)

        if (match != null) {

            val number =
                match.groupValues[1].toDoubleOrNull()

            if (number != null) {

                return "Square ${formatNumber(number.pow(2))} hai, Boss."
            }
        }

        // =========================================================
        // CUBE
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:ka|ki)?\s*cube\b"""
        ).find(c)

        if (match != null) {

            val number =
                match.groupValues[1].toDoubleOrNull()

            if (number != null) {

                return "Cube ${formatNumber(number.pow(3))} hai, Boss."
            }
        }

        // =========================================================
        // POWER
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:power|ki power|raised to)\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val base =
                match.groupValues[1].toDoubleOrNull()

            val exponent =
                match.groupValues[2].toDoubleOrNull()

            if (base != null && exponent != null) {

                return "Result ${formatNumber(base.pow(exponent))} hai, Boss."
            }
        }

        // =========================================================
        // CIRCLE AREA
        // =========================================================

        match = Regex(
            """(?:circle|circle ka)\s*(?:area)\s*(?:radius|r)?\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val radius =
                match.groupValues[1].toDoubleOrNull()

            if (radius != null) {

                val area =
                    Math.PI * radius * radius

                return "Circle ka area ${formatNumber(area)} square units hai, Boss."
            }
        }

        // =========================================================
        // CIRCLE CIRCUMFERENCE
        // =========================================================

        match = Regex(
            """(?:circle|circle ka)\s*(?:circumference|perimeter)\s*(?:radius|r)?\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val radius =
                match.groupValues[1].toDoubleOrNull()

            if (radius != null) {

                val circumference =
                    2.0 * Math.PI * radius

                return "Circle ka circumference ${formatNumber(circumference)} units hai, Boss."
            }
        }

        // =========================================================
        // RECTANGLE AREA
        // =========================================================

        match = Regex(
            """(?:rectangle|rectangle ka)\s*(?:area)\s*(?:length)?\s*(\d+(?:\.\d+)?)\s*(?:and|aur|by|x|×)\s*(?:width)?\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val length =
                match.groupValues[1].toDoubleOrNull()

            val width =
                match.groupValues[2].toDoubleOrNull()

            if (length != null && width != null) {

                val area =
                    length * width

                return "Rectangle ka area ${formatNumber(area)} square units hai, Boss."
            }
        }

        // =========================================================
        // RECTANGLE PERIMETER
        // =========================================================

        match = Regex(
            """(?:rectangle|rectangle ka)\s*(?:perimeter)\s*(?:length)?\s*(\d+(?:\.\d+)?)\s*(?:and|aur|by|x|×)\s*(?:width)?\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val length =
                match.groupValues[1].toDoubleOrNull()

            val width =
                match.groupValues[2].toDoubleOrNull()

            if (length != null && width != null) {

                val perimeter =
                    2.0 * (length + width)

                return "Rectangle ka perimeter ${formatNumber(perimeter)} units hai, Boss."
            }
        }

        return null
    }

    // =========================================================
    // PRIME CHECK
    // =========================================================

    private fun isPrime(
        number: Long
    ): Boolean {

        if (number < 2L) {
            return false
        }

        if (number == 2L) {
            return true
        }

        if (number % 2L == 0L) {
            return false
        }

        var divisor = 3L

        while (
            divisor <= number / divisor
        ) {

            if (number % divisor == 0L) {
                return false
            }

            divisor += 2L
        }

        return true
    }

    // =========================================================
    // FACTORIAL
    // =========================================================

    private fun factorial(
        number: Int
    ): Long {

        var result = 1L

        for (i in 2..number) {
            result *= i.toLong()
        }

        return result
    }

    // =========================================================
    // GCD / HCF
    // =========================================================

    private fun gcd(
        first: Long,
        second: Long
    ): Long {

        var a = abs(first)
        var b = abs(second)

        while (b != 0L) {

            val remainder =
                a % b

            a = b
            b = remainder
        }

        return a
    }

    // =========================================================
    // LCM
    // =========================================================

    private fun lcm(
        first: Long,
        second: Long
    ): Long {

        if (
            first == 0L ||
            second == 0L
        ) {
            return 0L
        }

        return abs(
            (first / gcd(first, second)) * second
        )
    }

    // =========================================================
    // EXTRACT NUMBERS
    // =========================================================

    private fun extractNumbers(
        text: String
    ): List<Double> {

        return Regex(
            """-?\d+(?:\.\d+)?"""
        )
            .findAll(text)
            .mapNotNull {
                it.value.toDoubleOrNull()
            }
            .toList()
    }

    // =========================================================
    // NUMBER FORMATTER
    // =========================================================

    private fun formatNumber(
        value: Double
    ): String {

        if (
            value.isNaN() ||
            value.isInfinite()
        ) {
            return value.toString()
        }

        if (
            value == value.toLong().toDouble()
        ) {
            return value.toLong().toString()
        }

        return String.format(
            Locale.US,
            "%.6f",
            value
        )
            .trimEnd('0')
            .trimEnd('.')
    }
}
