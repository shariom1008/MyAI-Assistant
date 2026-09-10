package com.example.myaiassistant

import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

object AurixCalculatorEngine {

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) {
            return null
        }

        // =========================================================
        // PERCENTAGE
        // =========================================================

        var match = Regex(
            """(?:(\d+(?:\.\d+)?)\s*(?:ka|of)\s*)?(\d+(?:\.\d+)?)\s*(?:%|percent|percentage)"""
        ).find(c)

        if (match != null && c.contains("%")) {

            val percent = match.groupValues[1].toDoubleOrNull()
            val number = match.groupValues[2].toDoubleOrNull()

            if (percent != null && number != null) {
                val result = number * percent / 100.0
                return "Result ${formatNumber(result)} hai, Boss."
            }
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:ka|of)\s*(\d+(?:\.\d+)?)\s*(?:percent|percentage)"""
        ).find(c)

        if (match != null) {

            val number = match.groupValues[1].toDoubleOrNull()
            val percent = match.groupValues[2].toDoubleOrNull()

            if (number != null && percent != null) {
                val result = number * percent / 100.0
                return "Result ${formatNumber(result)} hai, Boss."
            }
        }

        // =========================================================
        // SQUARE ROOT
        // =========================================================

        match = Regex(
            """(?:square root of|square root|sqrt of|sqrt|ka square root)\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val number = match.groupValues[1].toDoubleOrNull()

            if (number != null && number >= 0) {
                val result = sqrt(number)
                return "Square root ${formatNumber(result)} hai, Boss."
            }
        }

        // =========================================================
        // SQUARE
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:ka|ki)?\s*square\b"""
        ).find(c)

        if (match != null) {

            val number = match.groupValues[1].toDoubleOrNull()

            if (number != null) {
                val result = number.pow(2)
                return "Square ${formatNumber(result)} hai, Boss."
            }
        }

        // =========================================================
        // CUBE
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:ka|ki)?\s*cube\b"""
        ).find(c)

        if (match != null) {

            val number = match.groupValues[1].toDoubleOrNull()

            if (number != null) {
                val result = number.pow(3)
                return "Cube ${formatNumber(result)} hai, Boss."
            }
        }

        // =========================================================
        // POWER
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:power|raised to|ki power)\s*(\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val base = match.groupValues[1].toDoubleOrNull()
            val exponent = match.groupValues[2].toDoubleOrNull()

            if (base != null && exponent != null) {
                val result = base.pow(exponent)
                return "Result ${formatNumber(result)} hai, Boss."
            }
        }

        // =========================================================
        // ADDITION
        // =========================================================

        match = Regex(
            """(-?\d+(?:\.\d+)?)\s*(?:\+|plus|add)\s*(-?\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()

            if (a != null && b != null) {
                return "Result ${formatNumber(a + b)} hai, Boss."
            }
        }

        // =========================================================
        // SUBTRACTION
        // =========================================================

        match = Regex(
            """(-?\d+(?:\.\d+)?)\s*(?:-|minus|subtract)\s*(-?\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()

            if (a != null && b != null) {
                return "Result ${formatNumber(a - b)} hai, Boss."
            }
        }

        // =========================================================
        // MULTIPLICATION
        // =========================================================

        match = Regex(
            """(-?\d+(?:\.\d+)?)\s*(?:\*|x|×|multiply|multiplied by|into)\s*(-?\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()

            if (a != null && b != null) {
                return "Result ${formatNumber(a * b)} hai, Boss."
            }
        }

        // =========================================================
        // DIVISION
        // =========================================================

        match = Regex(
            """(-?\d+(?:\.\d+)?)\s*(?:/|÷|divide|divided by|divided)\s*(-?\d+(?:\.\d+)?)"""
        ).find(c)

        if (match != null) {

            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()

            if (a != null && b != null) {

                if (b == 0.0) {
                    return "Zero se divide nahi kar sakte, Boss."
                }

                return "Result ${formatNumber(a / b)} hai, Boss."
            }
        }

        // =========================================================
        // KILOMETER TO METER
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:km|kilometer|kilometers)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:meter|metre|meters|metres)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} kilometer mein ${formatNumber(value * 1000)} meter hote hain, Boss."
            }
        }

        // =========================================================
        // METER TO CENTIMETER
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:meter|metre|meters|metres)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:cm|centimeter|centimeters|centimetre|centimetres)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} meter mein ${formatNumber(value * 100)} centimeter hote hain, Boss."
            }
        }

        // =========================================================
        // CENTIMETER TO MILLIMETER
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:cm|centimeter|centimeters|centimetre|centimetres)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:mm|millimeter|millimeters|millimetre|millimetres)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} centimeter mein ${formatNumber(value * 10)} millimeter hote hain, Boss."
            }
        }

        // =========================================================
        // KILOGRAM TO GRAM
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:kg|kilogram|kilograms)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:g|gram|grams)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} kilogram mein ${formatNumber(value * 1000)} gram hote hain, Boss."
            }
        }

        // =========================================================
        // GRAM TO MILLIGRAM
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:g|gram|grams)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:mg|milligram|milligrams)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} gram mein ${formatNumber(value * 1000)} milligram hote hain, Boss."
            }
        }

        // =========================================================
        // LITER TO MILLILITER
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:l|liter|liters|litre|litres)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:ml|milliliter|milliliters|millilitre|millilitres)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} liter mein ${formatNumber(value * 1000)} milliliter hote hain, Boss."
            }
        }

        // =========================================================
        // HOUR TO MINUTES
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:hour|hours|hr|hrs)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:minute|minutes|min)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} hour mein ${formatNumber(value * 60)} minutes hote hain, Boss."
            }
        }

        // =========================================================
        // MINUTES TO SECONDS
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:minute|minutes|min)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:second|seconds|sec|secs)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} minute mein ${formatNumber(value * 60)} seconds hote hain, Boss."
            }
        }

        // =========================================================
        // DAYS TO HOURS
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:day|days)\s*(?:mein|me|to|into)?\s*(?:kitne|kitna)?\s*(?:hour|hours|hr|hrs)"""
        ).find(c)

        if (match != null) {

            val value = match.groupValues[1].toDoubleOrNull()

            if (value != null) {
                return "${formatNumber(value)} day mein ${formatNumber(value * 24)} hours hote hain, Boss."
            }
        }

        // =========================================================
        // DIRECT NUMERIC EXPRESSION
        // =========================================================

        if (
            c.matches(
                Regex(
                    """-?\d+(?:\.\d+)?\s*[\+\-\*/×÷]\s*-?\d+(?:\.\d+)?"""
                )
            )
        ) {

            val expression =
                c.replace("×", "*")
                    .replace("÷", "/")

            val result = calculateSimpleExpression(expression)

            if (result != null) {
                return "Result ${formatNumber(result)} hai, Boss."
            }
        }

        return null
    }

    // =========================================================
    // SIMPLE EXPRESSION CALCULATOR
    // =========================================================

    private fun calculateSimpleExpression(
        expression: String
    ): Double? {

        val match = Regex(
            """(-?\d+(?:\.\d+)?)\s*([\+\-\*/])\s*(-?\d+(?:\.\d+)?)"""
        ).matchEntire(expression.trim())
            ?: return null

        val a = match.groupValues[1].toDoubleOrNull()
            ?: return null

        val operator = match.groupValues[2]

        val b = match.groupValues[3].toDoubleOrNull()
            ?: return null

        return when (operator) {

            "+" -> a + b

            "-" -> a - b

            "*" -> a * b

            "/" -> {
                if (b == 0.0) {
                    null
                } else {
                    a / b
                }
            }

            else -> null
        }
    }

    // =========================================================
    // NUMBER FORMATTER
    // =========================================================

    private fun formatNumber(
        value: Double
    ): String {

        if (value.isNaN() || value.isInfinite()) {
            return value.toString()
        }

        if (value == value.toLong().toDouble()) {
            return value.toLong().toString()
        }

        return String.format(
            Locale.US,
            "%.6f",
            value
        ).trimEnd('0')
            .trimEnd('.')
    }
}
