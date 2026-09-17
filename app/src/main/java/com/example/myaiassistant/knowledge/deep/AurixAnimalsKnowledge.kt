package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixAnimalsKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Animals", "Mammals", listOf("mammal mammals"), """Mammals are vertebrates generally characterized by hair or fur and mammary glands.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Reptiles", listOf("reptile reptiles"), """Reptiles are ectothermic amniote vertebrates including snakes, lizards, turtles and crocodilians.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Birds", listOf("bird birds"), """Birds are feathered vertebrates, generally endothermic, with forelimbs modified as wings in the typical plan.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Amphibians", listOf("amphibian amphibians"), """Amphibians are vertebrates whose life cycles often include aquatic and terrestrial stages.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Insects", listOf("insect insects"), """Insects are arthropods with three main body regions and six legs in adults.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Food Chain", listOf("food chain food web"), """A food chain is a simplified sequence of feeding relationships; ecosystems usually contain interconnected food webs.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Animals", "Migration", listOf("animal migration"), """Animal migration is recurring movement between locations, often linked to food, reproduction or environmental conditions.""", ConfidenceLevel.HIGH, false),
    )
}
