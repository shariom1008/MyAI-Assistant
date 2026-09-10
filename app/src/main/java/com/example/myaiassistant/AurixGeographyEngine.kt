package com.example.myaiassistant

import java.util.Locale

object AurixGeographyEngine {

    // =========================================================
    // COUNTRIES AND CAPITALS
    // =========================================================

    private val capitals = mapOf(
        "india" to "New Delhi",
        "usa" to "Washington, D.C.",
        "united states" to "Washington, D.C.",
        "america" to "Washington, D.C.",
        "canada" to "Ottawa",
        "uk" to "London",
        "united kingdom" to "London",
        "england" to "London",
        "france" to "Paris",
        "germany" to "Berlin",
        "italy" to "Rome",
        "spain" to "Madrid",
        "portugal" to "Lisbon",
        "japan" to "Tokyo",
        "china" to "Beijing",
        "russia" to "Moscow",
        "ukraine" to "Kyiv",
        "nepal" to "Kathmandu",
        "bhutan" to "Thimphu",
        "bangladesh" to "Dhaka",
        "pakistan" to "Islamabad",
        "afghanistan" to "Kabul",
        "sri lanka" to "Sri Jayawardenepura Kotte",
        "myanmar" to "Naypyidaw",
        "thailand" to "Bangkok",
        "vietnam" to "Hanoi",
        "malaysia" to "Kuala Lumpur",
        "singapore" to "Singapore",
        "indonesia" to "Jakarta",
        "philippines" to "Manila",
        "south korea" to "Seoul",
        "north korea" to "Pyongyang",
        "australia" to "Canberra",
        "new zealand" to "Wellington",
        "brazil" to "Brasília",
        "argentina" to "Buenos Aires",
        "mexico" to "Mexico City",
        "egypt" to "Cairo",
        "south africa" to "Pretoria",
        "nigeria" to "Abuja",
        "kenya" to "Nairobi",
        "ethiopia" to "Addis Ababa",
        "saudi arabia" to "Riyadh",
        "uae" to "Abu Dhabi",
        "united arab emirates" to "Abu Dhabi",
        "qatar" to "Doha",
        "oman" to "Muscat",
        "iran" to "Tehran",
        "iraq" to "Baghdad",
        "turkey" to "Ankara",
        "greece" to "Athens",
        "switzerland" to "Bern",
        "austria" to "Vienna",
        "belgium" to "Brussels",
        "netherlands" to "Amsterdam",
        "norway" to "Oslo",
        "sweden" to "Stockholm",
        "denmark" to "Copenhagen",
        "finland" to "Helsinki",
        "poland" to "Warsaw",
        "czech republic" to "Prague",
        "czechia" to "Prague"
    )

    // =========================================================
    // INDIA STATES
    // =========================================================

    private val indianStates = mapOf(
        "andhra pradesh" to "Amaravati",
        "arunachal pradesh" to "Itanagar",
        "assam" to "Dispur",
        "bihar" to "Patna",
        "chhattisgarh" to "Raipur",
        "goa" to "Panaji",
        "gujarat" to "Gandhinagar",
        "haryana" to "Chandigarh",
        "himachal pradesh" to "Shimla",
        "jharkhand" to "Ranchi",
        "karnataka" to "Bengaluru",
        "kerala" to "Thiruvananthapuram",
        "madhya pradesh" to "Bhopal",
        "maharashtra" to "Mumbai",
        "manipur" to "Imphal",
        "meghalaya" to "Shillong",
        "mizoram" to "Aizawl",
        "nagaland" to "Kohima",
        "odisha" to "Bhubaneswar",
        "punjab" to "Chandigarh",
        "rajasthan" to "Jaipur",
        "sikkim" to "Gangtok",
        "tamil nadu" to "Chennai",
        "telangana" to "Hyderabad",
        "tripura" to "Agartala",
        "uttar pradesh" to "Lucknow",
        "uttarakhand" to "Dehradun",
        "west bengal" to "Kolkata"
    )

    // =========================================================
    // INDIA UNION TERRITORIES
    // =========================================================

    private val indianUTs = mapOf(
        "andaman and nicobar islands" to "Port Blair",
        "chandigarh" to "Chandigarh",
        "dadra and nagar haveli and daman and diu" to "Daman",
        "delhi" to "New Delhi",
        "jammu and kashmir" to "Srinagar (summer), Jammu (winter)",
        "ladakh" to "Leh",
        "lakshadweep" to "Kavaratti",
        "puducherry" to "Puducherry"
    )

    // =========================================================
    // CONTINENTS
    // =========================================================

    private val continentFacts = mapOf(
        "asia" to "Asia is the largest continent by area and population.",
        "africa" to "Africa is the second-largest continent by area.",
        "north america" to "North America is the third-largest continent by area.",
        "south america" to "South America is the fourth-largest continent by area.",
        "antarctica" to "Antarctica is the coldest continent and is covered largely by ice.",
        "europe" to "Europe is one of the world's seven continents.",
        "australia" to "Australia is the smallest continent by area."
    )

    // =========================================================
    // OCEANS
    // =========================================================

    private val oceanFacts = mapOf(
        "pacific ocean" to "The Pacific Ocean is the largest and deepest ocean.",
        "atlantic ocean" to "The Atlantic Ocean is the second-largest ocean.",
        "indian ocean" to "The Indian Ocean is the third-largest ocean.",
        "southern ocean" to "The Southern Ocean surrounds Antarctica.",
        "arctic ocean" to "The Arctic Ocean is the smallest and shallowest major ocean."
    )

    // =========================================================
    // MOUNTAINS
    // =========================================================

    private val mountainFacts = mapOf(
        "mount everest" to "Mount Everest is the highest mountain above sea level, at about 8,849 metres.",
        "everest" to "Mount Everest is the highest mountain above sea level.",
        "k2" to "K2 is the second-highest mountain above sea level, at about 8,611 metres.",
        "kanchenjunga" to "Kangchenjunga is the third-highest mountain above sea level, at about 8,586 metres."
    )

    // =========================================================
    // RIVERS
    // =========================================================

    private val riverFacts = mapOf(
        "ganga" to "The Ganga, or Ganges, is one of the major rivers of India.",
        "ganges" to "The Ganges, also called the Ganga, is one of the major rivers of India.",
        "yamuna" to "The Yamuna is a major tributary of the Ganga.",
        "brahmaputra" to "The Brahmaputra is a major transboundary river of Asia.",
        "indus" to "The Indus is a major river of South Asia."
    )

    // =========================================================
    // GENERAL GEOGRAPHY FACTS
    // =========================================================

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        // -----------------------------------------------------
        // INDIA CAPITAL
        // -----------------------------------------------------

        if (
            c.contains("capital of india") ||
            c.contains("india ki capital") ||
            c.contains("india ka capital") ||
            c.contains("bharat ki rajdhani") ||
            c.contains("bharat ki rajdhani kya") ||
            c == "india capital"
        ) {
            return "India ki capital New Delhi hai, Boss."
        }

        // -----------------------------------------------------
        // NUMBER OF INDIAN STATES
        // -----------------------------------------------------

        if (
            c.contains("how many states in india") ||
            c.contains("india mein kitne states") ||
            c.contains("india me kitne states") ||
            c.contains("bharat mein kitne rajya") ||
            c.contains("bharat me kitne rajya")
        ) {
            return "India mein 28 states hain, Boss."
        }

        // -----------------------------------------------------
        // NUMBER OF UNION TERRITORIES
        // -----------------------------------------------------

        if (
            c.contains("how many union territories") ||
            c.contains("india mein kitne union territories") ||
            c.contains("india me kitne union territories") ||
            c.contains("kitne kendrashasit pradesh")
        ) {
            return "India mein 8 Union Territories hain, Boss."
        }

        // -----------------------------------------------------
        // INDIA STATES CAPITAL
        // -----------------------------------------------------

        for ((state, capital) in indianStates) {

            if (
                containsWholeWord(c, state) &&
                (
                    c.contains("capital") ||
                    c.contains("rajdhani") ||
                    c.contains("capital kya") ||
                    c.contains("capital bata") ||
                    c.contains("capital batao")
                )
            ) {
                return "$state ki capital $capital hai, Boss."
            }
        }

        // -----------------------------------------------------
        // INDIA UNION TERRITORY CAPITAL
        // -----------------------------------------------------

        for ((ut, capital) in indianUTs) {

            if (
                containsWholeWord(c, ut) &&
                (
                    c.contains("capital") ||
                    c.contains("rajdhani")
                )
            ) {
                return "$ut ki capital $capital hai, Boss."
            }
        }

        // -----------------------------------------------------
        // COUNTRY CAPITAL
        // -----------------------------------------------------

        for ((country, capital) in capitals) {

            if (
                containsWholeWord(c, country) &&
                (
                    c.contains("capital") ||
                    c.contains("rajdhani") ||
                    c.contains("capital kya") ||
                    c.contains("capital bata") ||
                    c.contains("capital batao")
                )
            ) {
                return "$country ki capital $capital hai, Boss."
            }
        }

        // -----------------------------------------------------
        // CONTINENTS
        // -----------------------------------------------------

        if (
            c.contains("how many continents") ||
            c.contains("kitne continents") ||
            c.contains("kitne mahadweep") ||
            c.contains("continents kitne")
        ) {
            return "Duniya mein 7 continents hain, Boss."
        }

        for ((name, fact) in continentFacts) {

            if (containsWholeWord(c, name)) {

                if (
                    c.contains("continent") ||
                    c.contains("about") ||
                    c.contains("baare") ||
                    c.contains("bare") ||
                    c.contains("largest") ||
                    c.contains("smallest")
                ) {
                    return fact
                }
            }
        }

        // -----------------------------------------------------
        // OCEANS
        // -----------------------------------------------------

        if (
            c.contains("how many oceans") ||
            c.contains("kitne oceans") ||
            c.contains("kitne samundar")
        ) {
            return "Duniya mein 5 major oceans hain, Boss."
        }

        for ((name, fact) in oceanFacts) {

            if (containsWholeWord(c, name)) {
                return fact
            }
        }

        // -----------------------------------------------------
        // LARGEST OCEAN
        // -----------------------------------------------------

        if (
            c.contains("largest ocean") ||
            c.contains("biggest ocean") ||
            c.contains("sabse bada ocean") ||
            c.contains("sabse bada samundar")
        ) {
            return "Pacific Ocean duniya ka sabse bada ocean hai, Boss."
        }

        // -----------------------------------------------------
        // HIGHEST MOUNTAIN
        // -----------------------------------------------------

        if (
            c.contains("highest mountain") ||
            c.contains("highest peak") ||
            c.contains("highest mountain in world") ||
            c.contains("duniya ka sabse uncha pahad") ||
            c.contains("sabse unchi mountain")
        ) {
            return "Mount Everest duniya ka highest mountain hai, Boss."
        }

        // -----------------------------------------------------
        // MOUNTAINS
        // -----------------------------------------------------

        for ((name, fact) in mountainFacts) {

            if (containsWholeWord(c, name)) {
                return fact
            }
        }

        // -----------------------------------------------------
        // RIVERS
        // -----------------------------------------------------

        for ((name, fact) in riverFacts) {

            if (containsWholeWord(c, name)) {
                return fact
            }
        }

        // -----------------------------------------------------
        // EQUATOR
        // -----------------------------------------------------

        if (
            c.contains("what is equator") ||
            c.contains("equator kya hai") ||
            c.contains("equator kya hota hai") ||
            c.contains("equator ke baare") ||
            c.contains("equator ke bare")
        ) {
            return "Equator Earth ko Northern Hemisphere aur Southern Hemisphere mein divide karne wali imaginary line hai, Boss."
        }

        // -----------------------------------------------------
        // PRIME MERIDIAN
        // -----------------------------------------------------

        if (
            c.contains("prime meridian") ||
            c.contains("prime meridian kya hai")
        ) {
            return "Prime Meridian ek imaginary line hai jo 0 degree longitude se guzarti hai, Boss."
        }

        // -----------------------------------------------------
        // TROPIC OF CANCER
        // -----------------------------------------------------

        if (
            c.contains("tropic of cancer") ||
            c.contains("kark rekha") ||
            c.contains("kark rekha kya hai")
        ) {
            return "Tropic of Cancer Earth par lagbhag 23.5 degree north latitude par sthit imaginary line hai, Boss."
        }

        // -----------------------------------------------------
        // TROPIC OF CAPRICORN
        // -----------------------------------------------------

        if (
            c.contains("tropic of capricorn") ||
            c.contains("makar rekha") ||
            c.contains("makar rekha kya hai")
        ) {
            return "Tropic of Capricorn Earth par lagbhag 23.5 degree south latitude par sthit imaginary line hai, Boss."
        }

        // -----------------------------------------------------
        // LATITUDE
        // -----------------------------------------------------

        if (
            c.contains("what is latitude") ||
            c.contains("latitude kya hai") ||
            c.contains("latitude kya hota hai")
        ) {
            return "Latitude Earth par kisi location ki north ya south position batane wali imaginary lines hain, Boss."
        }

        // -----------------------------------------------------
        // LONGITUDE
        // -----------------------------------------------------

        if (
            c.contains("what is longitude") ||
            c.contains("longitude kya hai") ||
            c.contains("longitude kya hota hai")
        ) {
            return "Longitude Earth par kisi location ki east ya west position batane wali imaginary lines hain, Boss."
        }

        // -----------------------------------------------------
        // EARTH HEMISPHERES
        // -----------------------------------------------------

        if (
            c.contains("how many hemispheres") ||
            c.contains("kitne hemispheres")
        ) {
            return "Earth ko generally Northern, Southern, Eastern aur Western hemispheres mein describe kiya jata hai, Boss."
        }

        // -----------------------------------------------------
        // GEOGRAPHY IDENTITY
        // -----------------------------------------------------

        if (
            c == "geography" ||
            c == "geography kya hai" ||
            c.contains("what is geography")
        ) {
            return "Geography Earth, uski surface, places, people, environment aur unke relationships ka study hai, Boss."
        }

        return null
    }

    // =========================================================
    // WHOLE WORD MATCH
    // =========================================================

    private fun containsWholeWord(
        text: String,
        word: String
    ): Boolean {

        if (word.isBlank()) {
            return false
        }

        return Regex(
            """(?<![a-z])${Regex.escape(word)}(?![a-z])"""
        ).containsMatchIn(text)
    }
}
