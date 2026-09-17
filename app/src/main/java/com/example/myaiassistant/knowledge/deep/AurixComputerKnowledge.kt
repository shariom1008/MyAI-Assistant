package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixComputerKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Computer", "CPU", listOf("cpu central processing unit"), """The CPU executes instructions and performs arithmetic, logic and control operations.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "RAM", listOf("ram random access memory"), """RAM is volatile working memory used for active programs and data.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Operating System", listOf("operating system os"), """An operating system manages hardware resources and provides services to applications.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Database", listOf("database"), """A database stores, organizes and retrieves structured information.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "API", listOf("api application programming interface"), """An API is a defined interface allowing software components to communicate and use specified capabilities.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Internet", listOf("internet network"), """The Internet is a global system of interconnected networks using standard communication protocols.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "HTTP", listOf("http https"), """HTTP is a web application-layer protocol; HTTPS adds TLS protection.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Encryption", listOf("encryption"), """Encryption transforms readable information into protected form using cryptographic methods.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Cloud Computing", listOf("cloud computing"), """Cloud computing provides on-demand access to remote computing resources over networks.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Android", listOf("android os"), """Android is a mobile-focused operating-system platform with a Linux-based kernel and Android application framework.""", ConfidenceLevel.HIGH, false),
    )
}
