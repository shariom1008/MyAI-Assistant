package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixConstructionKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Construction", "Concrete", listOf("concrete"), """Concrete commonly combines cementitious binder, aggregates, water and optional admixtures.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Reinforced Concrete", listOf("reinforced concrete rcc"), """Reinforced concrete combines concrete with embedded reinforcement to carry structural actions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Cement", listOf("cement portland"), """Cement is a hydraulic binder that hardens through hydration and binds concrete or mortar constituents.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Aggregate", listOf("aggregate construction"), """Aggregate is granular material used as a major constituent of concrete.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Brick", listOf("brick building"), """Brick is a modular masonry unit commonly made from fired clay or other specified materials.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Slab", listOf("slab rcc"), """A slab is generally a horizontal structural element transferring loads to supporting members.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Column", listOf("column structural"), """A column is a vertical structural member commonly carrying axial compression along with other possible actions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Foundation", listOf("foundation building"), """A foundation transfers structural loads to the ground and supports stability.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Plaster", listOf("plaster wall"), """Plaster is a finishing/protective layer applied to walls or ceilings.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Surveying", listOf("surveying land"), """Surveying measures or establishes positions, distances, elevations and boundaries.""", ConfidenceLevel.HIGH, false),
    )
}
