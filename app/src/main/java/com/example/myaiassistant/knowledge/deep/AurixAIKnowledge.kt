package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixAIKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("AI", "Artificial Intelligence", listOf("artificial intelligence ai"), """AI is the field of methods for computer systems to perform tasks associated with capabilities often linked to human intelligence.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Machine Learning", listOf("machine learning ml"), """Machine learning algorithms learn patterns from data for tasks such as prediction and classification.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Deep Learning", listOf("deep learning neural network"), """Deep learning uses multi-layer neural networks to learn complex representations.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Large Language Model", listOf("large language model llm"), """An LLM is a language model trained on large datasets to model and generate text.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Neural Network", listOf("neural network"), """An artificial neural network uses interconnected computational units and learned parameters to represent patterns.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "NLP", listOf("nlp natural language processing"), """NLP develops methods for computers to process and generate human language.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Computer Vision", listOf("computer vision"), """Computer vision extracts and interprets useful information from images and video.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "AI Hallucination", listOf("ai hallucination"), """An AI hallucination is plausible-sounding output that is unsupported, incorrect or fabricated.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Prompt", listOf("prompt ai"), """A prompt is input or instruction given to an AI model to specify a task or desired response.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "RAG", listOf("rag retrieval augmented generation"), """RAG retrieves external or local information and supplies it as context to improve answer grounding.""", ConfidenceLevel.HIGH, false),
    )
}
