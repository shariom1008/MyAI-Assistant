package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixMaritimeKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Maritime", "Buoyancy", listOf("buoyancy archimedes"), """Buoyant force is the upward force associated with displaced fluid; Archimedes principle relates it to displaced fluid weight.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Ship Displacement", listOf("ship displacement"), """Ship displacement refers to the mass or weight of water displaced by a vessel, equivalent to vessel weight in equilibrium.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Hull", listOf("ship hull"), """The hull is the main watertight body of a vessel and provides buoyancy and structural integrity.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Propeller", listOf("ship propeller"), """A marine propeller generates thrust by transferring momentum to water.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Port and Starboard", listOf("port starboard"), """Port is the vessel’s left side and starboard its right side when facing forward.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Draft", listOf("ship draft"), """Draft is the vertical distance from the waterline to the vessel’s lowest submerged point.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Maritime", "Bridge", listOf("ship bridge"), """The bridge supports navigation, communication and operational control of a vessel.""", ConfidenceLevel.HIGH, false),
    )
}
