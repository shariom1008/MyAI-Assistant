package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixHistoryKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("History", "Archaeology", listOf("archaeology"), """Archaeology studies past human societies through material remains such as artefacts, structures and sites.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Industrial Revolution", listOf("industrial revolution"), """The Industrial Revolution was a major transformation involving mechanized production, factories, energy systems and transport.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "World War I", listOf("world war one wwi"), """World War I was a global conflict fought mainly from 1914 to 1918.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "World War II", listOf("world war two wwii"), """World War II was a global conflict from 1939 to 1945.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Indus Valley Civilization", listOf("indus valley harappan"), """The Indus Valley or Harappan Civilization was a Bronze Age South Asian civilization noted for planned settlements, drainage and trade.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Maurya Empire", listOf("maurya empire"), """The Maurya Empire was a major ancient Indian empire associated with Chandragupta Maurya and Ashoka.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Gupta Period", listOf("gupta empire"), """The Gupta period is associated with important developments in Indian science, mathematics, literature and art.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("History", "Indian Independence", listOf("indian independence"), """India became independent from British rule on 15 August 1947 and became a republic on 26 January 1950.""", ConfidenceLevel.HIGH, false),
    )
}
