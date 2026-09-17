package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixIndiaKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("India", "Indian Constitution", listOf("constitution india fundamental rights directive principles", "indian constitution"), """The Constitution of India establishes the country's governmental framework, institutions, rights and principles of governance.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Parliament of India", listOf("parliament lok sabha rajya sabha", "parliament of india"), """India's Parliament consists of the President and two houses: the Lok Sabha and the Rajya Sabha.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Indian Federal Structure", listOf("india union states centre state relations", "indian federal structure"), """India combines Union and state governments with constitutionally allocated legislative, executive and financial responsibilities.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Indian Agriculture", listOf("india agriculture crops irrigation", "indian agriculture"), """Indian agriculture includes diverse rain-fed and irrigated systems producing cereals, pulses, oilseeds, fibers, fruits and other crops.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Indian Space Programme", listOf("isro india space programme satellites launch vehicles", "indian space programme"), """India's space programme develops launch systems, satellites and space applications for communication, navigation, Earth observation and science.""", ConfidenceLevel.HIGH, false)
    )
}
