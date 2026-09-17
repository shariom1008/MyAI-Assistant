package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixPlantsKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Plants", "Photosynthesis", listOf("photosynthesis chlorophyll light glucose", "photosynthesis"), """Photosynthesis converts light energy into chemical energy; plants use carbon dioxide and water to synthesize carbohydrates while releasing oxygen in oxygenic photosynthesis.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Plant Hormones", listOf("plant hormones auxin gibberellin cytokinin", "plant hormones"), """Plant hormones such as auxin, gibberellin, cytokinin, abscisic acid and ethylene regulate growth and responses.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Xylem and Phloem", listOf("xylem phloem transport plant", "xylem and phloem"), """Xylem conducts water and mineral nutrients mainly upward from roots, while phloem distributes sugars and other organic compounds.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Plant Reproduction", listOf("plant reproduction pollination fertilization", "plant reproduction"), """Flowering plants reproduce through processes including pollination, fertilization and seed development.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Seed Germination", listOf("seed germination water oxygen temperature", "seed germination"), """Seed germination begins when suitable conditions activate metabolic growth; water, oxygen and temperature are important for many species.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Plants", "Plant Tropisms", listOf("phototropism gravitropism plant movement", "plant tropisms"), """Tropisms are directional growth responses to stimuli such as light or gravity.""", ConfidenceLevel.HIGH, false)
    )
}
