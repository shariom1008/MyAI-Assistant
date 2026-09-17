package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixBiologyKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Biology", "Cell", listOf("cell biology"), """The cell is the basic structural and functional unit of living organisms.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "DNA", listOf("dna genetics"), """DNA stores genetic information in cells.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "RNA", listOf("rna biology"), """RNA participates in gene expression, protein synthesis and many regulatory processes.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Photosynthesis", listOf("photosynthesis"), """Photosynthesis converts light energy into chemical energy; oxygenic photosynthesis uses CO2 and water and releases oxygen.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Mitochondria", listOf("mitochondria"), """Mitochondria are organelles important for cellular energy metabolism and oxidative phosphorylation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Ribosome", listOf("ribosome protein synthesis"), """Ribosomes translate mRNA information into polypeptide chains.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Gene", listOf("gene genetics"), """A gene is a functional DNA region associated with an RNA or protein product and/or its regulation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Genetics", listOf("genetics heredity"), """Genetics studies heredity, genes and biological variation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Microbiology", listOf("microbiology"), """Microbiology studies microorganisms and their biology and interactions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Immunology", listOf("immunology immune system"), """Immunology studies immune systems and immune responses.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Ecology", listOf("ecology ecosystem"), """Ecology studies interactions among organisms and their environment.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Evolution", listOf("evolution biology"), """Biological evolution describes changes in heritable characteristics of populations across generations.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Virus", listOf("virus virology"), """Viruses are infectious biological agents that require host-cell machinery for replication.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Bacteria", listOf("bacteria bacteriology"), """Bacteria are diverse prokaryotic microorganisms with many metabolic lifestyles.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Neuroscience", listOf("neuroscience brain"), """Neuroscience studies the nervous system, neurons and brain function.""", ConfidenceLevel.HIGH, false),
    )
}
