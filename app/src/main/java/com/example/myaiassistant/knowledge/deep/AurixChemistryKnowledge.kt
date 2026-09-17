package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixChemistryKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Chemistry", "Atom", listOf("atom atomic structure"), """An atom is the basic unit retaining an element’s chemical identity, with a nucleus and electron cloud.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Molecule", listOf("molecule"), """A molecule is a group of bonded atoms representing a molecular entity.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Mole", listOf("mole chemistry"), """One mole contains exactly 6.02214076 × 10^23 specified entities.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "pH", listOf("ph scale"), """For dilute aqueous systems, pH is commonly defined as −log10 of hydrogen-ion activity; concentration is an approximation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Acid", listOf("acid chemistry"), """A Brønsted–Lowry acid is a proton donor.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Base", listOf("base chemistry"), """A Brønsted–Lowry base is a proton acceptor.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Catalyst", listOf("catalyst"), """A catalyst changes reaction rate through an alternative pathway and is regenerated overall.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Stoichiometry", listOf("stoichiometry"), """Stoichiometry uses balanced chemical equations to calculate quantitative relationships.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Equilibrium", listOf("chemical equilibrium"), """At dynamic equilibrium, forward and reverse reaction rates are equal.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Electrochemistry", listOf("electrochemistry"), """Electrochemistry studies relationships between chemical reactions and electrical energy.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Organic Chemistry", listOf("organic chemistry"), """Organic chemistry primarily studies carbon-containing compounds and their reactions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Polymer", listOf("polymer"), """A polymer is a large molecule built from repeating structural units.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Chromatography", listOf("chromatography"), """Chromatography separates mixture components by differential interactions with stationary and mobile phases.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Spectroscopy", listOf("spectroscopy"), """Spectroscopy studies interactions of matter with electromagnetic radiation for characterization.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Chemistry", "Buffer", listOf("buffer solution"), """A buffer resists large pH changes when limited acid or base is added.""", ConfidenceLevel.HIGH, false),
    )
}
