package com.mizogy.langer.util

import kotlinx.coroutines.flow.Flow

data class TestAppState(
    val taskId: String,
    val taskStatus: String,
    val extractedMarkdown: String? = null,
    val devinSessionId: String? = null,
    val devinLogs: List<String> = emptyList(),
    val generatedDeckId: String? = null
)

data class TestDeck(
    val id: String,
    val name: String,
    val description: String,
    val category: String
)

data class TestCard(
    val id: String,
    val deckId: String,
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String,
    val imageUrl: String,
    val audioUrl: String? = null
)

expect class PlatformConvexVerifier(convexUrl: String) {
    fun subscribeToTask(taskId: String): Flow<TestAppState>
    suspend fun updateTaskState(
        taskId: String,
        status: String,
        markdown: String?,
        sessionId: String?
    )
    suspend fun triggerGeneration(url: String, taskId: String)
    suspend fun getGeneratedDeck(deckId: String): TestDeck?
    suspend fun getGeneratedCards(deckId: String): List<TestCard>
}
