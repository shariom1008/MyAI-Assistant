package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixChemistryKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Chemistry", "Periodic Trends", listOf("periodic trends atomic radius electronegativity", "periodic trends"), """Across a period atomic radius generally decreases while ionization energy and electronegativity generally increase, with known exceptions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Chemical Equilibrium", listOf("chemical equilibrium le chatelier", "chemical equilibrium"), """At dynamic chemical equilibrium, forward and reverse reaction rates are equal; changing conditions can shift the equilibrium composition.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Acids and Bases", listOf("acids bases ph pka", "acids and bases"), """Acids donate protons in Brønsted–Lowry theory, bases accept them; pH measures hydrogen-ion activity on a logarithmic scale.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Redox Reactions", listOf("redox oxidation reduction", "redox reactions"), """Oxidation involves loss of electrons or an increase in oxidation state, while reduction involves electron gain or a decrease in oxidation state.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Organic Functional Groups", listOf("organic functional groups alcohol aldehyde ketone", "organic functional groups"), """Functional groups such as hydroxyl, carbonyl, carboxyl and amino groups strongly influence the reactions and properties of organic molecules.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Polymers", listOf("polymer polymerization addition condensation", "polymers"), """Polymers are large molecules built from repeating units; common formation mechanisms include addition and condensation polymerization.""", ConfidenceLevel.HIGH, false)
    )
}
