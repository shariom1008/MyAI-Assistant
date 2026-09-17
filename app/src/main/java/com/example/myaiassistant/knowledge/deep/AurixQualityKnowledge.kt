package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixQualityKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Quality", "Deviation", listOf("deviation quality"), """A deviation is a documented departure from an approved procedure, process or expected condition.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Change Control", listOf("change control"), """Change control manages assessment, approval, implementation and documentation of proposed changes.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Root Cause Analysis", listOf("root cause analysis rca"), """RCA is a structured approach for identifying underlying causes of a problem.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Risk Assessment", listOf("risk assessment quality"), """Risk assessment evaluates hazards and factors such as severity, likelihood and controls to support risk-based decisions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Audit Trail", listOf("audit trail"), """An electronic audit trail records relevant creation, modification or deletion history of electronic records.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Data Integrity", listOf("data integrity"), """Data integrity means data remain complete, consistent, accurate and trustworthy throughout their lifecycle.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "SOP", listOf("sop standard operating procedure"), """An SOP is an approved written instruction describing how a routine process is to be performed.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Calibration", listOf("calibration instrument"), """Calibration compares instrument indication with a reference standard to establish and document measurement performance.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Qualification", listOf("qualification equipment"), """Qualification provides documented evidence that equipment is suitable for intended use under defined requirements.""", ConfidenceLevel.HIGH, false),
    )
}
