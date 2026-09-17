package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixFinanceKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Finance", "GDP", listOf("gdp gross domestic product"), """GDP measures the market value of final goods and services produced within an economy’s territory over a specified period, using national-accounting methods.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Inflation", listOf("inflation"), """Inflation is a sustained increase in the general price level over time.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Interest Rate", listOf("interest rate"), """An interest rate expresses the cost or return on money relative to principal over a specified period.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Compound Interest", listOf("compound interest"), """Compound interest includes accumulated interest in the base on which later interest is calculated according to the compounding schedule.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Diversification", listOf("investment diversification"), """Diversification spreads exposures across assets or categories to reduce concentration risk.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Liquidity", listOf("liquidity finance"), """Liquidity describes how readily an asset can be converted to cash with limited loss of value.""", ConfidenceLevel.HIGH, false),
    )
}
