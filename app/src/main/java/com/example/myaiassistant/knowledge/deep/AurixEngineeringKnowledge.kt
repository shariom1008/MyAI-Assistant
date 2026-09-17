package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixEngineeringKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Engineering", "Manufacturing", listOf("manufacturing"), """Manufacturing converts raw materials into products or components using processes, machines and labor.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "CNC", listOf("cnc computer numerical control"), """CNC machines execute programmed instructions to control machine tools for repeatable manufacturing.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Tolerance", listOf("engineering tolerance"), """Tolerance defines permitted variation around a nominal dimension.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Surface Finish", listOf("surface finish roughness"), """Surface finish describes surface texture and roughness characteristics.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Welding", listOf("welding"), """Welding joins materials using heat, pressure or a combination of them.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Casting", listOf("casting metal"), """Casting forms parts by introducing molten material into a mould and allowing it to solidify.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Machining", listOf("machining"), """Machining removes material with cutting tools to produce desired geometry.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Bearing", listOf("bearing machine"), """A bearing supports relative motion and helps manage friction and loads.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Pump", listOf("pump industrial"), """A pump transfers mechanical energy to move fluids.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Engineering", "Compressor", listOf("compressor air"), """A compressor raises gas pressure and supports transfer or storage applications.""", ConfidenceLevel.HIGH, false),
    )
}
