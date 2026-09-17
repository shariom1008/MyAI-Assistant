package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixPlantsKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Plants", "Root", listOf("root plant"), """Roots commonly anchor plants and take up water and minerals.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Leaf", listOf("leaf plant"), """Leaves commonly perform photosynthesis, gas exchange and transpiration.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Stem", listOf("stem plant"), """Stems support plant structures and transport water, minerals and photosynthates through vascular tissues.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Flower", listOf("flower plant"), """A flower is the reproductive structure of angiosperms involved in pollination and seed formation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Seed", listOf("seed plant"), """A seed is a mature ovule containing an embryo and associated tissues for plant reproduction and dispersal.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Transpiration", listOf("transpiration plant"), """Transpiration is water-vapor loss from plants, mainly through stomata.""", ConfidenceLevel.HIGH, false),
    )
}
