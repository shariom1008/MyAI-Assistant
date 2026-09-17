package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixHistoryKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("History", "Neolithic Revolution", listOf("neolithic revolution agriculture domestication", "neolithic revolution"), """The Neolithic transition involved increasing reliance on agriculture, animal domestication and settled communities in several regions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Bronze Age Civilizations", listOf("bronze age mesopotamia egypt indus china", "bronze age civilizations"), """Bronze Age societies developed complex settlements, metallurgy, trade networks and administrative institutions in several parts of the world.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Roman Republic", listOf("roman republic senate consul", "roman republic"), """The Roman Republic developed institutions including elected magistrates and a Senate before the later imperial system.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Decolonization", listOf("decolonization independence colonies twentieth century", "decolonization"), """Decolonization was the process by which many territories gained political independence from European empires, especially during the twentieth century.""", ConfidenceLevel.HIGH, false)
    )
}
