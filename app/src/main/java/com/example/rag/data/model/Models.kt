package com.example.rag.data.model

enum class ChunkingStrategy(val displayName: String, val description: String) {
    SENTENCE_AWARE(
        displayName = "Sentence-Aware",
        description = "Preserves grammatical sentence boundaries. Groups complete thoughts together (100-200 tokens)."
    ),
    FIXED_SIZE_WITH_OVERLAP(
        displayName = "Fixed-Size (200t + 40t overlap)",
        description = "Strict window slicing (200 tokens/chunk with 40-token sliding window overlap)."
    )
}

data class DocumentItem(
    val id: String,
    val title: String,
    val category: String,
    val author: String,
    val summary: String,
    val content: String,
    val estimatedPages: Int = 1,
    val isCustom: Boolean = false
)

data class DocumentChunk(
    val id: String,
    val docId: String,
    val docTitle: String,
    val chunkIndex: Int,
    val strategy: ChunkingStrategy,
    val content: String,
    val tokenCount: Int,
    val embedding: List<Float> = emptyList()
)

data class Citation(
    val docId: String,
    val docTitle: String,
    val category: String,
    val chunkIndex: Int,
    val snippet: String,
    val similarityScore: Float
)

enum class RetrievalMode(val displayName: String, val shortName: String, val description: String) {
    HYBRID(
        displayName = "Hybrid (Dense Vector + SQLite FTS4)",
        shortName = "Hybrid (Vector + FTS4)",
        description = "Combines dense semantic vector retrieval with SQLite FTS4 inverted index keyword matching via Reciprocal Rank Fusion (RRF)."
    ),
    VECTOR(
        displayName = "Dense Vector Search (Cosine Similarity)",
        shortName = "Dense Vector",
        description = "Cosine similarity matching across normalized embeddings. Ideal for semantic and conceptual understanding."
    ),
    FTS4(
        displayName = "SQLite FTS4 Sparse Keyword Search",
        shortName = "SQLite FTS4",
        description = "Token-indexed BM25/TF-IDF sparse matching via SQLite FTS4. Ideal for exact acronyms, names, and formulas."
    )
}

data class DocumentInsights(
    val docId: String,
    val executiveSummary: String,
    val keyTakeaways: List<String>,
    val technicalConcepts: List<String>,
    val suggestedQuestions: List<String>
)

data class ToolCallRecord(
    val toolName: String,
    val input: String,
    val output: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val role: String, // "user", "assistant", "system"
    val text: String,
    val citations: List<Citation> = emptyList(),
    val toolCalls: List<ToolCallRecord> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val strategyUsed: ChunkingStrategy = ChunkingStrategy.SENTENCE_AWARE,
    val retrievalMode: RetrievalMode = RetrievalMode.HYBRID,
    val modelName: String = "gemini-3.5-flash",
    val latencyMs: Long = 0L,
    val isQuotaFallback: Boolean = false
)

data class CuratedQuery(
    val id: String,
    val query: String,
    val category: String,
    val targetDocId: String,
    val targetDocTitle: String,
    val rationale: String,
    val readyAnswer: String = "",
    val keyCitations: List<String> = emptyList()
)

data class RetrievalResult(
    val chunk: DocumentChunk,
    val score: Float,
    val rank: Int,
    val vectorScore: Float = score,
    val ftsScore: Float = 0f,
    val retrievalMode: RetrievalMode = RetrievalMode.HYBRID
)

data class BenchmarkComparison(
    val query: String,
    val sentenceResults: List<RetrievalResult>,
    val fixedResults: List<RetrievalResult>,
    val sentenceAvgScore: Float,
    val fixedAvgScore: Float,
    val evaluationVerdict: String,
    val vectorAvgScore: Float = 0f,
    val ftsAvgScore: Float = 0f,
    val hybridAvgScore: Float = 0f
)
