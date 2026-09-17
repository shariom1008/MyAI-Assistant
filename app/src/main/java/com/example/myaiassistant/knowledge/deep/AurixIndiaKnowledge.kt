package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixIndiaKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("India", "Constitution", listOf("indian constitution"), """The Constitution of India is the supreme constitutional framework defining government institutions, powers, rights and principles.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Parliament", listOf("indian parliament"), """India’s Parliament consists of the President and two Houses: Lok Sabha and Rajya Sabha.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Fundamental Rights", listOf("fundamental rights india"), """Part III of the Constitution contains Fundamental Rights, subject to constitutional provisions and limitations.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Indian Rupee", listOf("indian rupee inr currency"), """The Indian Rupee is India’s currency; its ISO code is INR.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Himalayas", listOf("himalayas"), """The Himalayas are a major mountain system north of the Indian subcontinent.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Indian Monsoon", listOf("indian monsoon"), """The Indian monsoon is a seasonal circulation system that strongly influences rainfall patterns.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Agriculture", listOf("indian agriculture"), """Agriculture is a major Indian sector covering cereals, pulses, oilseeds, fibres, horticulture and other crops.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Biosphere Reserve", listOf("biosphere reserve india"), """Biosphere reserves integrate biodiversity conservation, sustainable development and research.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("India", "Ramsar Site", listOf("ramsar site india"), """Ramsar Sites are wetlands recognized as internationally important under the Ramsar Convention.""", ConfidenceLevel.HIGH, false),
    )
}
