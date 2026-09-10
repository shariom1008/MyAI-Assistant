package com.example.myaiassistant

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AurixTimeEngine {

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) return null

        val now = Calendar.getInstance()

        val timeFormat = SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        )

        val dateFormat = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        )

        val dayFormat = SimpleDateFormat(
            "EEEE",
            Locale.getDefault()
        )

        val monthFormat = SimpleDateFormat(
            "MMMM",
            Locale.getDefault()
        )

        val yearFormat = SimpleDateFormat(
            "yyyy",
            Locale.getDefault()
        )

        // =========================================================
        // CURRENT TIME
        // =========================================================

        if (
            c == "time" ||
            c == "current time" ||
            c.contains("what time is it") ||
            c.contains("what is the time") ||
            c.contains("time kya hai") ||
            c.contains("abhi time kya hai") ||
            c.contains("kitne baje hain") ||
            c.contains("kitna time hua") ||
            c.contains("current time batao")
        ) {
            return "Abhi time ${timeFormat.format(now.time)} hai, Boss."
        }

        // =========================================================
        // CURRENT DATE
        // =========================================================

        if (
            c == "date" ||
            c == "today date" ||
            c == "current date" ||
            c.contains("today ki date") ||
            c.contains("aaj ki date") ||
            c.contains("aaj date kya hai") ||
            c.contains("date kya hai") ||
            c.contains("aaj ki tareekh") ||
            c.contains("aaj ki tarikh")
        ) {
            return "Aaj ki date ${dateFormat.format(now.time)} hai, Boss."
        }

        // =========================================================
        // TODAY / DAY
        // =========================================================

        if (
            c == "today" ||
            c == "aaj" ||
            c.contains("what day is today") ||
            c.contains("today ka day") ||
            c.contains("aaj ka din") ||
            c.contains("aaj kaun sa din hai") ||
            c.contains("aaj kya day hai")
        ) {
            return "Aaj ${dayFormat.format(now.time)} hai, Boss."
        }

        // =========================================================
        // CURRENT MONTH
        // =========================================================

        if (
            c == "month" ||
            c == "current month" ||
            c.contains("which month") ||
            c.contains("what month") ||
            c.contains("kaunsa month") ||
            c.contains("current month kya hai") ||
            c.contains("abhi ka month")
        ) {
            return "Abhi ${monthFormat.format(now.time)} month hai, Boss."
        }

        // =========================================================
        // CURRENT YEAR
        // =========================================================

        if (
            c == "year" ||
            c == "current year" ||
            c.contains("which year") ||
            c.contains("what year") ||
            c.contains("kaunsa year") ||
            c.contains("current year kya hai") ||
            c.contains("abhi ka year")
        ) {
            return "Abhi year ${yearFormat.format(now.time)} hai, Boss."
        }

        // =========================================================
        // FULL DATE + DAY + TIME
        // =========================================================

        if (
            c.contains("today details") ||
            c.contains("aaj ki complete date") ||
            c.contains("today information") ||
            c.contains("date day time")
        ) {
            return "Aaj ${dayFormat.format(now.time)}, ${dateFormat.format(now.time)} hai aur time ${timeFormat.format(now.time)} hai, Boss."
        }

        // =========================================================
        // TOMORROW
        // =========================================================

        if (
            c == "tomorrow" ||
            c == "kal" ||
            c.contains("tomorrow ka day") ||
            c.contains("kal ka din") ||
            c.contains("kal kaun sa din hai") ||
            c.contains("tomorrow ki date") ||
            c.contains("kal ki date")
        ) {

            val tomorrow = now.clone() as Calendar
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)

            return if (
                c.contains("date") ||
                c.contains("tareekh") ||
                c.contains("tarikh")
            ) {
                "Kal ki date ${dateFormat.format(tomorrow.time)} hai, Boss."
            } else {
                "Kal ${dayFormat.format(tomorrow.time)} hai, Boss."
            }
        }

        // =========================================================
        // YESTERDAY
        // =========================================================

        if (
            c == "yesterday" ||
            c == "kal tha" ||
            c.contains("yesterday ka day") ||
            c.contains("kal ka din tha") ||
            c.contains("kal kaun sa din tha") ||
            c.contains("yesterday ki date")
        ) {

            val yesterday = now.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)

            return if (
                c.contains("date") ||
                c.contains("tareekh") ||
                c.contains("tarikh")
            ) {
                "Kal ki date ${dateFormat.format(yesterday.time)} thi, Boss."
            } else {
                "Kal ${dayFormat.format(yesterday.time)} tha, Boss."
            }
        }

        // =========================================================
        // DAY AFTER TOMORROW
        // =========================================================

        if (
            c.contains("day after tomorrow") ||
            c.contains("parso") ||
            c.contains("parson")
        ) {

            val dayAfterTomorrow = now.clone() as Calendar
            dayAfterTomorrow.add(Calendar.DAY_OF_YEAR, 2)

            return "Parso ${dayFormat.format(dayAfterTomorrow.time)} hoga, Boss."
        }

        // =========================================================
        // DAY BEFORE YESTERDAY
        // =========================================================

        if (
            c.contains("day before yesterday") ||
            c.contains("narso") ||
            c.contains("narsau")
        ) {

            val dayBeforeYesterday = now.clone() as Calendar
            dayBeforeYesterday.add(Calendar.DAY_OF_YEAR, -2)

            return "Parso se ek din pehle ${dayFormat.format(dayBeforeYesterday.time)} tha, Boss."
        }

        // =========================================================
        // WEEK
        // =========================================================

        if (
            c == "week" ||
            c == "current week" ||
            c.contains("week kaun sa hai") ||
            c.contains("is week")
        ) {

            val week = now.get(Calendar.WEEK_OF_YEAR)

            return "Ye year ka week number $week hai, Boss."
        }

        // =========================================================
        // DAYS IN CURRENT MONTH
        // =========================================================

        if (
            c.contains("days in this month") ||
            c.contains("current month mein kitne din") ||
            c.contains("is month mein kitne din") ||
            c.contains("iss month mein kitne din")
        ) {

            val days = now.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )

            return "Is month mein $days din hain, Boss."
        }

        // =========================================================
        // DAYS REMAINING IN CURRENT YEAR
        // =========================================================

        if (
            c.contains("days remaining this year") ||
            c.contains("year mein kitne din bache") ||
            c.contains("saal mein kitne din bache") ||
            c.contains("kitne din bache hain year mein")
        ) {

            val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
            val totalDays = now.getActualMaximum(
                Calendar.DAY_OF_YEAR
            )

            val remaining = totalDays - dayOfYear

            return "Is saal mein $remaining din remaining hain, Boss."
        }

        // =========================================================
        // DAYS PASSED IN CURRENT YEAR
        // =========================================================

        if (
            c.contains("days passed this year") ||
            c.contains("year mein kitne din beet gaye") ||
            c.contains("saal mein kitne din beet gaye")
        ) {

            val passed = now.get(Calendar.DAY_OF_YEAR)

            return "Is saal ke $passed din complete ho chuke hain, Boss."
        }

        // =========================================================
        // LEAP YEAR
        // =========================================================

        if (
            c.contains("leap year") ||
            c.contains("is this year leap") ||
            c.contains("kya ye leap year hai")
        ) {

            val year = now.get(Calendar.YEAR)

            val isLeap =
                year % 400 == 0 ||
                (year % 4 == 0 && year % 100 != 0)

            return if (isLeap) {
                "$year leap year hai, Boss."
            } else {
                "$year leap year nahi hai, Boss."
            }
        }

        // =========================================================
        // FULL CURRENT INFORMATION
        // =========================================================

        if (
            c.contains("date and time") ||
            c.contains("date time") ||
            c.contains("date aur time") ||
            c.contains("aaj date aur time")
        ) {

            return "Aaj ${dayFormat.format(now.time)}, ${dateFormat.format(now.time)} hai aur current time ${timeFormat.format(now.time)} hai, Boss."
        }

        // =========================================================
        // NOTHING FOUND
        // =========================================================

        return null
    }
}
