package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixMathematicsKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Mathematics", "Pythagorean Theorem", listOf("pythagorean theorem"), """For a right triangle, a² + b² = c² where c is the hypotenuse.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Quadratic Equation", listOf("quadratic equation formula"), """For ax²+bx+c=0, roots are (-b ± √(b²−4ac))/(2a), with a nonzero.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Percentage", listOf("percentage percent"), """A percentage expresses a ratio per hundred: x% = x/100.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Probability", listOf("probability"), """For equally likely finite outcomes, probability is favorable outcomes divided by total outcomes.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Mean", listOf("mean average"), """Arithmetic mean is the sum of values divided by their count.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Median", listOf("median statistics"), """Median is the middle ordered value, or average of the two middle values for an even count.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Standard Deviation", listOf("standard deviation"), """Standard deviation measures dispersion of values around their mean.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Derivative", listOf("derivative differentiation"), """A derivative represents instantaneous rate of change under appropriate mathematical conditions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Integral", listOf("integral integration"), """An integral represents accumulation and has an inverse relationship with differentiation under suitable conditions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Mathematics", "Pi", listOf("pi value"), """Pi is the circle circumference-to-diameter ratio, approximately 3.141592653589793.""", ConfidenceLevel.HIGH, false),
    )
}
