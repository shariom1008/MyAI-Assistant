package com.example.myaiassistant.knowledge

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AurixKnowledgeCache(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "aurix_knowledge_cache",
            Context.MODE_PRIVATE
        )

    // =====================================================
    // NORMALIZE QUESTION
    // =====================================================

    fun normalize(question: String): String {

        return question
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .replace("?", "")
            .trim()
    }

    // =====================================================
    // SAVE RESEARCH RESULT
    // =====================================================

    fun save(
        question: String,
        answer: String,
        confidence: ConfidenceLevel,
        sources: List<KnowledgeSource> = emptyList(),
        ttlMillis: Long? = null
    ) {

        val key = normalize(question)

        if (key.isBlank() || answer.isBlank()) {
            return
        }

        val json =
            JSONObject().apply {

                put("question", key)

                put(
                    "answer",
                    answer
                )

                put(
                    "confidence",
                    confidence.name
                )

                put(
                    "createdAt",
                    System.currentTimeMillis()
                )

                if (ttlMillis != null) {

                    put(
                        "expiresAt",
                        System.currentTimeMillis() +
                                ttlMillis
                    )
                }
            }

        // =================================================
        // SOURCES
        // =================================================

        val sourceArray =
            JSONArray()

        sources.forEach { source ->

            val sourceJson =
                JSONObject().apply {

                    put(
                        "name",
                        source.name
                    )

                    put(
                        "url",
                        source.url ?: ""
                    )

                    put(
                        "sourceType",
                        source.sourceType.name
                    )

                    put(
                        "retrievedAt",
                        source.retrievedAt ?: 0L
                    )
                }

            sourceArray.put(sourceJson)
        }

        json.put(
            "sources",
            sourceArray
        )

        preferences
            .edit()
            .putString(
                key,
                json.toString()
            )
            .apply()
    }

    // =====================================================
    // GET CACHED ANSWER
    // =====================================================

    fun get(
        question: String
    ): KnowledgeCacheEntry? {

        val key =
            normalize(question)

        if (key.isBlank()) {
            return null
        }

        val raw =
            preferences.getString(
                key,
                null
            )
                ?: return null

        return try {

            val json =
                JSONObject(raw)

            // =============================================
            // CHECK EXPIRY
            // =============================================

            val expiresAt =
                if (
                    json.has("expiresAt") &&
                    !json.isNull("expiresAt")
                ) {
                    json.getLong("expiresAt")
                } else {
                    null
                }

            if (
                expiresAt != null &&
                System.currentTimeMillis() > expiresAt
            ) {

                remove(question)

                return null
            }

            // =============================================
            // SOURCES
            // =============================================

            val sources =
                mutableListOf<KnowledgeSource>()

            val sourceArray =
                json.optJSONArray(
                    "sources"
                )
                    ?: JSONArray()

            for (
                i in 0 until sourceArray.length()
            ) {

                val sourceJson =
                    sourceArray.getJSONObject(i)

                val sourceType =
                    try {

                        SourceType.valueOf(
                            sourceJson.optString(
                                "sourceType"
                            )
                        )

                    } catch (
                        _: Exception
                    ) {

                        SourceType.UNKNOWN
                    }

                val retrievedAtValue =
                    sourceJson.optLong(
                        "retrievedAt",
                        0L
                    )

                sources.add(
                    KnowledgeSource(

                        name =
                            sourceJson.optString(
                                "name"
                            ),

                        url =
                            sourceJson
                                .optString("url")
                                .takeIf {
                                    it.isNotBlank()
                                },

                        sourceType =
                            sourceType,

                        retrievedAt =
                            retrievedAtValue
                                .takeIf {
                                    it != 0L
                                }
                    )
                )
            }

            // =============================================
            // RETURN CACHE ENTRY
            // =============================================

            KnowledgeCacheEntry(

                normalizedQuestion =
                    json.optString(
                        "question"
                    ),

                answer =
                    json.optString(
                        "answer"
                    ),

                confidence =
                    try {

                        ConfidenceLevel.valueOf(
                            json.optString(
                                "confidence"
                            )
                        )

                    } catch (
                        _: Exception
                    ) {

                        ConfidenceLevel.UNKNOWN
                    },

                sources =
                    sources,

                createdAt =
                    json.optLong(
                        "createdAt",
                        System.currentTimeMillis()
                    ),

                expiresAt =
                    expiresAt
            )

        } catch (
            _: Exception
        ) {

            null
        }
    }

    // =====================================================
    // CHECK IF QUESTION IS CACHED
    // =====================================================

    fun contains(
        question: String
    ): Boolean {

        return get(question) != null
    }

    // =====================================================
    // REMOVE ONE CACHE ENTRY
    // =====================================================

    fun remove(
        question: String
    ) {

        val key =
            normalize(question)

        preferences
            .edit()
            .remove(key)
            .apply()
    }

    // =====================================================
    // CLEAR COMPLETE KNOWLEDGE CACHE
    // =====================================================

    fun clear() {

        preferences
            .edit()
            .clear()
            .apply()
    }
}
