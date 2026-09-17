package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixEverydayKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Everyday", "Electricity Safety", listOf("electricity safety sockets overload", "electricity safety"), """Electrical safety includes avoiding overloaded outlets, damaged cords and exposed conductors and using protective devices appropriate to the installation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Food Storage", listOf("food storage refrigeration leftovers", "food storage"), """Safe food storage depends on appropriate temperature control, clean handling, suitable containers and observing recommended storage times.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Laundry Symbols", listOf("laundry symbols washing care labels", "laundry symbols"), """Laundry care symbols communicate recommended washing, bleaching, drying, ironing and professional-care methods.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Basic Measurement", listOf("measurement units length mass volume temperature", "basic measurement"), """Common everyday measurements use standardized units such as metres, kilograms, litres and degrees Celsius, with conversions between systems when needed.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Weather vs Climate", listOf("weather climate difference", "weather vs climate"), """Weather describes short-term atmospheric conditions, while climate describes statistical patterns over longer periods.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Time Zones", listOf("time zone utc local time", "time zones"), """Time zones divide the world into regions using offsets from reference standards such as UTC, with local rules sometimes changing seasonally.""", ConfidenceLevel.HIGH, false)
    )
}
