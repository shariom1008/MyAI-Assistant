package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixQualityKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Quality", "CAPA", listOf("capa corrective preventive action", "capa"), """CAPA is a quality-system process for addressing causes of problems and preventing recurrence through corrective and preventive actions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Good Documentation Practices", listOf("gdp documentation quality", "good documentation practices"), """Good documentation practices emphasize attributable, legible, contemporaneous, original and accurate records, with appropriate controls for corrections.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Quality", "Audit", listOf("quality audit internal external", "audit"), """A quality audit is a systematic examination of processes or records against defined requirements to identify conformity and improvement opportunities.""", ConfidenceLevel.HIGH, false)
    )
}
