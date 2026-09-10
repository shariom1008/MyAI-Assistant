package com.example.myaiassistant

import java.util.Locale
import kotlin.math.*

object AurixKnowledgeEngine {

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) {
            return null
        }

        // =====================================================
        // GENERAL KNOWLEDGE
        // =====================================================

        if (
            c.contains("capital of france") ||
            c.contains("france ki capital") ||
            c.contains("france ki rajdhani")
        ) {
            return "France ki capital Paris hai, Boss."
        }

        if (
            c.contains("capital of usa") ||
            c.contains("capital of america") ||
            c.contains("america ki capital") ||
            c.contains("usa ki capital")
        ) {
            return "USA ki capital Washington, D.C. hai, Boss."
        }

        if (
            c.contains("capital of uk") ||
            c.contains("uk ki capital") ||
            c.contains("england ki capital")
        ) {
            return "United Kingdom ki capital London hai, Boss."
        }

        if (
            c.contains("capital of japan") ||
            c.contains("japan ki capital")
        ) {
            return "Japan ki capital Tokyo hai, Boss."
        }

        if (
            c.contains("capital of china") ||
            c.contains("china ki capital")
        ) {
            return "China ki capital Beijing hai, Boss."
        }

        if (
            c.contains("capital of russia") ||
            c.contains("russia ki capital")
        ) {
            return "Russia ki capital Moscow hai, Boss."
        }

        // =====================================================
        // PLANETS
        // =====================================================

        if (
            c.contains("largest planet") ||
            c.contains("sabse bada planet") ||
            c.contains("sabse bada grah")
        ) {
            return "Jupiter solar system ka sabse bada planet hai, Boss."
        }

        if (
            c.contains("smallest planet") ||
            c.contains("sabse chhota planet") ||
            c.contains("sabse chhota grah")
        ) {
            return "Mercury solar system ka sabse chhota planet hai, Boss."
        }

        if (
            c.contains("red planet") ||
            c.contains("lal grah")
        ) {
            return "Mars ko Red Planet, yani Lal Grah kaha jata hai, Boss."
        }

        if (
            c.contains("closest planet to sun") ||
            c.contains("sun ke sabse paas planet")
        ) {
            return "Mercury Suraj ke sabse paas wala planet hai, Boss."
        }

        // =====================================================
        // SOLAR SYSTEM
        // =====================================================

        if (
            c.contains("how many planets") ||
            c.contains("kitne planets") ||
            c.contains("kitne grah")
        ) {
            return "Solar system mein 8 planets hain, Boss."
        }

        if (
            c.contains("how many continents") ||
            c.contains("kitne continents") ||
            c.contains("kitne mahadeep")
        ) {
            return "Earth par 7 continents hain, Boss."
        }

        if (
            c.contains("how many oceans") ||
            c.contains("kitne oceans")
        ) {
            return "Earth par 5 major oceans hain, Boss."
        }

        // =====================================================
        // SCIENCE
        // =====================================================

        if (
            c.contains("chemical formula of water") ||
            c.contains("water ka formula") ||
            c.contains("paani ka formula")
        ) {
            return "Water ka chemical formula H2O hai, Boss."
        }

        if (
            c.contains("speed of light") ||
            c.contains("light ki speed")
        ) {
            return "Vacuum mein light ki speed approximately 3 lakh kilometer per second hai, Boss."
        }

        if (
            c.contains("boiling point of water") ||
            c.contains("water boiling point") ||
            c.contains("paani kitne degree par ubalta hai")
        ) {
            return "Normal atmospheric pressure par water 100 degree Celsius par boil hota hai, Boss."
        }

        if (
            c.contains("freezing point of water") ||
            c.contains("water freezing point") ||
            c.contains("paani kitne degree par jamta hai")
        ) {
            return "Normal atmospheric pressure par water 0 degree Celsius par freeze hota hai, Boss."
        }

        // =====================================================
        // EARTH
        // =====================================================

        if (
            c.contains("how old is earth") ||
            c.contains("earth ki age") ||
            c.contains("prithvi ki age")
        ) {
            return "Earth ki age approximately 4.54 billion years hai, Boss."
        }

        if (
            c.contains("earth satellite") ||
            c.contains("earth ka satellite") ||
            c.contains("prithvi ka satellite")
        ) {
            return "Moon Earth ka natural satellite hai, Boss."
        }

        // =====================================================
        // HUMAN BODY
        // =====================================================

        if (
            c.contains("largest organ") ||
            c.contains("human body ka largest organ") ||
            c.contains("insaan ke sharir ka sabse bada ang")
        ) {
            return "Human body ka largest organ skin hai, Boss."
        }

        if (
            c.contains("human heart") &&
            (
                c.contains("chambers") ||
                c.contains("kamre")
            )
        ) {
            return "Human heart mein 4 chambers hote hain, Boss."
        }

        // =====================================================
        // INDIA
        // =====================================================

        if (
            c.contains("national animal of india") ||
            c.contains("india ka national animal") ||
            c.contains("bharat ka rashtriya pashu")
        ) {
            return "India ka national animal Bengal Tiger hai, Boss."
        }

        if (
            c.contains("national bird of india") ||
            c.contains("india ka national bird") ||
            c.contains("bharat ka rashtriya pakshi")
        ) {
            return "India ka national bird Indian Peacock hai, Boss."
        }

        if (
            c.contains("national flower of india") ||
            c.contains("india ka national flower") ||
            c.contains("bharat ka rashtriya phool")
        ) {
            return "India ka national flower Lotus hai, Boss."
        }

        if (
            c.contains("national fruit of india") ||
            c.contains("india ka national fruit")
        ) {
            return "India ka national fruit Mango hai, Boss."
        }

        // =====================================================
        // SIMPLE UNIT CONVERSIONS
        // =====================================================

        if (
            c.contains("1 meter mein kitne centimeter") ||
            c.contains("one meter mein kitne centimeter")
        ) {
            return "1 meter mein 100 centimeters hote hain, Boss."
        }

        if (
            c.contains("1 centimeter mein kitne millimeter") ||
            c.contains("one centimeter mein kitne millimeter")
        ) {
            return "1 centimeter mein 10 millimeters hote hain, Boss."
        }

        if (
            c.contains("1 kilogram mein kitne gram") ||
            c.contains("one kilogram mein kitne gram")
        ) {
            return "1 kilogram mein 1000 grams hote hain, Boss."
        }

        if (
            c.contains("1 liter mein kitne milliliter") ||
            c.contains("one liter mein kitne milliliter")
        ) {
            return "1 liter mein 1000 milliliters hote hain, Boss."
        }

        // =====================================================
        // SIMPLE MATH
        // =====================================================

        val simpleMath =
            Regex(
                """^\s*(\d+(?:\.\d+)?)\s*(plus|\+|minus|-|times|x|\*|divided by|/)\s*(\d+(?:\.\d+)?)\s*$"""
            ).find(c)

        if (simpleMath != null) {

            val a =
                simpleMath.groupValues[1].toDouble()

            val operation =
                simpleMath.groupValues[2]

            val b =
                simpleMath.groupValues[3].toDouble()

            val result =
                when (operation) {

                    "plus",
                    "+" ->
                        a + b

                    "minus",
                    "-" ->
                        a - b

                    "times",
                    "x",
                    "*" ->
                        a * b

                    "divided by",
                    "/" -> {
                        if (b == 0.0) {
                            return "Boss, zero se divide nahi kar sakte."
                        }
                        a / b
                    }

                    else ->
                        return null
                }

            val formatted =
                if (result % 1.0 == 0.0) {
                    result.toLong().toString()
                } else {
                    result.toString()
                }

            return "Answer $formatted hai, Boss."
        }

        // =====================================================
        // UNKNOWN
        // =====================================================

        return null
    }
}
