package com.example.rag.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RagDao {

    @Query("SELECT * FROM documents ORDER BY createdAt ASC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<DocumentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("SELECT * FROM chunks WHERE strategy = :strategy")
    fun getChunksForStrategy(strategy: String): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks WHERE strategy = :strategy")
    suspend fun getChunksForStrategySync(strategy: String): List<ChunkEntity>

    @Query("SELECT * FROM chunks WHERE docId = :docId")
    fun getChunksByDocument(docId: String): Flow<List<ChunkEntity>>

    @Query("SELECT COUNT(*) FROM chunks WHERE strategy = :strategy")
    suspend fun getChunkCountForStrategy(strategy: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<ChunkEntity>)

    @Query("DELETE FROM chunks WHERE docId = :docId")
    suspend fun deleteChunksForDocument(docId: String)

    @Query("DELETE FROM chunks WHERE strategy = :strategy")
    suspend fun deleteChunksForStrategy(strategy: String)

    @Query("DELETE FROM chunks")
    suspend fun deleteAllChunks()

    // FTS4 Sparse Full-Text Search Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFtsChunks(ftsChunks: List<ChunkFtsEntity>)

    @Query("""
        SELECT chunks.id, chunks.docId, chunks.docTitle, chunks.chunkIndex, chunks.strategy, chunks.content, chunks.tokenCount, chunks.embeddingCsv
        FROM chunks
        JOIN chunks_fts ON chunks.id = chunks_fts.chunkId
        WHERE chunks_fts.content MATCH :searchQuery AND chunks.strategy = :strategy
    """)
    suspend fun searchChunksFts(searchQuery: String, strategy: String): List<ChunkEntity>

    @Query("""
        SELECT chunks.id, chunks.docId, chunks.docTitle, chunks.chunkIndex, chunks.strategy, chunks.content, chunks.tokenCount, chunks.embeddingCsv
        FROM chunks
        JOIN chunks_fts ON chunks.id = chunks_fts.chunkId
        WHERE chunks_fts.content MATCH :searchQuery
    """)
    suspend fun searchAllChunksFts(searchQuery: String): List<ChunkEntity>

    @Query("DELETE FROM chunks_fts WHERE documentId = :docId")
    suspend fun deleteFtsChunksForDocument(docId: String)

    @Query("DELETE FROM chunks_fts WHERE strategy = :strategy")
    suspend fun deleteFtsChunksForStrategy(strategy: String)

    @Query("DELETE FROM chunks_fts")
    suspend fun deleteAllFtsChunks()

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentChatMessagesSync(limit: Int): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()
}
