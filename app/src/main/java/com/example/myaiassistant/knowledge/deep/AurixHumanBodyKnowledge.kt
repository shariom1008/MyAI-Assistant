package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixHumanBodyKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Human Body", "Blood", listOf("blood components"), """Blood contains plasma, red blood cells, white blood cells and platelets as major components.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "RBC", listOf("rbc red blood cells"), """Red blood cells mainly transport oxygen using hemoglobin.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "WBC", listOf("wbc white blood cells"), """White blood cells participate in immune defense.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Platelets", listOf("platelets thrombocytes"), """Platelets are cell fragments important for hemostasis and clot formation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Kidney", listOf("kidney renal"), """Kidneys filter blood and help regulate fluid, electrolytes and waste excretion.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Liver", listOf("liver hepatic"), """The liver has major roles in metabolism, bile production, synthesis and biotransformation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Heart", listOf("heart human"), """The heart pumps blood through pulmonary and systemic circulation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Lungs", listOf("lungs respiratory"), """Lungs are the primary organs for oxygen and carbon-dioxide gas exchange.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Brain", listOf("brain human"), """The brain coordinates cognition, movement, sensation and many regulatory functions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Digestive System", listOf("digestive system"), """The digestive system mechanically and chemically processes food and absorbs nutrients.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Bones", listOf("bones skeletal system"), """The adult human skeleton is commonly described as having 206 bones, with variation in counting conventions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Human Body", "Endocrine System", listOf("endocrine hormones"), """The endocrine system regulates body processes through hormones.""", ConfidenceLevel.HIGH, false),
    )
}
