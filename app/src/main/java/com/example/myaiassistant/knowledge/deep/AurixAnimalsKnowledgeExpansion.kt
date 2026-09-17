package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixAnimalsKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Animals", "Mammal", listOf("mammals hair milk warm blooded", "mammal"), """Mammals are vertebrates characterized by traits including hair and mammary glands; most maintain relatively stable internal body temperatures.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Bird", listOf("birds feathers beak eggs", "bird"), """Birds are feathered vertebrates with beaks; most species reproduce by laying eggs and many are adapted for flight.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Reptile", listOf("reptiles scales ectotherm", "reptile"), """Reptiles are amniote vertebrates commonly characterized by keratinized scales and generally ectothermic physiology.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Amphibian", listOf("amphibian frog salamander", "amphibian"), """Amphibians commonly have life cycles associated with both aquatic and terrestrial environments and often have permeable skin.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Animal Communication", listOf("animal communication signals behavior", "animal communication"), """Animals communicate using visual, acoustic, chemical, tactile and other signals that can influence behavior.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Conservation", listOf("wildlife conservation habitat biodiversity", "conservation"), """Wildlife conservation aims to maintain populations, habitats and biodiversity through measures such as habitat protection, sustainable management and species recovery.""", ConfidenceLevel.HIGH, false)
    )
}
