package com.example.rag.engine

import com.example.rag.data.model.ChunkingStrategy
import com.example.rag.data.model.DocumentChunk
import com.example.rag.data.model.DocumentItem
import java.util.UUID

object ChunkingEngine {

    private const val FIXED_CHUNK_TOKEN_SIZE = 200
    private const val FIXED_CHUNK_TOKEN_OVERLAP = 40
    private const val SENTENCE_TARGET_TOKEN_SIZE = 160

    fun chunkDocument(document: DocumentItem, strategy: ChunkingStrategy): List<DocumentChunk> {
        return chunkText(
            text = document.content,
            docId = document.id,
            docTitle = document.title,
            strategy = strategy
        )
    }

    fun chunkText(
        text: String,
        docId: String,
        docTitle: String,
        strategy: ChunkingStrategy
    ): List<DocumentChunk> {
        return when (strategy) {
            ChunkingStrategy.SENTENCE_AWARE -> chunkSentenceAware(text, docId, docTitle)
            ChunkingStrategy.FIXED_SIZE_WITH_OVERLAP -> chunkFixedSizeWithOverlap(text, docId, docTitle)
        }
    }

    /**
     * Sentence-Aware Chunking:
     * Splits into discrete grammatical sentences and section headers, then bundles them
     * into cohesive semantic paragraphs without breaking mid-sentence.
     */
    private fun chunkSentenceAware(text: String, docId: String, docTitle: String): List<DocumentChunk> {
        val sentences = extractSentences(text)
        if (sentences.isEmpty()) return emptyList()

        val chunks = mutableListOf<DocumentChunk>()
        var currentChunkSentences = mutableListOf<String>()
        var currentTokenCount = 0
        var chunkIndex = 0

        for (sentence in sentences) {
            val sentenceTokens = estimateTokenCount(sentence)
            if (currentTokenCount + sentenceTokens > SENTENCE_TARGET_TOKEN_SIZE && currentChunkSentences.isNotEmpty()) {
                val chunkContent = currentChunkSentences.joinToString(" ").trim()
                chunks.add(
                    DocumentChunk(
                        id = "${docId}_sa_$chunkIndex",
                        docId = docId,
                        docTitle = docTitle,
                        chunkIndex = chunkIndex,
                        strategy = ChunkingStrategy.SENTENCE_AWARE,
                        content = chunkContent,
                        tokenCount = estimateTokenCount(chunkContent)
                    )
                )
                chunkIndex++
                currentChunkSentences = mutableListOf()
                currentTokenCount = 0
            }
            currentChunkSentences.add(sentence)
            currentTokenCount += sentenceTokens
        }

        if (currentChunkSentences.isNotEmpty()) {
            val chunkContent = currentChunkSentences.joinToString(" ").trim()
            chunks.add(
                DocumentChunk(
                    id = "${docId}_sa_$chunkIndex",
                    docId = docId,
                    docTitle = docTitle,
                    chunkIndex = chunkIndex,
                    strategy = ChunkingStrategy.SENTENCE_AWARE,
                    content = chunkContent,
                    tokenCount = estimateTokenCount(chunkContent)
                )
            )
        }

        return chunks
    }

    /**
     * Fixed-Size Chunking with Overlap:
     * Splits words/tokens strictly into fixed windows (200 tokens) with 40-token overlap stride.
     */
    private fun chunkFixedSizeWithOverlap(text: String, docId: String, docTitle: String): List<DocumentChunk> {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val chunks = mutableListOf<DocumentChunk>()
        val stride = (FIXED_CHUNK_TOKEN_SIZE - FIXED_CHUNK_TOKEN_OVERLAP).coerceAtLeast(1)
        var chunkIndex = 0
        var start = 0

        while (start < words.size) {
            val end = (start + FIXED_CHUNK_TOKEN_SIZE).coerceAtMost(words.size)
            val chunkWords = words.subList(start, end)
            val chunkContent = chunkWords.joinToString(" ")

            chunks.add(
                DocumentChunk(
                    id = "${docId}_fx_$chunkIndex",
                    docId = docId,
                    docTitle = docTitle,
                    chunkIndex = chunkIndex,
                    strategy = ChunkingStrategy.FIXED_SIZE_WITH_OVERLAP,
                    content = chunkContent,
                    tokenCount = chunkWords.size
                )
            )
            chunkIndex++

            if (end >= words.size) break
            start += stride
        }

        return chunks
    }

    private fun extractSentences(text: String): List<String> {
        val lines = text.lines()
        val sentences = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Markdown headers are treated as distinct anchor units
            if (trimmed.startsWith("#")) {
                sentences.add(trimmed)
                continue
            }

            // Split on sentence boundaries: period, exclamation, question mark followed by space or end of string
            val rawSplit = trimmed.split(Regex("(?<=[.!?])\\s+"))
            for (s in rawSplit) {
                val clean = s.trim()
                if (clean.isNotEmpty()) {
                    sentences.add(clean)
                }
            }
        }
        return sentences
    }

    fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        // Rule of thumb: ~0.75 words per token or ~1.3 tokens per word
        return (words.size * 1.25).toInt().coerceAtLeast(words.size)
    }
}
