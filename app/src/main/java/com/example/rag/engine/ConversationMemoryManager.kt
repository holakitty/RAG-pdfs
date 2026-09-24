package com.example.rag.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConversationTurn(
    val turnNumber: Int,
    val userQuery: String,
    val assistantResponse: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ConversationMemoryManager(private val maxTurns: Int = 5) {

    private val turns = mutableListOf<ConversationTurn>()
    private val _turnsFlow = MutableStateFlow<List<ConversationTurn>>(emptyList())
    val turnsFlow: StateFlow<List<ConversationTurn>> = _turnsFlow.asStateFlow()

    @Synchronized
    fun addTurn(userQuery: String, assistantResponse: String) {
        val nextTurnNumber = (turns.lastOrNull()?.turnNumber ?: 0) + 1
        turns.add(
            ConversationTurn(
                turnNumber = nextTurnNumber,
                userQuery = userQuery,
                assistantResponse = assistantResponse
            )
        )
        // Keep strictly the last 5 conversational turns
        while (turns.size > maxTurns) {
            turns.removeAt(0)
        }
        _turnsFlow.value = turns.toList()
    }

    @Synchronized
    fun getRecentTurns(): List<ConversationTurn> {
        return turns.toList()
    }

    @Synchronized
    fun clearMemory() {
        turns.clear()
        _turnsFlow.value = emptyList()
    }

    /**
     * Formats conversational context into prompt dialogue for the LLM.
     */
    fun formatHistoryContext(): String {
        if (turns.isEmpty()) return ""
        val sb = StringBuilder("=== CONVERSATIONAL MEMORY (Previous ${turns.size} Turns) ===\n")
        turns.forEachIndexed { index, turn ->
            sb.append("Turn #${index + 1}:\n")
            sb.append("User: ${turn.userQuery}\n")
            sb.append("Assistant: ${turn.assistantResponse.take(300)}...\n\n")
        }
        sb.append("=== END MEMORY ===\n")
        return sb.toString()
    }
}
