package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixSpaceKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Space", "Orbital Velocity", listOf("orbital velocity satellite orbit", "orbital velocity"), """Orbital velocity is the speed required for an object to follow a particular orbit under gravity; for a circular orbit it depends on gravitational parameter and orbital radius.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Escape Velocity", listOf("escape velocity gravity", "escape velocity"), """Escape velocity is the minimum idealized speed needed to escape a body's gravitational field without further propulsion, neglecting atmospheric and other losses.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Rocket Staging", listOf("rocket staging stages propulsion", "rocket staging"), """Rocket staging discards spent mass during ascent so later stages can continue accelerating a reduced mass.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Satellite Orbits", listOf("leo meo geo satellite orbit", "satellite orbits", "leo"), """Common Earth orbits include low Earth orbit, medium Earth orbit and geostationary orbit, each serving different mission requirements.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Exoplanets", listOf("exoplanet transit radial velocity", "exoplanets"), """Exoplanets are planets beyond the Solar System; methods such as transits and radial velocity can reveal their presence and properties.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Space", "Space Debris", listOf("space debris orbital debris", "space debris"), """Space debris consists of human-made objects and fragments left in orbit; it can threaten spacecraft through high-speed collisions.""", ConfidenceLevel.HIGH, false)
    )
}
