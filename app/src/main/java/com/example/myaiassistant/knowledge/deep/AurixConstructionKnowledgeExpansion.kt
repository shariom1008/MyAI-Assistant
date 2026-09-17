package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixConstructionKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Construction", "Concrete Mix Design", listOf("concrete mix design cement water aggregate", "concrete mix design"), """Concrete performance depends on cementitious materials, water, aggregates, admixtures and proportions selected for strength, workability, durability and exposure.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Foundation Types", listOf("building foundation isolated raft pile", "foundation types"), """Common foundation systems include shallow foundations such as isolated or raft foundations and deep foundations such as piles, selected from site and structural requirements.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Brick Masonry", listOf("brick masonry mortar wall", "brick masonry"), """Brick masonry uses masonry units joined with mortar; wall behavior depends on unit properties, mortar, geometry, workmanship and loading.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Construction", "Waterproofing", listOf("building waterproofing roof basement", "waterproofing"), """Waterproofing systems limit water ingress using membranes, coatings, drainage and correctly detailed joints and penetrations.""", ConfidenceLevel.HIGH, false)
    )
}
