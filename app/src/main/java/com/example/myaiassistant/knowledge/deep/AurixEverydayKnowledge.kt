package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixEverydayKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Everyday", "Why Sky Is Blue", listOf("why sky blue"), """The daytime sky appears blue mainly because shorter visible wavelengths are scattered more strongly by atmospheric molecules through Rayleigh scattering.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Why Ice Floats", listOf("why ice floats"), """Ice is less dense than liquid water because its hydrogen-bonded crystal structure is relatively open.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Rainbow", listOf("rainbow how forms"), """Rainbows arise from refraction, internal reflection and wavelength-dependent dispersion of sunlight in water droplets.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Thunder", listOf("thunder why"), """Lightning rapidly heats and expands nearby air, producing a pressure wave heard as thunder.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Boiling", listOf("boiling water"), """Boiling occurs when a liquid’s vapor pressure becomes sufficient for vapor bubbles to form throughout the liquid under the surrounding pressure.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Everyday", "Rust", listOf("rust iron"), """Rusting is electrochemical corrosion of iron in the presence of water and oxygen, producing iron oxides/hydroxides.""", ConfidenceLevel.HIGH, false),
    )
}
