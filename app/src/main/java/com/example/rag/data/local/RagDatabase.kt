package com.example.rag.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocumentEntity::class,
        ChunkEntity::class,
        ChatMessageEntity::class,
        ChunkFtsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RagDatabase : RoomDatabase() {
    abstract fun ragDao(): RagDao

    companion object {
        @Volatile
        private var INSTANCE: RagDatabase? = null

        fun getInstance(context: Context): RagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RagDatabase::class.java,
                    "rag_ai_pipeline.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
