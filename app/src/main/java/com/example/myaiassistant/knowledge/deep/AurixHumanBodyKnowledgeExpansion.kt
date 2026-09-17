package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixHumanBodyKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("HumanBody", "Blood Components", listOf("blood plasma red cells white cells platelets", "blood components"), """Blood consists mainly of plasma, red blood cells, white blood cells and platelets, each serving distinct transport, defense or clotting roles.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("HumanBody", "Kidney Function", listOf("kidney nephron filtration urine", "kidney function"), """Kidneys filter blood through nephrons and regulate water, electrolytes, acid-base balance and waste excretion.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("HumanBody", "Liver Function", listOf("liver metabolism detoxification bile", "liver function"), """The liver performs major roles in metabolism, nutrient processing, bile production and biotransformation of many substances.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("HumanBody", "Nervous System", listOf("nervous system brain spinal cord nerves", "nervous system"), """The nervous system receives information, integrates signals and coordinates rapid responses through the brain, spinal cord and peripheral nerves.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("HumanBody", "Endocrine System", listOf("endocrine hormones glands", "endocrine system"), """The endocrine system uses hormones released by glands to regulate processes such as growth, metabolism, reproduction and stress responses.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("HumanBody", "Diabetes and Insulin", listOf("diabetes insulin blood sugar glucose diabetes mein insulin insulin ka role", "diabetes and insulin", "insulin"), """Insulin is a hormone produced by the pancreas that helps regulate blood glucose by promoting glucose uptake and storage. In type 1 diabetes, the body produces little or no insulin; in type 2 diabetes, insulin resistance and impaired insulin production can contribute to elevated blood glucose.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("HumanBody", "Immune System", listOf("immune system innate adaptive immunity", "immune system"), """Innate immunity provides rapid general defenses, while adaptive immunity uses specialized lymphocytes and immunological memory.""", ConfidenceLevel.HIGH, false)
    )
}
