package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixBusinessKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Business", "Supply and Demand", listOf("supply demand"), """Supply and demand is a basic framework for analyzing relationships among quantities supplied, quantities demanded and prices.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Revenue", listOf("revenue business"), """Revenue is gross income from sales of goods or services before relevant expenses.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Profit", listOf("profit business"), """Profit is broadly revenue minus relevant expenses over a defined period.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Inventory", listOf("inventory stock business"), """Inventory includes raw materials, work in progress and finished goods held for production or sale.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Marketing", listOf("marketing business"), """Marketing covers activities for understanding customers, communicating value and bringing offerings to market.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Business", "Cash Flow", listOf("cash flow business"), """Cash flow is movement of cash inflows and outflows over a period.""", ConfidenceLevel.HIGH, false),
    )
}
