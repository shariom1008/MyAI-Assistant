package com.example.myaiassistant.knowledge
import android.content.Context
import com.example.myaiassistant.AurixKnowledgeEngine
class AurixKnowledgeRouter(context:Context){
 private val cache=AurixKnowledgeCache(context)
 fun answer(question:String):KnowledgeAnswer{
  val q=question.trim();if(q.isBlank())return KnowledgeAnswer(question,"",KnowledgeType.UNKNOWN,ConfidenceLevel.UNKNOWN,true)
  val local=try{AurixKnowledgeEngine.answer(q)}catch(_:Exception){null}
  if(!local.isNullOrBlank())return KnowledgeAnswer(q,local,KnowledgeType.LOCAL,ConfidenceLevel.HIGH,false,false)
  val deep=try{AurixDeepKnowledge.search(q)}catch(_:Exception){null}
  if(deep!=null&&deep.answer.isNotBlank())return KnowledgeAnswer(q,deep.answer,KnowledgeType.LOCAL,deep.confidence,false,deep.currentInformation)
  val cached=try{cache.get(q)}catch(_:Exception){null}
  if(cached!=null&&cached.answer.isNotBlank())return KnowledgeAnswer(q,cached.answer,KnowledgeType.CACHED_RESEARCH,cached.confidence,false,false,cached.sources,cached.createdAt)
  return KnowledgeAnswer(q,"",KnowledgeType.UNKNOWN,ConfidenceLevel.UNKNOWN,true,false)
 }
}
