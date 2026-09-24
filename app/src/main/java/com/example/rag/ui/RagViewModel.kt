package com.example.rag.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rag.data.local.RagDatabase
import com.example.rag.data.model.BenchmarkComparison
import com.example.rag.data.model.ChatMessage
import com.example.rag.data.model.ChunkingStrategy
import com.example.rag.data.model.Citation
import com.example.rag.data.model.CuratedQuery
import com.example.rag.data.model.DocumentChunk
import com.example.rag.data.model.DocumentInsights
import com.example.rag.data.model.DocumentItem
import com.example.rag.data.model.RetrievalMode
import com.example.rag.data.repository.RagRepository
import com.example.rag.engine.ConversationTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class RagUiState(
    val activeStrategy: ChunkingStrategy = ChunkingStrategy.SENTENCE_AWARE,
    val activeRetrievalMode: RetrievalMode = RetrievalMode.HYBRID,
    val isGenerating: Boolean = false,
    val isReindexing: Boolean = false,
    val isUploadingPdf: Boolean = false,
    val uploadProgressMessage: String? = null,
    val apiKey: String = "",
    val googleSearchEnabled: Boolean = true,
    val topK: Int = 4,
    val benchmarkComparison: BenchmarkComparison? = null,
    val isBenchmarking: Boolean = false,
    val selectedDocument: DocumentItem? = null,
    val selectedCitation: Citation? = null,
    val selectedDocumentInsights: DocumentInsights? = null,
    val isGeneratingInsights: Boolean = false,
    val notificationMessage: String? = null
)

class RagViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("rag_ai_prefs", Context.MODE_PRIVATE)
    private val database = RagDatabase.getInstance(application)
    val repository = RagRepository(database.ragDao())

    private val _uiState = MutableStateFlow(
        RagUiState(
            apiKey = prefs.getString("custom_gemini_api_key", "") ?: "",
            googleSearchEnabled = prefs.getBoolean("enable_google_search", true)
        )
    )
    val uiState: StateFlow<RagUiState> = _uiState.asStateFlow()

    val documents: StateFlow<List<DocumentItem>> = repository.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.getChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryTurns: StateFlow<List<ConversationTurn>> = repository.memoryManager.turnsFlow

    val curatedQueries: List<CuratedQuery> = repository.getCuratedQueries()

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty(effectiveApiKey())
        }
    }

    private fun effectiveApiKey(): String? {
        val customKey = _uiState.value.apiKey.trim()
        return if (customKey.isNotBlank()) customKey else null
    }

    fun setStrategy(strategy: ChunkingStrategy) {
        _uiState.value = _uiState.value.copy(activeStrategy = strategy)
    }

    fun setRetrievalMode(mode: RetrievalMode) {
        _uiState.value = _uiState.value.copy(
            activeRetrievalMode = mode,
            notificationMessage = "Switched to ${mode.displayName}"
        )
    }

    fun generateInsightsForDocument(doc: DocumentItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingInsights = true)
            val insights = repository.generateDocumentInsights(doc.id)
            _uiState.value = _uiState.value.copy(
                selectedDocumentInsights = insights,
                isGeneratingInsights = false,
                notificationMessage = "Generated free executive insights for '${doc.title.take(25)}'!"
            )
        }
    }

    fun clearDocumentInsights() {
        _uiState.value = _uiState.value.copy(selectedDocumentInsights = null)
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
        _uiState.value = _uiState.value.copy(
            apiKey = key.trim(),
            notificationMessage = if (key.isNotBlank()) "Custom API key saved! Automatic fallback active." else "Switched to default quota."
        )
    }

    fun setGoogleSearchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("enable_google_search", enabled).apply()
        _uiState.value = _uiState.value.copy(googleSearchEnabled = enabled)
    }

    fun setTopK(k: Int) {
        _uiState.value = _uiState.value.copy(topK = k.coerceIn(1, 8))
    }

    fun selectDocument(doc: DocumentItem?) {
        _uiState.value = _uiState.value.copy(selectedDocument = doc)
        if (doc == null) {
            _uiState.value = _uiState.value.copy(selectedDocumentInsights = null)
        }
    }

    fun selectCitation(citation: Citation?) {
        _uiState.value = _uiState.value.copy(selectedCitation = citation)
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(notificationMessage = null)
    }

    fun uploadPdfs(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            if (uris.isEmpty()) return@launch
            val currentCustomCount = repository.getCustomDocumentsCount()
            if (currentCustomCount >= 20) {
                _uiState.value = _uiState.value.copy(
                    notificationMessage = "Maximum limit of 20 uploaded PDFs already reached. Delete an existing document to upload more."
                )
                return@launch
            }

            val availableSlots = 20 - currentCustomCount
            val urisToProcess = uris.take(availableSlots)
            val skippedCount = uris.size - urisToProcess.size

            _uiState.value = _uiState.value.copy(
                isUploadingPdf = true,
                uploadProgressMessage = "Processing ${urisToProcess.size} PDF(s)..."
            )

            var successCount = 0
            for ((index, uri) in urisToProcess.withIndex()) {
                _uiState.value = _uiState.value.copy(
                    uploadProgressMessage = "Parsing & indexing PDF ${index + 1} of ${urisToProcess.size} into Vector & FTS4 tables..."
                )
                val result = repository.importPdfFromUri(context, uri, effectiveApiKey())
                if (result.isSuccess) {
                    successCount++
                }
            }

            val msg = if (skippedCount > 0) {
                "Successfully indexed $successCount PDF(s). $skippedCount skipped (max 20 PDFs limit reached)."
            } else {
                "Successfully uploaded & indexed $successCount PDF(s) into Room Vector & FTS4! Ready for Q&A."
            }

            _uiState.value = _uiState.value.copy(
                isUploadingPdf = false,
                uploadProgressMessage = null,
                notificationMessage = msg
            )
        }
    }

    fun deleteDocument(docId: String) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
            _uiState.value = _uiState.value.copy(
                selectedDocument = null,
                selectedDocumentInsights = null,
                notificationMessage = "Document and all its vectorized/FTS chunks removed."
            )
        }
    }

    fun uploadSampleResearchPdf() {
        viewModelScope.launch {
            val currentCustomCount = repository.getCustomDocumentsCount()
            if (currentCustomCount >= 20) {
                _uiState.value = _uiState.value.copy(
                    notificationMessage = "Maximum limit of 20 uploaded PDFs reached (20/20)."
                )
                return@launch
            }

            val samplePdfs = listOf(
                Triple(
                    "Autonomous UAV Swarm Coordination & Decentralized Consensus.pdf",
                    "Autonomous Robotics & Aerospace",
                    """
                    # Autonomous UAV Swarm Coordination & Decentralized Consensus
                    
                    Author: Dr. Kenneth Hall, Robotics & Autonomous Flight Systems
                    
                    ## 1. Decentralized Multi-Agent Flocking Algorithms
                    Unmanned Aerial Vehicle (UAV) swarms operating in GPS-denied tactical environments require decentralized consensus protocols to maintain formation flight and collision avoidance without single points of failure. Olfati-Saber flocking dynamics establish three fundamental navigation rules: cohesion, alignment, and separation, mathematically enforced using smooth artificial potential functions ψ(r) and ad-hoc communication topologies represented by dynamic graph Laplacians L(t).
                    
                    ## 2. Ultra-Wideband (UWB) Relative Ranging & State Estimation
                    To overcome severe GPS spoofing and multipath interference, each swarm agent carries an Ultra-Wideband (UWB) transceiver and an onboard Extended Kalman Filter (EKF). Inter-agent range measurements are combined with visual-inertial odometry (VIO) to maintain relative positioning accuracy within 5 centimeters across a 30-agent swarm.
                    
                    ## 3. Asynchronous Byzantine-Fault-Tolerant (BFT) Target Allocation
                    When distributing reconnaissance waypoints across heterogeneous drones, the swarm executes a distributed auction protocol backed by asynchronous Byzantine Fault Tolerance. Even if up to 30% of drones experience adversarial communication jamming or sensor faults, consensus on mission payload allocation converges in sub-100 millisecond rounds.
                    """.trimIndent()
                ),
                Triple(
                    "Next-Gen Neuromorphic Photonic Computing & Spiking Neural Circuits.pdf",
                    "Optoelectronics & AI Hardware",
                    """
                    # Next-Gen Neuromorphic Photonic Computing & Spiking Neural Circuits
                    
                    Author: Prof. Alistair Sterling, Integrated Photonics Laboratory
                    
                    ## 1. Integrated Silicon Photonics for Deep Learning Acceleration
                    Electronic neuromorphic architectures are fundamentally constrained by interconnect RC delay and parasitic capacitive heating. Integrated photonic tensor processors replace electrical interconnects with silicon-on-insulator (SOI) optical waveguides and micro-ring resonators (MRRs). By modulating distinct wavelength-division multiplexed (WDM) laser carriers, multiply-accumulate (MAC) operations occur at the speed of light with sub-femtojoule per bit energy consumption.
                    
                    ## 2. Spiking Optical Neurons via Phase-Change Materials (PCM)
                    Non-volatile phase-change materials such as GST (Ge₂Sb₂Te₅) embedded atop optical waveguides provide optical synaptic plasticity. Femtosecond optical pump pulses trigger reversible amorphous-to-crystalline phase transitions, modulating waveguide absorption and effectively mimicking biological spike-timing-dependent plasticity (STDP).
                    
                    ## 3. Sub-Nanosecond Latency for Edge Machine Perception
                    Photonic neural networks demonstrate inference latencies under 500 picoseconds per layer with throughput exceeding 10 Tera-operations per second per watt (TOPS/W), rendering them ideal for autonomous vehicle LiDAR processing, hypersonic guidance telemetry, and high-frequency financial modeling.
                    """.trimIndent()
                ),
                Triple(
                    "Deep Sea Hydrothermal Vent Microbial Metabolisms & Astrobiology.pdf",
                    "Astrobiology & Marine Extremophiles",
                    """
                    # Deep Sea Hydrothermal Vent Microbial Metabolisms & Astrobiology
                    
                    Author: Dr. Sylvia Mendoza, Deep Oceanic Research Institute
                    
                    ## 1. Chemolithoautotrophy in Serpentinizing Hydrothermal Fields
                    Deep-sea ultramafic hydrothermal vents (such as the Lost City Field) generate hydrogen-rich, hyperalkaline (pH 9-11) fluids through serpentinization reactions: (Mg,Fe)₂SiO₄ + H₂O → serpentine + magnetite + H₂. Chemolithoautotrophic archaea and bacteria synthesize organic compounds from dissolved inorganic carbon without sunlight, using the Wood-Ljungdahl (acetyl-CoA) pathway.
                    
                    ## 2. Redox Geochemical Gradients as Analogues for Enceladus and Europa
                    The precipitous electrochemical potential gradients across porous iron-sulfur vent chimneys generate abiotic proton-motive forces (~200 mV) directly analogous to cellular respiration. These natural catalytic micro-reactors provide strong planetary analogues for the subsurface oceans of Saturn's moon Enceladus and Jupiter's moon Europa, where hydrothermal plumes have been confirmed.
                    
                    ## 3. Thermophilic Enzymatic Stability under 400 Bar Hydrostatic Pressure
                    Extremophilic enzymes (piezophiles) from Methanocaldococcus and Pyrococcus utilize dense hydrophobic cores, extensive salt bridge networks, and proline substitutions to retain active-site tertiary conformation under 400 atmospheres of hydrostatic pressure and 110°C hydrothermal water jets.
                    """.trimIndent()
                )
            )

            val sample = samplePdfs.random()
            _uiState.value = _uiState.value.copy(
                isUploadingPdf = true,
                uploadProgressMessage = "Generating & indexing PDF '${sample.first}' into Room SQLite Vector & FTS4..."
            )

            val result = repository.importSampleResearchPdf(
                title = sample.first,
                category = sample.second,
                author = "Uploaded Research PDF",
                pages = 3,
                content = sample.third,
                apiKey = effectiveApiKey()
            )

            _uiState.value = _uiState.value.copy(
                isUploadingPdf = false,
                uploadProgressMessage = null,
                notificationMessage = if (result.isSuccess) {
                    "Uploaded PDF '${sample.first.take(30)}...' indexed into Room FTS4 & Vector tables! Ready for Q&A."
                } else {
                    result.exceptionOrNull()?.message ?: "Failed to upload sample PDF."
                }
            )
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
            _uiState.value = _uiState.value.copy(notificationMessage = "Chat and conversational memory cleared.")
        }
    }

    fun sendMessage(queryText: String) {
        val query = queryText.trim()
        if (query.isBlank() || _uiState.value.isGenerating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)

            // Save user message
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "user",
                text = query,
                timestamp = System.currentTimeMillis(),
                strategyUsed = _uiState.value.activeStrategy,
                retrievalMode = _uiState.value.activeRetrievalMode
            )
            repository.saveChatMessage(userMsg)

            try {
                // 1. Unified retrieval: Hybrid (Vector + FTS4), Vector Only, or FTS4 Keyword
                val retrievedChunks = repository.retrieveChunks(
                    query = query,
                    strategy = _uiState.value.activeStrategy,
                    mode = _uiState.value.activeRetrievalMode,
                    topK = _uiState.value.topK,
                    apiKey = effectiveApiKey()
                )

                // 2. Call RAG engine with tools, citations, memory
                val response = repository.geminiService.answerQuery(
                    query = query,
                    retrievedChunks = retrievedChunks,
                    activeApiKey = effectiveApiKey(),
                    enableGoogleSearch = _uiState.value.googleSearchEnabled
                )

                // 3. Save assistant message
                val assistantMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    text = response.text,
                    citations = response.citations,
                    toolCalls = response.toolCalls,
                    timestamp = System.currentTimeMillis(),
                    strategyUsed = _uiState.value.activeStrategy,
                    retrievalMode = _uiState.value.activeRetrievalMode,
                    modelName = response.modelUsed,
                    latencyMs = response.latencyMs,
                    isQuotaFallback = response.isQuotaFallback
                )
                repository.saveChatMessage(assistantMsg)

                if (response.isQuotaFallback) {
                    _uiState.value = _uiState.value.copy(
                        notificationMessage = "Quota reached/offline: Synthesized via On-Device RAG Engine with citations."
                    )
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    text = "Encountered an issue processing query: ${e.localizedMessage ?: "Unknown error"}. Please check your connection or custom API key in Settings.",
                    timestamp = System.currentTimeMillis()
                )
                repository.saveChatMessage(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }

    fun runBenchmark(query: String) {
        val q = query.trim()
        if (q.isBlank() || _uiState.value.isBenchmarking) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBenchmarking = true)
            try {
                val comparison = repository.compareChunkingStrategies(
                    query = q,
                    topK = _uiState.value.topK,
                    apiKey = effectiveApiKey()
                )
                _uiState.value = _uiState.value.copy(
                    benchmarkComparison = comparison,
                    isBenchmarking = false,
                    notificationMessage = "Benchmark completed across 10 documents!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isBenchmarking = false)
            }
        }
    }

    fun reindexAll(strategy: ChunkingStrategy) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReindexing = true)
            repository.indexAllForStrategy(strategy, effectiveApiKey())
            _uiState.value = _uiState.value.copy(
                isReindexing = false,
                notificationMessage = "Re-indexed 10 documents using ${strategy.displayName}."
            )
        }
    }

    fun addNewDocument(title: String, category: String, author: String, content: String) {
        viewModelScope.launch {
            repository.addCustomDocument(title, category, author, content, effectiveApiKey())
            _uiState.value = _uiState.value.copy(
                notificationMessage = "Added & embedded document '$title'!"
            )
        }
    }
}
