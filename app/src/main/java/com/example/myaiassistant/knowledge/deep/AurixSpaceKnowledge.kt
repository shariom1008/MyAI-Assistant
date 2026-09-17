package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixSpaceKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Space", "Solar System", listOf("solar system"), """The Solar System consists of the Sun and objects gravitationally bound to it, including planets, dwarf planets, moons, asteroids and comets.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Black Hole", listOf("black hole"), """A black hole is a region of spacetime from which light cannot escape once inside the event horizon.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Galaxy", listOf("galaxy galaxies"), """A galaxy is a large gravitationally bound system containing stars, gas, dust, dark matter and other components.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Light-Year", listOf("light year"), """A light-year is a distance unit equal to the distance light travels in a Julian year, about 9.46 trillion km.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Planet", listOf("planet"), """A planet is an astronomical body orbiting a star and meeting classification criteria such as hydrostatic equilibrium and orbital clearing in the Solar System definition.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Asteroid", listOf("asteroid"), """Asteroids are generally rocky or metallic small Solar System bodies.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Comet", listOf("comet"), """Comets are icy small bodies that can develop a coma and tail near the Sun.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Exoplanet", listOf("exoplanet"), """An exoplanet is a planet outside the Solar System orbiting another star or stellar remnant.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Nebula", listOf("nebula"), """A nebula is an interstellar cloud of gas and dust, often associated with star formation or stellar remnants.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Supernova", listOf("supernova"), """A supernova is a highly energetic stellar explosion or terminal stellar event.""", ConfidenceLevel.HIGH, false),
    )
}
