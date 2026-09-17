package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixPhysicsKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Physics", "Gravitation", listOf("gravity gravitation universal law", "gravitation"), """Newton's law of universal gravitation states that two masses attract with force F = G m1 m2 / r².""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Simple Harmonic Motion", listOf("simple harmonic motion shm", "simple harmonic motion"), """In simple harmonic motion, restoring acceleration is proportional to displacement and directed toward equilibrium.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Doppler Effect", listOf("doppler effect sound light", "doppler effect"), """The Doppler effect is the observed change in frequency caused by relative motion between a wave source and an observer.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Electromagnetic Induction", listOf("electromagnetic induction faraday", "electromagnetic induction"), """Faraday's law states that changing magnetic flux through a circuit induces an electromotive force.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Wave Interference", listOf("interference waves constructive destructive", "wave interference"), """Interference is the superposition of waves; it can be constructive or destructive depending on phase.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Photoelectric Effect", listOf("photoelectric effect photons", "photoelectric effect"), """The photoelectric effect occurs when electromagnetic radiation supplies enough energy to eject electrons from a material; photon energy is E = hf.""", ConfidenceLevel.HIGH, false)
    )
}
