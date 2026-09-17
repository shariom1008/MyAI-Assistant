package com.example.myaiassistant.knowledge
import java.util.Locale
import com.example.myaiassistant.knowledge.deep.*
data class AurixDeepKnowledgeItem(val category:String,val topic:String,val keywords:List<String>,val answer:String,val confidence:ConfidenceLevel=ConfidenceLevel.HIGH,val currentInformation:Boolean=false)
object AurixDeepKnowledge {
 private val entries=listOf(
            AurixBusinessKnowledge.entries,
            AurixBiologyKnowledge.entries,
            AurixMaritimeKnowledge.entries,
            AurixFinanceKnowledge.entries,
            AurixPhysicsKnowledge.entries,
            AurixEverydayKnowledge.entries,
            AurixIndiaKnowledge.entries,
            AurixAviationKnowledge.entries,
            AurixSpaceKnowledge.entries,
            AurixElectronicsKnowledge.entries,
            AurixMathematicsKnowledge.entries,
            AurixGeographyKnowledge.entries,
            AurixPharmaceuticalKnowledge.entries,
            AurixHumanBodyKnowledge.entries,
            AurixChemistryKnowledge.entries,
            AurixAIKnowledge.entries,
            AurixAnimalsKnowledge.entries,
            AurixQualityKnowledge.entries,
            AurixPlantsKnowledge.entries,
            AurixConstructionKnowledge.entries,
            AurixComputerKnowledge.entries,
            AurixHistoryKnowledge.entries,
            AurixEngineeringKnowledge.entries,
 ).flatten()
 fun search(question:String):AurixDeepKnowledgeItem?{
  val q=normalize(question);if(q.isBlank())return null
  var best:AurixDeepKnowledgeItem?=null;var bestScore=0
  for(e in entries){var score=0;for(k0 in e.keywords){val k=normalize(k0);if(k.isBlank())continue;if(q==k)score+=100 else if(q.contains(k))score+=60 else {val m=k.split(" ").count{w->w.length>=3&&q.contains(w)};score+=m*12}};val t=normalize(e.topic);if(t.isNotBlank()&&q.contains(t))score+=30;if(score>bestScore){bestScore=score;best=e}}
  return if(bestScore>=20)best else null
 }
fun all(): List<AurixDeepKnowledgeItem> {
    return entries
}
 private fun normalize(v:String)=v.lowercase(Locale.ENGLISH).replace(Regex("[^a-z0-9+.-]")," ").replace(Regex("\\s+")," ").trim()
}
