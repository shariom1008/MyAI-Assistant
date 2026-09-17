package com.example.myaiassistant.knowledge

import android.content.Context
import com.example.myaiassistant.AurixKnowledgeEngine

class AurixKnowledgeRouter(
    context: Context
) {

    private val cache =
        AurixKnowledgeCache(context)

    fun answer(
        question: String
    ): KnowledgeAnswer {

        val cleanQuestion =
            question.trim()

        if (cleanQuestion.isBlank()) {

            return KnowledgeAnswer(
                question = question,
                answer = "",
                knowledgeType = KnowledgeType.UNKNOWN,
                confidence = ConfidenceLevel.UNKNOWN,
                needsResearch = true
            )
        }

        // =====================================================
        // 1. EXISTING LOCAL AURIX KNOWLEDGE ENGINE
        // =====================================================

        val localAnswer =
            try {

                AurixKnowledgeEngine.answer(
                    cleanQuestion
                )

            } catch (
                _: Exception
            ) {

                null
            }

        if (
            !localAnswer.isNullOrBlank()
        ) {

            return KnowledgeAnswer(

                question =
                    cleanQuestion,

                answer =
                    localAnswer,

                knowledgeType =
                    KnowledgeType.LOCAL,

                confidence =
                    ConfidenceLevel.HIGH,

                needsResearch =
                    false,

                isCurrentInformation =
                    false
            )
        }

        // =====================================================
        // 2. DEEP LOCAL KNOWLEDGE
        // =====================================================

        val deepKnowledge =
            try {

                AurixDeepKnowledge.search(
                    cleanQuestion
                )

            } catch (
                _: Exception
            ) {

                null
            }

        if (
            deepKnowledge != null &&
            deepKnowledge.answer.isNotBlank()
        ) {

            return KnowledgeAnswer(

                question =
                    cleanQuestion,

                answer =
                    deepKnowledge.answer,

                knowledgeType =
                    KnowledgeType.LOCAL,

                confidence =
                    deepKnowledge.confidence,

                needsResearch =
                    false,

                isCurrentInformation =
                    deepKnowledge.currentInformation
            )
        }

        // =====================================================
        // 3. KNOWLEDGE CACHE
        // =====================================================

        val cachedAnswer =
            try {

                cache.get(
                    cleanQuestion
                )

            } catch (
                _: Exception
            ) {

                null
            }

        if (
            cachedAnswer != null &&
            cachedAnswer.answer.isNotBlank()
        ) {

            return KnowledgeAnswer(

                question =
                    cleanQuestion,

                answer =
                    cachedAnswer.answer,

                knowledgeType =
                    KnowledgeType.CACHED_RESEARCH,

                confidence =
                    cachedAnswer.confidence,

                needsResearch =
                    false,

                isCurrentInformation =
                    false,

                sources =
                    cachedAnswer.sources,

                createdAt =
                    cachedAnswer.createdAt
            )
        }

        // =====================================================
        // 4. NOTHING FOUND
        // =====================================================

        return KnowledgeAnswer(

            question =
                cleanQuestion,

            answer =
                "",

            knowledgeType =
                KnowledgeType.UNKNOWN,

            confidence =
                ConfidenceLevel.UNKNOWN,

            needsResearch =
                true,

            isCurrentInformation =
                false
        )
    }
}
