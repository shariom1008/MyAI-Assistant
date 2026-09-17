package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixEngineeringKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Engineering", "Stress and Strain", listOf("stress strain young modulus", "stress and strain"), """Stress is force per area, while strain is deformation relative to original dimension; Young's modulus relates them in the linear elastic regime for suitable materials.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Factor of Safety", listOf("factor safety engineering design", "factor of safety"), """A factor of safety provides design margin between an allowable or design condition and a relevant failure or limit state.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Fluid Mechanics", listOf("fluid mechanics pressure flow bernoulli", "fluid mechanics"), """Fluid mechanics studies fluids at rest and in motion; Bernoulli's equation relates pressure, velocity and elevation under specified ideal assumptions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Thermal Expansion", listOf("thermal expansion coefficient", "thermal expansion"), """Most materials change dimensions with temperature; the coefficient of thermal expansion quantifies the change for a given temperature interval.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Control Systems", listOf("control system feedback pid", "control systems"), """Control systems regulate a process using measurements and control actions; PID controllers combine proportional, integral and derivative terms.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Engineering Materials", listOf("engineering materials metals ceramics polymers composites", "engineering materials"), """Engineering materials include metals, ceramics, polymers and composites, selected according to mechanical, thermal, chemical and manufacturing requirements.""", ConfidenceLevel.HIGH, false)
    )
}
