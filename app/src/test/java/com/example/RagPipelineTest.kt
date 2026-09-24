package com.example

import com.example.rag.data.model.ChunkingStrategy
import com.example.rag.data.model.DocumentItem
import com.example.rag.engine.ChunkingEngine
import com.example.rag.engine.ConversationMemoryManager
import com.example.rag.engine.EmbeddingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RagPipelineTest {

    @Test
    fun testSentenceAwareChunking() {
        val sampleText = """
            Quantum computing harnesses quantum mechanical phenomena. Superposition allows qubits to exist in multiple states simultaneously. Entanglement links quantum states across arbitrary distances. Peter Shor formulated a polynomial time algorithm for integer factorization.
        """.trimIndent()

        val doc = DocumentItem(
            id = "test_doc",
            title = "Test Quantum",
            category = "Physics",
            author = "Tester",
            summary = "Summary",
            content = sampleText
        )

        val chunks = ChunkingEngine.chunkDocument(doc, ChunkingStrategy.SENTENCE_AWARE)
        assertTrue(chunks.isNotEmpty())
        assertEquals("test_doc", chunks.first().docId)
        assertEquals(ChunkingStrategy.SENTENCE_AWARE, chunks.first().strategy)
    }

    @Test
    fun testFixedSizeChunkingWithOverlap() {
        // Create 300 words
        val words = (1..300).map { "token$it" }.joinToString(" ")
        val doc = DocumentItem(
            id = "test_doc_fixed",
            title = "Test Fixed",
            category = "Math",
            author = "Tester",
            summary = "Summary",
            content = words
        )

        val chunks = ChunkingEngine.chunkDocument(doc, ChunkingStrategy.FIXED_SIZE_WITH_OVERLAP)
        // With 300 words, window 200, stride 160: chunk 0 is 0..200, chunk 1 is 160..300 -> 2 chunks
        assertEquals(2, chunks.size)
        assertEquals(ChunkingStrategy.FIXED_SIZE_WITH_OVERLAP, chunks.first().strategy)
    }

    @Test
    fun testCosineSimilarityIdenticalAndOrthogonal() {
        val vec1 = EmbeddingEngine.computeLocalSemanticVector("quantum computing superposition qubits")
        val vec2 = EmbeddingEngine.computeLocalSemanticVector("quantum computing superposition qubits")
        val simIdentical = EmbeddingEngine.cosineSimilarity(vec1, vec2)
        assertTrue("Identical texts should have similarity near 1.0", simIdentical > 0.99f)

        val vecUnrelated = EmbeddingEngine.computeLocalSemanticVector("cooking italian pasta tomato basil recipe")
        val simDifferent = EmbeddingEngine.cosineSimilarity(vec1, vecUnrelated)
        assertTrue("Different texts should have lower similarity than identical", simDifferent < simIdentical)
    }

    @Test
    fun testConversationalMemoryLast5Turns() {
        val memory = ConversationMemoryManager(maxTurns = 5)
        for (i in 1..8) {
            memory.addTurn("User Question $i", "Assistant Response $i")
        }

        val turns = memory.getRecentTurns()
        assertEquals(5, turns.size)
        // The earliest turn kept should be turn 4
        assertEquals("User Question 4", turns.first().userQuery)
        assertEquals("User Question 8", turns.last().userQuery)
    }

    @Test
    fun testReciprocalRankFusionScore() {
        val kRrf = 60.0
        val vRank = 1
        val fRank = 1
        val vComponent = 1.0 / (kRrf + vRank)
        val fComponent = 1.0 / (kRrf + fRank)
        val rrfScore = (vComponent + fComponent).toFloat()
        assertTrue("RRF score for rank 1 should be positive", rrfScore > 0f)

        // Rank 1 in both should score strictly higher than Rank 5 in both
        val vComp5 = 1.0 / (kRrf + 5)
        val fComp5 = 1.0 / (kRrf + 5)
        val rrfScore5 = (vComp5 + fComp5).toFloat()
        assertTrue("Higher ranks should have higher RRF scores", rrfScore > rrfScore5)
    }

    @Test
    fun testFtsQueryTokenization() {
        val rawQuery = "Quantum Attention & Shor's algorithm"
        val tokens = rawQuery.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length >= 2 }
            .map { "$it*" }
        val ftsQuery = tokens.joinToString(" OR ")
        assertTrue(ftsQuery.contains("Quantum*"))
        assertTrue(ftsQuery.contains("Attention*"))
        assertTrue(ftsQuery.contains("Shor*"))
        assertTrue(ftsQuery.contains("algorithm*"))
    }

    @Test
    fun testCuratedQueriesAreFreeOfAsterisksAndInParagraphs() {
        val queries = com.example.rag.engine.PreloadedKnowledgeBase.curatedQueries
        assertTrue("Curated queries should not be empty", queries.isNotEmpty())

        for (q in queries) {
            assertTrue("Query '${q.id}' query text must not contain asterisks", !q.query.contains("*"))
            assertTrue("Query '${q.id}' readyAnswer must not be blank", q.readyAnswer.isNotBlank())
            assertTrue("Query '${q.id}' readyAnswer must not contain any asterisks (*)", !q.readyAnswer.contains("*"))
            assertTrue("Query '${q.id}' readyAnswer should be structured in paragraphs or sentences", q.readyAnswer.length > 50)
            assertTrue("Query '${q.id}' key citations must not be empty", q.keyCitations.isNotEmpty())
        }
    }

    @Test
    fun testSanitizeTextFreeOfAsterisks() {
        val rawInput = """
            ### 1. Quantum Computing Foundations
            **Superposition** is a **fundamental** principle:
            * Bullet item one with *italics*
            * Bullet item two with **bold emphasis**
            
            This is a concluding paragraph without asterisks.
        """.trimIndent()

        val sanitized = com.example.rag.engine.GeminiRagService.sanitizeTextFreeOfAsterisks(rawInput)

        assertTrue("Sanitized text must not contain any asterisks", !sanitized.contains("*"))
        assertTrue("Sanitized text must not contain markdown heading hashes", !sanitized.contains("###"))
        assertTrue("Sanitized text should contain the cleaned text", sanitized.contains("Superposition is a fundamental principle"))
        assertTrue("Sanitized text should contain concluding paragraph", sanitized.contains("This is a concluding paragraph without asterisks."))
    }

    @Test
    fun testPdfTextExtractorSampleCreationAndParsing() {
        val samplePdfBytes = com.example.rag.engine.PdfTextExtractor.createSamplePdfBytes(
            title = "Autonomous Drone Fleet Telemetry.pdf",
            category = "Robotics & Swarms",
            author = "Dr. Swarm",
            pages = 3,
            content = "Autonomous UAV swarms maintain flocking consensus and avoid obstacles using potential fields."
        )

        assertTrue("Generated PDF bytes must not be empty", samplePdfBytes.isNotEmpty())
        val extracted = com.example.rag.engine.PdfTextExtractor.extractFromBytes(samplePdfBytes, "Autonomous Drone Fleet Telemetry.pdf")
        assertTrue("Extracted title must match", extracted.title.contains("Autonomous Drone Fleet Telemetry"))
        assertTrue("Extracted text must contain key content", extracted.text.contains("Autonomous UAV swarms"))
    }
}

