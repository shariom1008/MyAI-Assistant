package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixGeographyKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Geography", "Continents", listOf("continents seven"), """The common seven-continent convention lists Asia, Africa, North America, South America, Antarctica, Europe and Australia/Oceania.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Equator", listOf("equator"), """The Equator is the 0° latitude line dividing Earth into Northern and Southern Hemispheres.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Prime Meridian", listOf("prime meridian"), """The Prime Meridian is the 0° longitude reference line conventionally associated with Greenwich.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Latitude", listOf("latitude"), """Latitude measures angular position north or south of the Equator.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Longitude", listOf("longitude"), """Longitude measures angular position east or west of the Prime Meridian.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Monsoon", listOf("monsoon"), """Monsoon is a seasonal circulation pattern associated with seasonal changes in winds and precipitation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Plate Tectonics", listOf("plate tectonics"), """Plate tectonics describes movement and interaction of large lithospheric plates.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Mountain", listOf("mountain"), """A mountain is an elevated landform rising significantly above surrounding terrain.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Desert", listOf("desert"), """A desert is a region characterized by low long-term precipitation; deserts may be hot or cold.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Ocean", listOf("ocean"), """Oceans are Earth’s large interconnected saltwater bodies and play major roles in climate and ecosystems.""", ConfidenceLevel.HIGH, false),
    )
}
