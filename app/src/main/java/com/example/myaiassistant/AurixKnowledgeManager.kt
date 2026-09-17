package com.example.myaiassistant

import android.content.Context
import com.example.myaiassistant.knowledge.AurixKnowledgeRouter
import com.example.myaiassistant.knowledge.AurixSourceVerifier
import com.example.myaiassistant.knowledge.ConfidenceLevel
import com.example.myaiassistant.knowledge.KnowledgeAnswer
import com.example.myaiassistant.knowledge.KnowledgeType

class AurixKnowledgeManager(
    context: Context
) {

    private val router =
        AurixKnowledgeRouter(context)

    private val sourceVerifier =
        AurixSourceVerifier()

    fun answer(
        question: String
    ): KnowledgeAnswer {

        val result =
            router.answer(question)

        // =====================================================
        // VERIFY SOURCES
        // =====================================================

        if (result.sources.isNotEmpty()) {

            val verifiedSources =
                sourceVerifier.verifyAll(
                    result.sources
                )

            return result.copy(
                sources = verifiedSources
            )
        }

        return result
    }

    fun shouldResearch(
        result: KnowledgeAnswer
    ): Boolean {

        return result.needsResearch ||
                result.knowledgeType ==
                KnowledgeType.UNKNOWN
    }

    fun hasReliableAnswer(
        result: KnowledgeAnswer
    ): Boolean {

        return result.answer.isNotBlank() &&
                result.confidence !=
                ConfidenceLevel.UNKNOWN &&
                result.confidence !=
                ConfidenceLevel.LOW
    }

    fun isLocalAnswer(
        result: KnowledgeAnswer
    ): Boolean {

        return result.knowledgeType ==
                KnowledgeType.LOCAL
    }

    fun isCachedAnswer(
        result: KnowledgeAnswer
    ): Boolean {

        return result.knowledgeType ==
                KnowledgeType.CACHED_RESEARCH
    }
}
