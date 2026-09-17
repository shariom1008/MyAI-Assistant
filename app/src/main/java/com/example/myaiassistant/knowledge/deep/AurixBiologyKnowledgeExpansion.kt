package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixBiologyKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Biology", "DNA Replication", listOf("dna replication helicase polymerase", "dna replication", "dna"), """DNA replication copies genetic material using complementary base pairing, with DNA polymerases extending new strands.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "RNA and Transcription", listOf("rna transcription mrna", "rna and transcription", "rna"), """Transcription uses a DNA template to synthesize RNA; messenger RNA can carry coding information to ribosomes.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Translation", listOf("translation ribosome protein synthesis", "translation"), """During translation, ribosomes read mRNA codons and link amino acids into a polypeptide.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Cell Cycle", listOf("cell cycle g1 s g2 mitosis", "cell cycle"), """The cell cycle includes growth phases, DNA synthesis and division; checkpoints help regulate progression.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Evolution by Natural Selection", listOf("natural selection evolution adaptation", "evolution by natural selection"), """Natural selection changes the frequency of heritable traits when individuals with advantageous traits leave more surviving offspring.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Biology", "Ecology Food Web", listOf("food chain food web trophic levels", "ecology food web"), """Food webs represent feeding relationships among organisms, with energy generally decreasing at higher trophic levels.""", ConfidenceLevel.HIGH, false)
    )
}
