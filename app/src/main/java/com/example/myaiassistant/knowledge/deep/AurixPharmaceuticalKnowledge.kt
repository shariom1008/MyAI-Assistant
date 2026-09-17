package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixPharmaceuticalKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Pharmaceutical", "HPLC", listOf("hplc high performance liquid chromatography"), """HPLC separates, identifies and quantifies components using a liquid mobile phase and a stationary phase.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Dissolution", listOf("dissolution test"), """Dissolution testing characterizes drug release from a dosage form into a specified medium under defined conditions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Disintegration", listOf("disintegration test"), """Disintegration testing measures breakdown of a dosage form under specified conditions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Friability", listOf("friability tablet"), """Friability evaluates a tablet’s resistance to abrasion and mechanical shock.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Granulation", listOf("granulation pharma"), """Granulation converts powders into larger agglomerates to improve properties such as flow and compressibility.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Tablet Compression", listOf("tablet compression"), """Tablet compression compacts powder or granules into tablets using defined tooling and pressure.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Blend Uniformity", listOf("blend uniformity"), """Blend uniformity assesses consistency of active-ingredient distribution in a powder blend.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Assay", listOf("assay drug"), """Assay determines the quantity or potency of a target substance by an analytical procedure.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Impurities", listOf("pharma impurities"), """Pharmaceutical impurities may arise from synthesis, degradation, processing or storage.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Stability", listOf("pharmaceutical stability"), """Stability studies monitor quality attributes over time under specified storage conditions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "GMP", listOf("gmp good manufacturing practice"), """GMP provides principles and controls intended to ensure medicines are consistently produced and controlled to defined quality standards.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Process Validation", listOf("process validation"), """Process validation establishes documented evidence that a process can consistently produce product meeting predetermined requirements.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Cleaning Validation", listOf("cleaning validation"), """Cleaning validation demonstrates that an approved cleaning procedure consistently removes residues to predefined acceptable limits.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "OOS", listOf("oos out of specification"), """An OOS result is outside an approved specification or acceptance criterion and can require a documented investigation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "OOT", listOf("oot out of trend"), """An OOT result may meet specification but show an unusual departure from an established trend.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "CAPA", listOf("capa corrective preventive action"), """CAPA manages corrective actions and preventive actions addressing causes of problems and recurrence risk.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "ALCOA Plus", listOf("alcoa alcoa plus data integrity"), """ALCOA+ principles emphasize attributable, legible, contemporaneous, original and accurate data plus complete, consistent, enduring and available characteristics.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "FTIR", listOf("ftir infrared"), """FTIR measures infrared absorption patterns and can support material identification and characterization.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Pharmaceutical", "Dissolution Apparatus", listOf("dissolution apparatus basket paddle"), """Common pharmacopeial dissolution apparatus include basket and paddle systems; the required apparatus depends on the method or monograph.""", ConfidenceLevel.HIGH, false),
    )
}
