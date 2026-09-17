package com.example.myaiassistant.knowledge

enum class KnowledgeType { LOCAL, CACHED_RESEARCH, LIVE_RESEARCH, UNKNOWN }
enum class ConfidenceLevel { VERY_HIGH, HIGH, MEDIUM, LOW, UNKNOWN }
enum class SourceType { OFFICIAL, GOVERNMENT, SCIENTIFIC, EDUCATIONAL, TRUSTED, SECONDARY, UNKNOWN }
data class KnowledgeSource(val name:String,val url:String?=null,val sourceType:SourceType=SourceType.UNKNOWN,val retrievedAt:Long?=null)
data class KnowledgeAnswer(val question:String,val answer:String,val knowledgeType:KnowledgeType,val confidence:ConfidenceLevel,val needsResearch:Boolean=false,val isCurrentInformation:Boolean=false,val sources:List<KnowledgeSource> = emptyList(),val createdAt:Long=System.currentTimeMillis())
data class KnowledgeCacheEntry(val normalizedQuestion:String,val answer:String,val confidence:ConfidenceLevel,val sources:List<KnowledgeSource> = emptyList(),val createdAt:Long=System.currentTimeMillis(),val expiresAt:Long?=null)
