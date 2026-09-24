package com.example.rag.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val author: String,
    val summary: String,
    val content: String,
    val estimatedPages: Int,
    val isCustom: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chunks")
data class ChunkEntity(
    @PrimaryKey val id: String,
    val docId: String,
    val docTitle: String,
    val chunkIndex: Int,
    val strategy: String, // "SENTENCE_AWARE" or "FIXED_SIZE_WITH_OVERLAP"
    val content: String,
    val tokenCount: Int,
    val embeddingCsv: String // comma separated floats
)

/**
 * Room FTS4 virtual table for high-performance full-text search and BM25 ranking
 * over text chunks, serving as the sparse retrieval engine of the RAG pipeline.
 *
 * Note: 'docid' is an SQLite FTS4 internal reserved identifier, so 'documentId'
 * is used to reference the parent document ID.
 */
@Fts4
@Entity(tableName = "chunks_fts")
data class ChunkFtsEntity(
    val chunkId: String,
    val documentId: String,
    val docTitle: String,
    val strategy: String,
    val content: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val text: String,
    val citationsJson: String,
    val toolCallsJson: String,
    val strategyUsed: String,
    val modelName: String,
    val latencyMs: Long,
    val isQuotaFallback: Boolean,
    val timestamp: Long
)
