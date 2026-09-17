package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixPharmaceuticalKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Pharmaceutical", "Bioavailability", listOf("bioavailability oral drug absorption", "bioavailability"), """Bioavailability is the fraction of an administered dose that reaches systemic circulation in an active form; intravenous dosing is conventionally treated as 100%.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Half Life", listOf("drug half life pharmacokinetics", "half life"), """A drug's elimination half-life is the time required for its concentration or amount in the relevant compartment to fall by half under the applicable kinetic conditions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "First Pass Effect", listOf("first pass metabolism hepatic first pass", "first pass effect"), """First-pass metabolism can reduce the systemic availability of orally administered drugs before they reach general circulation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Drug Receptor", listOf("drug receptor agonist antagonist", "drug receptor"), """Receptors are biological macromolecules that recognize ligands; agonists activate receptor signaling while antagonists block or reduce activation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Pharmaceutical Excipients", listOf("excipients tablet binder disintegrant", "pharmaceutical excipients"), """Excipients are non-active formulation ingredients used for functions such as binding, disintegration, lubrication, preservation or controlled release.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Stability Testing", listOf("drug stability testing degradation", "stability testing"), """Pharmaceutical stability testing evaluates how product quality changes with time under defined environmental and packaging conditions.""", ConfidenceLevel.HIGH, false)
    )
}
