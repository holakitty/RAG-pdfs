# RAG AI Pipeline & Research Assistant (Android)

A production-grade mobile **Retrieval-Augmented Generation (RAG)** application built natively with **Kotlin**, **Jetpack Compose**, **Room SQLite (with FTS4 & Vector Embeddings)**, and **Google Gemini API**.

---

## 📱 About This Project

This is a **native Android application** developed in Google AI Studio. 

Unlike a standard website, this project is packaged as an **Android APK / AAB (Android App Bundle)** designed to run on Android devices (phones, foldables, tablets) or in the Google AI Studio streaming emulator.

---

## ✨ Key Features

1. **Dual Search Engine (Dense + Sparse Retrieval)**:
   - **Vector Semantic Search**: 64-dimensional normalized vector embeddings with cosine similarity.
   - **SQLite FTS4 Full-Text Search**: Inverted index keyword search running locally on device via SQLite FTS4 virtual tables.
   - **Hybrid RRF Search**: Reciprocal Rank Fusion ($k = 60$) combining vector and keyword ranks for state-of-the-art retrieval accuracy.

2. **Custom PDF Ingestion (Up to 20 PDFs)**:
   - Upload up to 20 custom technical PDF documents directly on-device.
   - Automatic text extraction, sentence-aware chunking, token counting, and vector embedding calculation.
   - Instant indexing into Room database and FTS4 tables.

3. **Pre-Data Curated Q&A Hub**:
   - 12 comprehensive, authoritative answers to deep technical queries across physics, AI, aerospace, cybersecurity, and genomics.
   - All Q&A answers are formatted in fluent, multi-sentence paragraphs completely free of asterisks (`*`) or markdown bolding.
   - Real-time clickable citations linking back to source document chunks.

4. **Offline Local RAG Synthesis Fallback**:
   - If an API key is not supplied or free quota is reached, an on-device local RAG synthesis engine generates contextual, multi-paragraph syntheses with bracketed source citations.

5. **Conversational Memory**:
   - Retains the last 5 conversational turns to answer follow-up queries with full dialogue continuity.

---

## 🚀 How to Run & Build

### Option 1: Run in Google AI Studio
- Open your project in Google AI Studio.
- The app runs live inside the built-in streaming Android emulator directly in your browser.

### Option 2: Open in Android Studio
1. Clone this repository:
   ```bash
   git clone <your-repo-url>
   ```
2. Open Android Studio (`File > Open...`) and select the project directory.
3. Allow Gradle to sync.
4. Run on any Android device or virtual device running Android 8.0 (API 26) or higher.

### Option 3: Build APK / Android App Bundle (AAB)
To generate an installable APK for your phone:
```bash
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

To generate a release Android App Bundle for Google Play Console:
```bash
./gradlew bundleRelease
```

---

## 📂 Project Architecture

```
app/src/main/java/com/example/rag/
├── data/
│   ├── db/                 # Room Database, DAOs, Entity tables (FTS4 + Chunks)
│   ├── model/              # Domain models (Document, Chunk, Citation, CuratedQuery)
│   └── repository/         # RagRepository (Hybrid search, RRF fusion, PDF ingest)
├── engine/
│   ├── ChunkingEngine.kt   # Sentence-aware & fixed-size chunking
│   ├── EmbeddingEngine.kt  # 64-dim semantic embeddings & cosine similarity
│   ├── GeminiRagService.kt # Gemini API + On-device Paragraph Synthesis
│   ├── PdfTextExtractor.kt # PDF parsing and sample generation
│   └── PreloadedKnowledgeBase.kt # Curated papers & asterisk-free Q&A
└── ui/
    ├── screens/            # ChatScreen, DocumentsScreen, QueriesScreen, BenchmarkScreen
    └── components/         # CitationsRow, ModeSelector, Dialogs
```
