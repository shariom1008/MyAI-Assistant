package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixMaritimeKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Maritime", "Metacentric Height", listOf("ship metacentric height stability", "metacentric height"), """Metacentric height is a measure used in assessing a ship's initial transverse stability; its interpretation depends on the vessel's geometry and loading.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Ship Propulsion", listOf("marine propulsion diesel electric steam", "ship propulsion"), """Marine propulsion systems include diesel, gas turbine, electric and hybrid arrangements, selected according to vessel type and operating profile.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Navigation Lights", listOf("maritime navigation lights colregs", "navigation lights"), """Navigation lights communicate a vessel's status, aspect and maneuvering information according to applicable maritime rules.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Port Operations", listOf("port terminal berth cargo", "port operations"), """Port operations coordinate vessel berthing, cargo handling, storage, customs and inland transport connections.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Ballast Water", listOf("ballast water ship invasive species", "ballast water"), """Ballast water stabilizes ships but can transport organisms between regions, so ships follow applicable ballast-water management requirements.""", ConfidenceLevel.HIGH, false)
    )
}
