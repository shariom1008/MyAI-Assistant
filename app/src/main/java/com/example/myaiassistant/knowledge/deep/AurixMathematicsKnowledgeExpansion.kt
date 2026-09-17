package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixMathematicsKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Mathematics", "Derivatives", listOf("derivative calculus rate of change", "derivatives"), """A derivative measures the instantaneous rate of change of a function and is the limit of a difference quotient when the limit exists.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Integrals", listOf("integral calculus area antiderivative", "integrals"), """An integral can represent accumulated quantity or signed area and is related to antiderivatives through the fundamental theorem of calculus.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Matrices", listOf("matrix determinant inverse linear algebra", "matrices"), """Matrices represent arrays and linear transformations; determinants and inverses have important roles in solving linear systems when defined.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Vectors", listOf("vector magnitude direction dot product", "vectors"), """A vector has magnitude and direction; the dot product combines two vectors into a scalar and relates to their angle.""", ConfidenceLevel.HIGH, false)
    )
}
