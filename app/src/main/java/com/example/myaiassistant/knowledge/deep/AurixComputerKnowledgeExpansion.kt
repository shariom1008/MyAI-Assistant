package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixComputerKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Computer", "Computer Network", listOf("computer network lan wan protocol", "computer network"), """Computer networks connect devices to exchange data using communication protocols; LANs cover local areas while WANs span larger regions.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Data Structures", listOf("data structures array linked list tree hash", "data structures"), """Data structures organize data for efficient operations; arrays, linked lists, trees and hash tables have different trade-offs.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Algorithms", listOf("algorithm complexity sorting searching", "algorithms"), """An algorithm is a finite procedure for solving a problem; time and space complexity describe resource growth as input size increases.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Computer", "Cybersecurity Basics", listOf("cybersecurity authentication encryption backup", "cybersecurity basics"), """Basic cybersecurity uses controls such as authentication, least privilege, encryption, patching and reliable backups to reduce risks.""", ConfidenceLevel.HIGH, false)
    )
}
