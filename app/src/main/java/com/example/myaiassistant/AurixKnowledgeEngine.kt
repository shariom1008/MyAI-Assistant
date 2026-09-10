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
        // GEOGRAPHY
        // =====================================================
        
        AurixGeographyEngine.answer(c)?.let {
             return it
        }

        // =====================================================
        // PERIODIC TABLE — 118 ELEMENTS
        // =====================================================

        AurixPeriodicTable.answer(c)?.let {
            return it
        }

        // =====================================================
        // PHARMACEUTICAL INDUSTRY
        // =====================================================

        AurixPharmaEngine.answer(c)?.let {
            return it
        }

                // =====================================================
        // SCIENCE
        // =====================================================

        AurixScienceEngine.answer(c)?.let {
            return it
        }

        // =====================================================
        // ADVANCED MATH
        // =====================================================

        AurixMathEngine.answer(c)?.let {
            return it
        }

        // =====================================================
        // CALCULATOR
        // =====================================================

        AurixCalculatorEngine.answer(c)?.let {
            return it
        }

        // =====================================================
        // UNIT CONVERSION
        // =====================================================

        AurixUnitEngine.answer(c)?.let {
            return it
        }

        // =====================================================
        // DICTIONARY
        // =====================================================

        AurixDictionaryEngine.answer(c)?.let {
            return it
        }

        // =====================================================
        // TIME / DATE
        // =====================================================

        AurixTimeEngine.answer(c)?.let {
            return it
        }

        // =====================================================
        // GENERAL KNOWLEDGE — CAPITALS
        // =====================================================

        val capitals = mapOf(
            "france" to "Paris",
            "usa" to "Washington, D.C.",
            "america" to "Washington, D.C.",
            "uk" to "London",
            "england" to "London",
            "japan" to "Tokyo",
            "china" to "Beijing",
            "russia" to "Moscow",
            "germany" to "Berlin",
            "italy" to "Rome",
            "spain" to "Madrid",
            "canada" to "Ottawa",
            "australia" to "Canberra",
            "brazil" to "Brasília",
            "nepal" to "Kathmandu",
            "bhutan" to "Thimphu",
            "bangladesh" to "Dhaka",
            "pakistan" to "Islamabad",
            "afghanistan" to "Kabul",
            "sri lanka" to "Sri Jayawardenepura Kotte",
            "south korea" to "Seoul",
            "north korea" to "Pyongyang",
            "indonesia" to "Jakarta",
            "thailand" to "Bangkok",
            "singapore" to "Singapore",
            "malaysia" to "Kuala Lumpur",
            "uae" to "Abu Dhabi",
            "saudi arabia" to "Riyadh",
            "egypt" to "Cairo",
            "south africa" to "Pretoria"
        )

        for ((country, capital) in capitals) {

            if (
                c == "capital of $country" ||
                c == "$country ki capital" ||
                c == "$country ki rajdhani"
            ) {
                return "$country ki capital $capital hai, Boss."
            }
        }

        // =====================================================
        // INDIA
        // =====================================================

        if (
            c.contains("capital of india") ||
            c.contains("india ki capital") ||
            c.contains("india ki rajdhani") ||
            c.contains("bharat ki rajdhani")
        ) {
            return "India ki capital New Delhi hai, Boss."
        }

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

        if (
            c.contains("how many states in india") ||
            c.contains("india mein kitne states") ||
            c.contains("india mein kitne rajya")
        ) {
            return "India mein 28 states hain, Boss."
        }

        if (
            c.contains("how many union territories") ||
            c.contains("india mein kitne union territories")
        ) {
            return "India mein 8 Union Territories hain, Boss."
        }

        if (
            c.contains("currency of india") ||
            c.contains("india ki currency") ||
            c.contains("bharat ki currency")
        ) {
            return "India ki currency Indian Rupee hai, Boss."
        }

        if (
            c.contains("national language of india") ||
            c.contains("india ki national language")
        ) {
            return "India ki Constitution mein koi national language declare nahi ki gayi hai, Boss."
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
            c.contains("sun ke sabse paas planet") ||
            c.contains("suraj ke sabse paas planet")
        ) {
            return "Mercury Suraj ke sabse paas wala planet hai, Boss."
        }

        if (
            c.contains("farthest planet from sun") ||
            c.contains("sun se sabse door planet") ||
            c.contains("suraj se sabse door planet")
        ) {
            return "Neptune Suraj se sabse door wala planet hai, Boss."
        }

        if (
            c.contains("how many planets") ||
            c.contains("kitne planets") ||
            c.contains("kitne grah")
        ) {
            return "Solar system mein 8 planets hain, Boss."
        }

        if (
            c.contains("how many moons does earth have") ||
            c.contains("earth ke kitne moons") ||
            c.contains("prithvi ke kitne chandrama")
        ) {
            return "Earth ka ek natural satellite hai, jise Moon kaha jata hai, Boss."
        }

        if (
            c.contains("earth satellite") ||
            c.contains("earth ka satellite") ||
            c.contains("prithvi ka satellite")
        ) {
            return "Moon Earth ka natural satellite hai, Boss."
        }

        if (
            c.contains("largest star") ||
            c.contains("sun largest star")
        ) {
            return "Sun hamare solar system ka star hai, lekin Universe ka sabse bada star nahi hai, Boss."
        }

        // =====================================================
        // SOLAR SYSTEM / EARTH
        // =====================================================

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

        if (
            c.contains("how old is earth") ||
            c.contains("earth ki age") ||
            c.contains("prithvi ki age")
        ) {
            return "Earth ki age approximately 4.54 billion years hai, Boss."
        }

        if (
            c.contains("earth shape") ||
            c.contains("shape of earth") ||
            c.contains("prithvi ka shape")
        ) {
            return "Earth approximately spherical shape ki hai, Boss."
        }

        if (
            c.contains("largest ocean") ||
            c.contains("sabse bada ocean")
        ) {
            return "Pacific Ocean duniya ka sabse bada ocean hai, Boss."
        }

        if (
            c.contains("largest continent") ||
            c.contains("sabse bada continent")
        ) {
            return "Asia duniya ka sabse bada continent hai, Boss."
        }

        // =====================================================
        // SCIENCE
        // =====================================================

        if (
            c.contains("chemical formula of water") ||
            c.contains("water ka formula") ||
            c.contains("water ka chemical formula") ||
            c.contains("water chemical formula") ||
            c.contains("paani ka formula") ||
            c.contains("paani ka chemical formula") ||
            c.contains("pani ka formula") ||
            c.contains("pani ka chemical formula") ||
            c.contains("formula of water") ||
            c == "h2o"
        ) {
            return "Water ka chemical formula H2O hai, Boss."
        }

        if (
            c.contains("chemical formula of oxygen") ||
            c.contains("oxygen ka formula")
        ) {
            return "Oxygen ka molecular formula O2 hai, Boss."
        }

        if (
            c.contains("chemical formula of carbon dioxide") ||
            c.contains("carbon dioxide ka formula")
        ) {
            return "Carbon dioxide ka chemical formula CO2 hai, Boss."
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

        if (
            c.contains("force formula") ||
            c.contains("force ka formula")
        ) {
            return "Force ka formula F equals m into a hai, Boss."
        }

        if (
            c.contains("gravity on earth") ||
            c.contains("earth gravity") ||
            c.contains("prithvi ki gravity")
        ) {
            return "Earth par gravitational acceleration approximately 9.8 meter per second squared hai, Boss."
        }

        if (
            c.contains("what is photosynthesis") ||
            c.contains("photosynthesis kya hai")
        ) {
            return "Photosynthesis woh process hai jisme plants sunlight ki help se food banate hain, Boss."
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

        if (
            c.contains("how many bones") ||
            c.contains("human body mein kitni bones") ||
            c.contains("insaan ke sharir mein kitni haddiyan")
        ) {
            return "Adult human body mein normally 206 bones hoti hain, Boss."
        }

        if (
            c.contains("how many teeth") ||
            c.contains("adult human teeth") ||
            c.contains("insaan ke kitne daant")
        ) {
            return "Adult human ke normally 32 permanent teeth hote hain, Boss."
        }

        if (
            c.contains("largest muscle") ||
            c.contains("human body largest muscle")
        ) {
            return "Gluteus maximus human body ki largest muscle mani jati hai, Boss."
        }

        if (
            c.contains("brain controls") ||
            c.contains("brain kya control karta hai")
        ) {
            return "Brain body ke movement, senses, thoughts, memory aur bahut si automatic functions ko control karta hai, Boss."
        }

        // =====================================================
        // UNIT CONVERSIONS
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
            c.contains("1 kilometer mein kitne meter") ||
            c.contains("one kilometer mein kitne meter") ||
            c.contains("1 km mein kitne meter")
        ) {
            return "1 kilometer mein 1000 meters hote hain, Boss."
        }

        if (
            c.contains("1 kilogram mein kitne gram") ||
            c.contains("one kilogram mein kitne gram")
        ) {
            return "1 kilogram mein 1000 grams hote hain, Boss."
        }

        if (
            c.contains("1 gram mein kitne milligram") ||
            c.contains("one gram mein kitne milligram")
        ) {
            return "1 gram mein 1000 milligrams hote hain, Boss."
        }

        if (
            c.contains("1 liter mein kitne milliliter") ||
            c.contains("one liter mein kitne milliliter")
        ) {
            return "1 liter mein 1000 milliliters hote hain, Boss."
        }

        if (
            c.contains("1 hour mein kitne minutes") ||
            c.contains("one hour mein kitne minutes")
        ) {
            return "1 hour mein 60 minutes hote hain, Boss."
        }

        if (
            c.contains("1 minute mein kitne seconds") ||
            c.contains("one minute mein kitne seconds")
        ) {
            return "1 minute mein 60 seconds hote hain, Boss."
        }

        if (
            c.contains("1 day mein kitne hours") ||
            c.contains("one day mein kitne hours")
        ) {
            return "1 day mein 24 hours hote hain, Boss."
        }

        if (
            c.contains("1 week mein kitne days") ||
            c.contains("one week mein kitne days")
        ) {
            return "1 week mein 7 days hote hain, Boss."
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
                if (result.isFinite() && result % 1.0 == 0.0) {
                    result.toLong().toString()
                } else {
                    "%.4f".format(Locale.US, result)
                        .trimEnd('0')
                        .trimEnd('.')
                }

            return "Answer $formatted hai, Boss."
        }

        // =====================================================
        // DIRECTIONS
        // =====================================================

        if (
            c.contains("sun rises") ||
            c.contains("sun kis direction") ||
            c.contains("suraj kis direction") ||
            c.contains("sun direction")
        ) {
            return "Sun East, yani Purab direction se rise hota hai, Boss."
        }

        if (
            c.contains("sun sets") ||
            c.contains("suraj kis direction mein doobta")
        ) {
            return "Sun West, yani Paschim direction mein set hota hai, Boss."
        }

        // =====================================================
        // BASIC FACTS
        // =====================================================

        if (
            c.contains("how many days in a year") ||
            c.contains("year mein kitne days")
        ) {
            return "Normally ek year mein 365 days hote hain, aur leap year mein 366 days, Boss."
        }

        if (
            c.contains("how many hours in a day") ||
            c.contains("day mein kitne hours")
        ) {
            return "Ek day mein 24 hours hote hain, Boss."
        }

        if (
            c.contains("how many minutes in an hour") ||
            c.contains("hour mein kitne minutes")
        ) {
            return "Ek hour mein 60 minutes hote hain, Boss."
        }

        // =====================================================
        // UNKNOWN
        // =====================================================

        return null
    }
}
