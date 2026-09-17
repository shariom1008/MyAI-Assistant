package com.example.myaiassistant.knowledge
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
class AurixKnowledgeCache(context:Context){
 private val preferences=context.getSharedPreferences("aurix_knowledge_cache",Context.MODE_PRIVATE)
 fun normalize(question:String)=question.trim().lowercase().replace(Regex("\\s+")," ").replace("?","").trim()
 fun save(question:String,answer:String,confidence:ConfidenceLevel,sources:List<KnowledgeSource> = emptyList(),ttlMillis:Long?=null){val key=normalize(question);if(key.isBlank()||answer.isBlank())return;val now=System.currentTimeMillis();val j=JSONObject().apply{put("question",key);put("answer",answer);put("confidence",confidence.name);put("createdAt",now);if(ttlMillis!=null)put("expiresAt",now+ttlMillis)};val a=JSONArray();sources.forEach{s->a.put(JSONObject().apply{put("name",s.name);put("url",s.url?:"");put("sourceType",s.sourceType.name);put("retrievedAt",s.retrievedAt?:0L)})};j.put("sources",a);preferences.edit().putString(key,j.toString()).apply()}
 fun get(question:String):KnowledgeCacheEntry?{val key=normalize(question);if(key.isBlank())return null;val raw=preferences.getString(key,null)?:return null;return try{val j=JSONObject(raw);val exp=if(j.has("expiresAt")&&!j.isNull("expiresAt"))j.getLong("expiresAt")else null;if(exp!=null&&System.currentTimeMillis()>exp){remove(question);return null};val src=mutableListOf<KnowledgeSource>();val a=j.optJSONArray("sources")?:JSONArray();for(i in 0 until a.length()){val s=a.getJSONObject(i);val st=try{SourceType.valueOf(s.optString("sourceType"))}catch(_:Exception){SourceType.UNKNOWN};src.add(KnowledgeSource(s.optString("name"),s.optString("url").takeIf{it.isNotBlank()},st,s.optLong("retrievedAt",0L).takeIf{it!=0L}))};KnowledgeCacheEntry(j.optString("question"),j.optString("answer"),try{ConfidenceLevel.valueOf(j.optString("confidence"))}catch(_:Exception){ConfidenceLevel.UNKNOWN},src,j.optLong("createdAt",System.currentTimeMillis()),exp)}catch(_:Exception){null}}
 fun contains(question:String)=get(question)!=null
 fun remove(question:String){preferences.edit().remove(normalize(question)).apply()}
 fun clear(){preferences.edit().clear().apply()}
}
