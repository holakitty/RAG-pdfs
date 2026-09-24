package com.example.rag.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.rag.ui.screens.BenchmarkScreen
import com.example.rag.ui.screens.ChatScreen
import com.example.rag.ui.screens.DocumentsScreen
import com.example.rag.ui.screens.QueriesScreen
import com.example.rag.ui.screens.SettingsScreen

enum class RagTab(val label: String, val testTag: String) {
    CHAT("Chat & Q&A", "tab_chat"),
    DOCUMENTS("10 Docs", "tab_docs"),
    BENCHMARK("Chunking", "tab_benchmark"),
    QUERIES("Curated", "tab_queries"),
    SETTINGS("Pipeline", "tab_settings")
}

@Composable
fun MainApp(
    viewModel: RagViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableIntStateOf(0) }
    val documents by viewModel.documents.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                RagTab.entries.forEachIndexed { index, tab ->
                    val icon = when (tab) {
                        RagTab.CHAT -> Icons.Default.AutoAwesome
                        RagTab.DOCUMENTS -> Icons.AutoMirrored.Filled.MenuBook
                        RagTab.BENCHMARK -> Icons.AutoMirrored.Filled.CompareArrows
                        RagTab.QUERIES -> Icons.Default.Lightbulb
                        RagTab.SETTINGS -> Icons.Default.Settings
                    }

                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(imageVector = icon, contentDescription = tab.label) },
                        label = { Text(text = tab.label, fontSize = 11.sp) },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentTab) {
            0 -> ChatScreen(
                viewModel = viewModel,
                onNavigateToDocument = { docId ->
                    val doc = documents.find { it.id == docId }
                    if (doc != null) {
                        viewModel.selectDocument(doc)
                        currentTab = 1
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> DocumentsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> BenchmarkScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> QueriesScreen(
                viewModel = viewModel,
                onExecuteQueryInChat = { query ->
                    currentTab = 0
                    viewModel.sendMessage(query)
                },
                modifier = Modifier.padding(innerPadding)
            )
            4 -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
