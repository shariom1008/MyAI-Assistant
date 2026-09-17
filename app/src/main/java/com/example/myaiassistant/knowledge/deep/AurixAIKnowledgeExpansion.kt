package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixAIKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("AI", "Neural Networks", listOf("neural network layers weights activation", "neural networks"), """Neural networks use connected computational units with learned parameters; layers transform inputs through weighted operations and nonlinear activations.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Transformer", listOf("transformer attention neural network", "transformer"), """Transformers use attention mechanisms to model relationships among elements in sequences and are widely used in language and multimodal systems.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Embeddings", listOf("embeddings vector representation ai", "embeddings"), """An embedding represents an item such as text as a numerical vector so similarity and other relationships can be computed.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "Overfitting", listOf("overfitting machine learning generalization", "overfitting"), """Overfitting occurs when a model fits training data too closely and performs worse on unseen data.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("AI", "AI Evaluation", listOf("ai evaluation benchmark accuracy hallucination", "ai evaluation"), """AI evaluation uses task-specific tests and metrics to measure capabilities, reliability, robustness and failure modes.""", ConfidenceLevel.HIGH, false)
    )
}
