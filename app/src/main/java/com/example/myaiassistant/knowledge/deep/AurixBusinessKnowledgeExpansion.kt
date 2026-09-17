package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixBusinessKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Business", "Business Model", listOf("business model value proposition revenue", "business model"), """A business model describes how an organization creates, delivers and captures value, including customers, activities, resources and revenue mechanisms.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Supply Chain", listOf("supply chain procurement logistics inventory", "supply chain"), """A supply chain coordinates sourcing, production, inventory, logistics and delivery from inputs to customers.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Marketing Mix", listOf("marketing mix 4ps product price place promotion", "marketing mix"), """The traditional marketing mix uses product, price, place and promotion as a framework for planning market offerings.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Break Even", listOf("break even fixed variable costs", "break even"), """Break-even analysis identifies the sales level at which total revenue equals total costs under specified assumptions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Business Risk", listOf("business risk operational market financial", "business risk"), """Business risks can arise from market conditions, operations, finance, technology, regulation and other uncertainties.""", ConfidenceLevel.HIGH, false)
    )
}
