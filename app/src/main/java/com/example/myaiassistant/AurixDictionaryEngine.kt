package com.example.myaiassistant

import java.util.Locale

object AurixUnitEngine {

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) return null

        // =========================================================
        // LENGTH
        // =========================================================

        var match = Regex(
            """(\d+(?:\.\d+)?)\s*(km|kilometer|kilometers)\s*(?:to|mein|me)?\s*(m|meter|meters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} kilometer = ${format(value * 1000)} meter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(m|meter|meters)\s*(?:to|mein|me)?\s*(cm|centimeter|centimeters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} meter = ${format(value * 100)} centimeter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(cm|centimeter|centimeters)\s*(?:to|mein|me)?\s*(mm|millimeter|millimeters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} centimeter = ${format(value * 10)} millimeter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(m|meter|meters)\s*(?:to|mein|me)?\s*(ft|feet|foot)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} meter = ${format(value * 3.28084)} feet, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(ft|feet|foot)\s*(?:to|mein|me)?\s*(m|meter|meters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} feet = ${format(value * 0.3048)} meter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(inch|inches|in)\s*(?:to|mein|me)?\s*(cm|centimeter|centimeters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} inch = ${format(value * 2.54)} centimeter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(mile|miles)\s*(?:to|mein|me)?\s*(km|kilometer|kilometers)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} mile = ${format(value * 1.609344)} kilometer, Boss."
        }

        // =========================================================
        // WEIGHT
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(kg|kilogram|kilograms)\s*(?:to|mein|me)?\s*(g|gram|grams)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} kilogram = ${format(value * 1000)} gram, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(g|gram|grams)\s*(?:to|mein|me)?\s*(mg|milligram|milligrams)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} gram = ${format(value * 1000)} milligram, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(ton|tons|tonne|tonnes)\s*(?:to|mein|me)?\s*(kg|kilogram|kilograms)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} ton = ${format(value * 1000)} kilogram, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(lb|lbs|pound|pounds)\s*(?:to|mein|me)?\s*(kg|kilogram|kilograms)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} pound = ${format(value * 0.45359237)} kilogram, Boss."
        }

        // =========================================================
        // VOLUME
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(l|liter|liters|litre|litres)\s*(?:to|mein|me)?\s*(ml|milliliter|milliliters|millilitre|millilitres)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} liter = ${format(value * 1000)} milliliter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(ml|milliliter|milliliters)\s*(?:to|mein|me)?\s*(l|liter|liters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} milliliter = ${format(value / 1000)} liter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(gallon|gallons)\s*(?:to|mein|me)?\s*(l|liter|liters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} gallon = ${format(value * 3.785411784)} liter, Boss."
        }

        // =========================================================
        // TEMPERATURE
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:degree\s*)?(?:c|celsius)\s*(?:to|mein|me)?\s*(?:f|fahrenheit)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            val result = (value * 9.0 / 5.0) + 32.0
            return "${format(value)} degree Celsius = ${format(result)} degree Fahrenheit, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:degree\s*)?(?:f|fahrenheit)\s*(?:to|mein|me)?\s*(?:c|celsius)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            val result = (value - 32.0) * 5.0 / 9.0
            return "${format(value)} degree Fahrenheit = ${format(result)} degree Celsius, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:degree\s*)?(?:c|celsius)\s*(?:to|mein|me)?\s*(?:k|kelvin)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} degree Celsius = ${format(value + 273.15)} Kelvin, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:k|kelvin)\s*(?:to|mein|me)?\s*(?:c|celsius)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} Kelvin = ${format(value - 273.15)} degree Celsius, Boss."
        }

        // =========================================================
        // SPEED
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:km/h|kmph|kph)\s*(?:to|mein|me)?\s*(?:m/s|mps)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} km/h = ${format(value / 3.6)} m/s, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:m/s|mps)\s*(?:to|mein|me)?\s*(?:km/h|kmph|kph)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} m/s = ${format(value * 3.6)} km/h, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:mph|mile per hour|miles per hour)\s*(?:to|mein|me)?\s*(?:km/h|kmph|kph)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} mph = ${format(value * 1.609344)} km/h, Boss."
        }

        // =========================================================
        // AREA
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:sq\s*ft|square feet|square foot)\s*(?:to|mein|me)?\s*(?:sq\s*m|square meter|square meters)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} square feet = ${format(value * 0.09290304)} square meter, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:sq\s*m|square meter|square meters)\s*(?:to|mein|me)?\s*(?:sq\s*ft|square feet|square foot)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} square meter = ${format(value * 10.7639104167)} square feet, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:acre|acres)\s*(?:to|mein|me)?\s*(?:sq\s*ft|square feet|square foot)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} acre = ${format(value * 43560.0)} square feet, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:hectare|hectares|ha)\s*(?:to|mein|me)?\s*(?:acre|acres)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} hectare = ${format(value * 2.47105381)} acres, Boss."
        }

        // =========================================================
        // PRESSURE
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:bar|bars)\s*(?:to|mein|me)?\s*(?:psi)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} bar = ${format(value * 14.5037738)} PSI, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:psi)\s*(?:to|mein|me)?\s*(?:bar|bars)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} PSI = ${format(value * 0.0689475729)} bar, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:bar|bars)\s*(?:to|mein|me)?\s*(?:pa|pascal|pascals)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} bar = ${format(value * 100000.0)} Pascal, Boss."
        }

        // =========================================================
        // TIME
        // =========================================================

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:hour|hours|hr|hrs)\s*(?:to|mein|me)?\s*(?:minute|minutes|min)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} hour = ${format(value * 60)} minutes, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:minute|minutes|min)\s*(?:to|mein|me)?\s*(?:second|seconds|sec|secs)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} minute = ${format(value * 60)} seconds, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:day|days)\s*(?:to|mein|me)?\s*(?:hour|hours|hr|hrs)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} day = ${format(value * 24)} hours, Boss."
        }

        match = Regex(
            """(\d+(?:\.\d+)?)\s*(?:week|weeks)\s*(?:to|mein|me)?\s*(?:day|days)"""
        ).find(c)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            return "${format(value)} week = ${format(value * 7)} days, Boss."
        }

        return null
    }

    private fun format(value: Double): String {

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
