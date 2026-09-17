package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixGeographyKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Geography", "Climate Zones", listOf("climate zones tropical temperate polar", "climate zones"), """Climate zones classify long-term patterns of temperature and precipitation; broad categories include tropical, temperate and polar climates.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "River Basin", listOf("river basin watershed drainage basin", "river basin"), """A river basin is the land area drained by a river and its tributaries, bounded by topographic divides.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Latitude and Longitude", listOf("latitude longitude coordinates", "latitude and longitude"), """Latitude measures angular position north or south of the equator, while longitude measures angular position east or west of a reference meridian.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Geography", "Erosion", listOf("erosion weathering deposition", "erosion"), """Weathering breaks down material, erosion transports it, and deposition lays transported sediment down in a new location.""", ConfidenceLevel.HIGH, false)
    )
}
