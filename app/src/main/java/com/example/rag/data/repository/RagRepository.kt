package com.example.rag.data.repository

import android.content.Context
import android.net.Uri
import com.example.rag.data.local.ChatMessageEntity
import com.example.rag.data.local.ChunkEntity
import com.example.rag.data.local.ChunkFtsEntity
import com.example.rag.data.local.DocumentEntity
import com.example.rag.data.local.RagDao
import com.example.rag.data.model.BenchmarkComparison
import com.example.rag.data.model.ChatMessage
import com.example.rag.data.model.ChunkingStrategy
import com.example.rag.data.model.Citation
import com.example.rag.data.model.CuratedQuery
import com.example.rag.data.model.DocumentChunk
import com.example.rag.data.model.DocumentInsights
import com.example.rag.data.model.DocumentItem
import com.example.rag.data.model.RetrievalMode
import com.example.rag.data.model.RetrievalResult
import com.example.rag.data.model.ToolCallRecord
import com.example.rag.engine.ChunkingEngine
import com.example.rag.engine.ConversationMemoryManager
import com.example.rag.engine.EmbeddingEngine
import com.example.rag.engine.GeminiRagService
import com.example.rag.engine.PdfTextExtractor
import com.example.rag.engine.PreloadedKnowledgeBase
import com.example.rag.engine.ToolManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RagRepository(
    private val ragDao: RagDao,
    val memoryManager: ConversationMemoryManager = ConversationMemoryManager(maxTurns = 5),
    private val toolManager: ToolManager = ToolManager()
) {
    val geminiService = GeminiRagService(memoryManager, toolManager)

    suspend fun initializeDatabaseIfEmpty(apiKey: String?) = withContext(Dispatchers.IO) {
        val count = ragDao.getDocumentCount()
        if (count == 0) {
            val entities = PreloadedKnowledgeBase.documents.map { doc ->
                DocumentEntity(
                    id = doc.id,
                    title = doc.title,
                    category = doc.category,
                    author = doc.author,
                    summary = doc.summary,
                    content = doc.content,
                    estimatedPages = doc.estimatedPages,
                    isCustom = doc.isCustom
                )
            }
            ragDao.insertDocuments(entities)

            // Index chunks for both strategies
            indexAllForStrategy(ChunkingStrategy.SENTENCE_AWARE, apiKey)
            indexAllForStrategy(ChunkingStrategy.FIXED_SIZE_WITH_OVERLAP, apiKey)
        }
    }

    fun getAllDocuments(): Flow<List<DocumentItem>> {
        return ragDao.getAllDocuments().map { entities ->
            entities.map { entity ->
                DocumentItem(
                    id = entity.id,
                    title = entity.title,
                    category = entity.category,
                    author = entity.author,
                    summary = entity.summary,
                    content = entity.content,
                    estimatedPages = entity.estimatedPages,
                    isCustom = entity.isCustom
                )
            }
        }
    }

    suspend fun getCustomDocumentsCount(): Int = withContext(Dispatchers.IO) {
        ragDao.getAllDocuments().firstOrNull()?.count { it.isCustom } ?: 0
    }

    suspend fun deleteDocument(docId: String) = withContext(Dispatchers.IO) {
        ragDao.deleteFtsChunksForDocument(docId)
        ragDao.deleteChunksForDocument(docId)
        ragDao.deleteDocument(docId)
    }

    suspend fun importPdfFromUri(
        context: Context,
        uri: Uri,
        apiKey: String?
    ): Result<DocumentItem> = withContext(Dispatchers.IO) {
        val currentCount = getCustomDocumentsCount()
        if (currentCount >= 20) {
            return@withContext Result.failure(IllegalStateException("Maximum limit of 20 uploaded PDFs reached."))
        }

        val extracted = PdfTextExtractor.extractFromUri(context, uri)
        val docId = "pdf_${System.currentTimeMillis()}_${(100..999).random()}"
        val doc = DocumentEntity(
            id = docId,
            title = extracted.title,
            category = "User PDF Upload",
            author = extracted.author,
            summary = extracted.text.take(200).replace("\n", " ").trim() + "...",
            content = extracted.text,
            estimatedPages = extracted.pageCount,
            isCustom = true
        )
        ragDao.insertDocument(doc)

        val docItem = DocumentItem(
            id = doc.id,
            title = doc.title,
            category = doc.category,
            author = doc.author,
            summary = doc.summary,
            content = doc.content,
            estimatedPages = doc.estimatedPages,
            isCustom = true
        )

        for (strategy in ChunkingStrategy.entries) {
            val chunks = ChunkingEngine.chunkDocument(docItem, strategy)
            val chunkEntities = chunks.map { chunk ->
                val embedding = EmbeddingEngine.getEmbedding(chunk.content, apiKey)
                ChunkEntity(
                    id = chunk.id,
                    docId = chunk.docId,
                    docTitle = chunk.docTitle,
                    chunkIndex = chunk.chunkIndex,
                    strategy = strategy.name,
                    content = chunk.content,
                    tokenCount = chunk.tokenCount,
                    embeddingCsv = EmbeddingEngine.serializeEmbedding(embedding)
                )
            }
            val ftsEntities = chunkEntities.map { c ->
                ChunkFtsEntity(
                    chunkId = c.id,
                    documentId = c.docId,
                    docTitle = c.docTitle,
                    strategy = c.strategy,
                    content = c.content
                )
            }
            ragDao.insertChunks(chunkEntities)
            ragDao.insertFtsChunks(ftsEntities)
        }

        Result.success(docItem)
    }

    suspend fun importSampleResearchPdf(
        title: String,
        category: String,
        author: String,
        pages: Int,
        content: String,
        apiKey: String?
    ): Result<DocumentItem> = withContext(Dispatchers.IO) {
        val currentCount = getCustomDocumentsCount()
        if (currentCount >= 20) {
            return@withContext Result.failure(IllegalStateException("Maximum limit of 20 uploaded PDFs reached."))
        }

        val pdfBytes = PdfTextExtractor.createSamplePdfBytes(
            title = title,
            category = category,
            author = author,
            pages = pages,
            content = content
        )
        val extracted = PdfTextExtractor.extractFromBytes(pdfBytes, title)

        val docId = "pdf_${System.currentTimeMillis()}_${(100..999).random()}"
        val doc = DocumentEntity(
            id = docId,
            title = extracted.title,
            category = category,
            author = extracted.author,
            summary = extracted.text.take(200).replace("\n", " ").trim() + "...",
            content = extracted.text,
            estimatedPages = extracted.pageCount,
            isCustom = true
        )
        ragDao.insertDocument(doc)

        val docItem = DocumentItem(
            id = doc.id,
            title = doc.title,
            category = doc.category,
            author = doc.author,
            summary = doc.summary,
            content = doc.content,
            estimatedPages = doc.estimatedPages,
            isCustom = true
        )

        for (strategy in ChunkingStrategy.entries) {
            val chunks = ChunkingEngine.chunkDocument(docItem, strategy)
            val chunkEntities = chunks.map { chunk ->
                val embedding = EmbeddingEngine.getEmbedding(chunk.content, apiKey)
                ChunkEntity(
                    id = chunk.id,
                    docId = chunk.docId,
                    docTitle = chunk.docTitle,
                    chunkIndex = chunk.chunkIndex,
                    strategy = strategy.name,
                    content = chunk.content,
                    tokenCount = chunk.tokenCount,
                    embeddingCsv = EmbeddingEngine.serializeEmbedding(embedding)
                )
            }
            val ftsEntities = chunkEntities.map { c ->
                ChunkFtsEntity(
                    chunkId = c.id,
                    documentId = c.docId,
                    docTitle = c.docTitle,
                    strategy = c.strategy,
                    content = c.content
                )
            }
            ragDao.insertChunks(chunkEntities)
            ragDao.insertFtsChunks(ftsEntities)
        }

        Result.success(docItem)
    }

    suspend fun addCustomDocument(
        title: String,
        category: String,
        author: String,
        content: String,
        apiKey: String?
    ) = withContext(Dispatchers.IO) {
        val docId = "custom_${System.currentTimeMillis()}"
        val doc = DocumentEntity(
            id = docId,
            title = title,
            category = category,
            author = author.ifBlank { "User Upload" },
            summary = content.take(150) + "...",
            content = content,
            estimatedPages = (content.length / 1500).coerceAtLeast(1),
            isCustom = true
        )
        ragDao.insertDocument(doc)

        // Chunk and embed for both strategies
        val docItem = DocumentItem(
            id = doc.id,
            title = doc.title,
            category = doc.category,
            author = doc.author,
            summary = doc.summary,
            content = doc.content,
            estimatedPages = doc.estimatedPages,
            isCustom = true
        )

        for (strategy in ChunkingStrategy.entries) {
            val chunks = ChunkingEngine.chunkDocument(docItem, strategy)
            val chunkEntities = chunks.map { chunk ->
                val embedding = EmbeddingEngine.getEmbedding(chunk.content, apiKey)
                ChunkEntity(
                    id = chunk.id,
                    docId = chunk.docId,
                    docTitle = chunk.docTitle,
                    chunkIndex = chunk.chunkIndex,
                    strategy = strategy.name,
                    content = chunk.content,
                    tokenCount = chunk.tokenCount,
                    embeddingCsv = EmbeddingEngine.serializeEmbedding(embedding)
                )
            }
            val ftsEntities = chunkEntities.map { c ->
                ChunkFtsEntity(
                    chunkId = c.id,
                    documentId = c.docId,
                    docTitle = c.docTitle,
                    strategy = c.strategy,
                    content = c.content
                )
            }
            ragDao.insertChunks(chunkEntities)
            ragDao.insertFtsChunks(ftsEntities)
        }
    }

    suspend fun indexAllForStrategy(strategy: ChunkingStrategy, apiKey: String?) = withContext(Dispatchers.IO) {
        val documents = ragDao.getAllDocuments().firstOrNull() ?: emptyList()
        ragDao.deleteChunksForStrategy(strategy.name)
        ragDao.deleteFtsChunksForStrategy(strategy.name)

        val allChunkEntities = mutableListOf<ChunkEntity>()
        for (doc in documents) {
            val docItem = DocumentItem(
                id = doc.id,
                title = doc.title,
                category = doc.category,
                author = doc.author,
                summary = doc.summary,
                content = doc.content,
                estimatedPages = doc.estimatedPages,
                isCustom = doc.isCustom
            )
            val chunks = ChunkingEngine.chunkDocument(docItem, strategy)
            chunks.forEach { chunk ->
                val embedding = EmbeddingEngine.getEmbedding(chunk.content, apiKey)
                allChunkEntities.add(
                    ChunkEntity(
                        id = chunk.id,
                        docId = chunk.docId,
                        docTitle = chunk.docTitle,
                        chunkIndex = chunk.chunkIndex,
                        strategy = strategy.name,
                        content = chunk.content,
                        tokenCount = chunk.tokenCount,
                        embeddingCsv = EmbeddingEngine.serializeEmbedding(embedding)
                    )
                )
            }
        }
        ragDao.insertChunks(allChunkEntities)
        val ftsEntities = allChunkEntities.map { c ->
            ChunkFtsEntity(
                chunkId = c.id,
                documentId = c.docId,
                docTitle = c.docTitle,
                strategy = c.strategy,
                content = c.content
            )
        }
        ragDao.insertFtsChunks(ftsEntities)
    }

    fun getChunksForStrategy(strategy: ChunkingStrategy): Flow<List<DocumentChunk>> {
        return ragDao.getChunksForStrategy(strategy.name).map { entities ->
            entities.map { e ->
                DocumentChunk(
                    id = e.id,
                    docId = e.docId,
                    docTitle = e.docTitle,
                    chunkIndex = e.chunkIndex,
                    strategy = strategy,
                    content = e.content,
                    tokenCount = e.tokenCount,
                    embedding = EmbeddingEngine.deserializeEmbedding(e.embeddingCsv)
                )
            }
        }
    }

    /**
     * Semantic Top-K Vector Retrieval with Cosine Similarity (Dense)
     */
    suspend fun retrieveTopK(
        query: String,
        strategy: ChunkingStrategy,
        topK: Int = 4,
        apiKey: String?
    ): List<RetrievalResult> = withContext(Dispatchers.IO) {
        val queryEmbedding = EmbeddingEngine.getEmbedding(query, apiKey)
        val chunkEntities = ragDao.getChunksForStrategySync(strategy.name)

        val scoredResults = chunkEntities.mapNotNull { chunkEntity ->
            val chunkVec = EmbeddingEngine.deserializeEmbedding(chunkEntity.embeddingCsv)
            if (chunkVec.isEmpty()) return@mapNotNull null
            val score = EmbeddingEngine.cosineSimilarity(queryEmbedding, chunkVec)
            val chunk = DocumentChunk(
                id = chunkEntity.id,
                docId = chunkEntity.docId,
                docTitle = chunkEntity.docTitle,
                chunkIndex = chunkEntity.chunkIndex,
                strategy = strategy,
                content = chunkEntity.content,
                tokenCount = chunkEntity.tokenCount,
                embedding = chunkVec
            )
            Pair(chunk, score)
        }

        scoredResults
            .sortedByDescending { it.second }
            .take(topK)
            .mapIndexed { index, pair ->
                RetrievalResult(
                    chunk = pair.first,
                    score = pair.second,
                    rank = index + 1,
                    vectorScore = pair.second,
                    ftsScore = 0f,
                    retrievalMode = RetrievalMode.VECTOR
                )
            }
    }

    /**
     * SQLite FTS4 Inverted-Index Full-Text Search (Sparse BM25-like)
     */
    suspend fun searchChunksFts(
        query: String,
        strategy: ChunkingStrategy,
        topK: Int = 4
    ): List<RetrievalResult> = withContext(Dispatchers.IO) {
        val cleanTokens = query.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length >= 2 }
        val ftsQuery = if (cleanTokens.isNotEmpty()) {
            cleanTokens.joinToString(" OR ") { "$it*" }
        } else {
            "\"$query\""
        }

        val matchedEntities = try {
            ragDao.searchChunksFts(ftsQuery, strategy.name)
        } catch (e: Exception) {
            try {
                if (cleanTokens.isNotEmpty()) {
                    ragDao.searchChunksFts(cleanTokens.first() + "*", strategy.name)
                } else emptyList()
            } catch (e2: Exception) {
                emptyList()
            }
        }

        if (matchedEntities.isEmpty()) return@withContext emptyList()

        val queryTerms = query.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 2 }
        val scored = matchedEntities.map { entity ->
            val contentLower = entity.content.lowercase()
            val titleLower = entity.docTitle.lowercase()
            var matches = 0
            for (term in queryTerms) {
                if (titleLower.contains(term)) matches += 3
                var idx = 0
                while (idx != -1) {
                    idx = contentLower.indexOf(term, idx)
                    if (idx != -1) {
                        matches++
                        idx += term.length
                    }
                }
            }
            val ftsScore = (1f - (1f / (1f + matches * 0.15f))).coerceIn(0.15f, 0.98f)
            val chunk = DocumentChunk(
                id = entity.id,
                docId = entity.docId,
                docTitle = entity.docTitle,
                chunkIndex = entity.chunkIndex,
                strategy = strategy,
                content = entity.content,
                tokenCount = entity.tokenCount,
                embedding = EmbeddingEngine.deserializeEmbedding(entity.embeddingCsv)
            )
            Pair(chunk, ftsScore)
        }

        scored.sortedByDescending { it.second }
            .take(topK)
            .mapIndexed { index, (chunk, score) ->
                RetrievalResult(
                    chunk = chunk,
                    score = score,
                    rank = index + 1,
                    vectorScore = 0f,
                    ftsScore = score,
                    retrievalMode = RetrievalMode.FTS4
                )
            }
    }

    /**
     * State-of-the-Art Hybrid Retrieval:
     * Dense Vector Search (Semantic Cosine) + SQLite FTS4 Inverted Index (BM25 Sparse)
     * merged using Reciprocal Rank Fusion (RRF).
     */
    suspend fun retrieveHybrid(
        query: String,
        strategy: ChunkingStrategy,
        topK: Int = 4,
        apiKey: String?
    ): List<RetrievalResult> = withContext(Dispatchers.IO) {
        val candidateK = (topK * 2).coerceAtLeast(6)
        val vectorCandidates = retrieveTopK(query, strategy, candidateK, apiKey)
        val ftsCandidates = searchChunksFts(query, strategy, candidateK)

        val candidateChunks = mutableMapOf<String, DocumentChunk>()
        val vectorRanks = mutableMapOf<String, Int>()
        val vectorScores = mutableMapOf<String, Float>()
        val ftsRanks = mutableMapOf<String, Int>()
        val ftsScores = mutableMapOf<String, Float>()

        vectorCandidates.forEach { r ->
            candidateChunks[r.chunk.id] = r.chunk
            vectorRanks[r.chunk.id] = r.rank
            vectorScores[r.chunk.id] = r.score
        }

        ftsCandidates.forEach { r ->
            candidateChunks[r.chunk.id] = r.chunk
            ftsRanks[r.chunk.id] = r.rank
            ftsScores[r.chunk.id] = r.score
        }

        val kRrf = 60.0
        val rrfMaxScore = (2.0 / (kRrf + 1.0)).toFloat()

        val fused = candidateChunks.keys.map { id ->
            val vRank = vectorRanks[id]
            val fRank = ftsRanks[id]

            val vRrf = if (vRank != null) 1.0 / (kRrf + vRank) else 0.0
            val fRrf = if (fRank != null) 1.0 / (kRrf + fRank) else 0.0

            val combined = (vRrf + fRrf).toFloat()
            val normalized = ((combined / rrfMaxScore) * 0.95f).coerceIn(0.2f, 0.99f)

            Triple(
                id,
                normalized,
                Pair(vectorScores[id] ?: 0f, ftsScores[id] ?: 0f)
            )
        }

        fused.sortedByDescending { it.second }
            .take(topK)
            .mapIndexed { index, (id, score, subScores) ->
                RetrievalResult(
                    chunk = candidateChunks[id]!!,
                    score = score,
                    rank = index + 1,
                    vectorScore = subScores.first,
                    ftsScore = subScores.second,
                    retrievalMode = RetrievalMode.HYBRID
                )
            }
    }

    /**
     * Unified RAG Retrieval Entry Point
     */
    suspend fun retrieveChunks(
        query: String,
        strategy: ChunkingStrategy,
        mode: RetrievalMode,
        topK: Int = 4,
        apiKey: String?
    ): List<RetrievalResult> = when (mode) {
        RetrievalMode.HYBRID -> retrieveHybrid(query, strategy, topK, apiKey)
        RetrievalMode.VECTOR -> retrieveTopK(query, strategy, topK, apiKey)
        RetrievalMode.FTS4 -> {
            val fts = searchChunksFts(query, strategy, topK)
            if (fts.isNotEmpty()) fts else retrieveTopK(query, strategy, topK, apiKey)
        }
    }

    /**
     * Free AI Feature: Instant on-device executive insights, key takeaways,
     * core technical concepts, and suggested research questions for any document.
     */
    suspend fun generateDocumentInsights(docId: String): DocumentInsights = withContext(Dispatchers.IO) {
        val doc = ragDao.getDocumentById(docId)
            ?: return@withContext DocumentInsights(
                docId = docId,
                executiveSummary = "Document not found.",
                keyTakeaways = emptyList(),
                technicalConcepts = emptyList(),
                suggestedQuestions = emptyList()
            )

        val paragraphs = doc.content.split(Regex("\n\n+")).filter { it.isNotBlank() && !it.startsWith("#") }
        val executiveSummary = paragraphs.firstOrNull() ?: doc.summary

        val sentences = doc.content.split(Regex("(?<=[.!?])\\s+"))
            .filter { s -> s.length in 35..250 && !s.startsWith("#") }

        val keyTakeaways = sentences
            .filter { s ->
                s.contains("key", ignoreCase = true) ||
                s.contains("significant", ignoreCase = true) ||
                s.contains("allows", ignoreCase = true) ||
                s.contains("enables", ignoreCase = true) ||
                s.contains("achieves", ignoreCase = true) ||
                s.contains("critical", ignoreCase = true) ||
                s.contains("fundamental", ignoreCase = true) ||
                s.contains("architecture", ignoreCase = true) ||
                s.contains("efficiency", ignoreCase = true) ||
                s.contains("quantum", ignoreCase = true) ||
                s.contains("attention", ignoreCase = true)
            }.distinct().take(4).ifEmpty {
                sentences.take(3)
            }

        val technicalConcepts = doc.content.split(Regex("[^a-zA-Z0-9_-]+"))
            .filter { it.length > 5 && it[0].isUpperCase() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(6)
            .map { it.first }

        val suggestedQuestions = listOf(
            "How does ${doc.title.take(35)} compare to classical baseline methods?",
            "What are the main engineering tradeoffs described in ${doc.title.take(35)}?",
            "Explain the mathematical or architectural mechanism in ${doc.title.take(35)}."
        )

        DocumentInsights(
            docId = docId,
            executiveSummary = executiveSummary,
            keyTakeaways = keyTakeaways,
            technicalConcepts = technicalConcepts,
            suggestedQuestions = suggestedQuestions
        )
    }

    /**
     * Benchmark and compare retrieval efficiency between Sentence-Aware and Fixed-Size Chunking
     * AND Vector vs FTS4 vs Hybrid.
     */
    suspend fun compareChunkingStrategies(
        query: String,
        topK: Int = 3,
        apiKey: String?
    ): BenchmarkComparison = withContext(Dispatchers.IO) {
        val sentenceResults = retrieveTopK(query, ChunkingStrategy.SENTENCE_AWARE, topK, apiKey)
        val fixedResults = retrieveTopK(query, ChunkingStrategy.FIXED_SIZE_WITH_OVERLAP, topK, apiKey)

        val ftsResults = searchChunksFts(query, ChunkingStrategy.SENTENCE_AWARE, topK)
        val hybridResults = retrieveHybrid(query, ChunkingStrategy.SENTENCE_AWARE, topK, apiKey)

        val sentAvg = if (sentenceResults.isNotEmpty()) sentenceResults.map { it.score }.average().toFloat() else 0f
        val fixAvg = if (fixedResults.isNotEmpty()) fixedResults.map { it.score }.average().toFloat() else 0f
        val ftsAvg = if (ftsResults.isNotEmpty()) ftsResults.map { it.score }.average().toFloat() else 0f
        val hybridAvg = if (hybridResults.isNotEmpty()) hybridResults.map { it.score }.average().toFloat() else 0f

        val verdict = when {
            sentAvg > fixAvg + 0.03f -> "Sentence-Aware outperformed Fixed-Size (+${"%.1f".format((sentAvg - fixAvg) * 100)}% score). Semantic boundaries preserved cohesive context."
            fixAvg > sentAvg + 0.03f -> "Fixed-Size with Overlap scored slightly higher (+${"%.1f".format((fixAvg - sentAvg) * 100)}% score) due to uniform token window density."
            else -> "Comparable retrieval performance (diff < 3%). Sentence-Aware provides cleaner citation boundaries."
        }

        BenchmarkComparison(
            query = query,
            sentenceResults = sentenceResults,
            fixedResults = fixedResults,
            sentenceAvgScore = sentAvg,
            fixedAvgScore = fixAvg,
            evaluationVerdict = verdict,
            vectorAvgScore = sentAvg,
            ftsAvgScore = ftsAvg,
            hybridAvgScore = hybridAvg
        )
    }

    /**
     * Chat Message Persistence and Conversion
     */
    fun getChatMessages(): Flow<List<ChatMessage>> {
        return ragDao.getAllChatMessages().map { list ->
            list.map { entity ->
                ChatMessage(
                    id = entity.id,
                    role = entity.role,
                    text = entity.text,
                    citations = parseCitations(entity.citationsJson),
                    toolCalls = parseToolCalls(entity.toolCallsJson),
                    timestamp = entity.timestamp,
                    strategyUsed = try {
                        ChunkingStrategy.valueOf(entity.strategyUsed)
                    } catch (e: Exception) {
                        ChunkingStrategy.SENTENCE_AWARE
                    },
                    modelName = entity.modelName,
                    latencyMs = entity.latencyMs,
                    isQuotaFallback = entity.isQuotaFallback
                )
            }
        }
    }

    suspend fun saveChatMessage(msg: ChatMessage) = withContext(Dispatchers.IO) {
        val entity = ChatMessageEntity(
            id = msg.id,
            role = msg.role,
            text = msg.text,
            citationsJson = serializeCitations(msg.citations),
            toolCallsJson = serializeToolCalls(msg.toolCalls),
            strategyUsed = msg.strategyUsed.name,
            modelName = msg.modelName,
            latencyMs = msg.latencyMs,
            isQuotaFallback = msg.isQuotaFallback,
            timestamp = msg.timestamp
        )
        ragDao.insertChatMessage(entity)
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        ragDao.clearChatHistory()
        memoryManager.clearMemory()
    }

    // Ready-made queries generator
    fun getCuratedQueries(): List<CuratedQuery> {
        return PreloadedKnowledgeBase.curatedQueries
    }

    private fun serializeCitations(citations: List<Citation>): String {
        val arr = JSONArray()
        citations.forEach { c ->
            arr.put(JSONObject().apply {
                put("docId", c.docId)
                put("docTitle", c.docTitle)
                put("category", c.category)
                put("chunkIndex", c.chunkIndex)
                put("snippet", c.snippet)
                put("score", c.similarityScore.toDouble())
            })
        }
        return arr.toString()
    }

    private fun parseCitations(jsonStr: String): List<Citation> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<Citation>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Citation(
                        docId = obj.optString("docId"),
                        docTitle = obj.optString("docTitle"),
                        category = obj.optString("category"),
                        chunkIndex = obj.optInt("chunkIndex"),
                        snippet = obj.optString("snippet"),
                        similarityScore = obj.optDouble("score").toFloat()
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parsing error
        }
        return list
    }

    private fun serializeToolCalls(tools: List<ToolCallRecord>): String {
        val arr = JSONArray()
        tools.forEach { t ->
            arr.put(JSONObject().apply {
                put("toolName", t.toolName)
                put("input", t.input)
                put("output", t.output)
                put("timestamp", t.timestamp)
            })
        }
        return arr.toString()
    }

    private fun parseToolCalls(jsonStr: String): List<ToolCallRecord> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<ToolCallRecord>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ToolCallRecord(
                        toolName = obj.optString("toolName"),
                        input = obj.optString("input"),
                        output = obj.optString("output"),
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
        return list
    }
}
