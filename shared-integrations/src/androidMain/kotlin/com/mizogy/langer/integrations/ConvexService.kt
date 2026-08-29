package com.mizogy.langer.integrations

import dev.convex.android.ConvexClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

@Serializable
data class AppState(
    val taskId: String,
    val taskStatus: String,
    val extractedMarkdown: String? = null,
    val devinSessionId: String? = null,
    val devinLogs: List<String> = emptyList(),
    val generatedDeckId: String? = null
)

@Serializable
data class ConvexDeck(
    val id: String,
    val name: String,
    val description: String,
    val category: String
)

@Serializable
data class ConvexCard(
    val id: String,
    val deckId: String,
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String,
    val imageUrl: String,
    val audioUrl: String? = null
)

class ConvexService(
    private val convexUrl: String
) {
    private val client = ConvexClient(convexUrl)

    /**
     * Subscribes to real-time updates of a task's state in Convex
     */
    fun subscribeToTask(taskId: String): Flow<AppState> {
        return client.subscribe<AppState>(
            name = "tasks:getTaskState",
            args = mapOf("taskId" to taskId)
        ).map { result ->
            result.getOrThrow()
        }
    }

    /**
     * Triggers a mutation to update state when a task changes
     */
    suspend fun updateTaskState(
        taskId: String, 
        status: String, 
        markdown: String?, 
        sessionId: String?
    ) {
        client.mutation(
            name = "tasks:updateState",
            args = mapOf(
                "taskId" to taskId,
                "status" to status,
                "markdown" to markdown,
                "sessionId" to sessionId
            )
        )
    }

    /**
     * Triggers the Web-to-Flashcard generator action on the backend
     */
    suspend fun triggerGeneration(url: String, taskId: String) {
        client.action(
            name = "generator:generateDeckFromUrl",
            args = mapOf("url" to url, "taskId" to taskId)
        )
    }

    /**
     * Fetches the generated deck metadata from Convex
     */
    suspend fun getGeneratedDeck(deckId: String): ConvexDeck? {
        return client.subscribe<ConvexDeck?>(
            name = "tasks:getGeneratedDeck",
            args = mapOf("deckId" to deckId)
        ).map { it.getOrThrow() }.first()
    }

    /**
     * Fetches the generated flashcards from Convex
     */
    suspend fun getGeneratedCards(deckId: String): List<ConvexCard> {
        return client.subscribe<List<ConvexCard>>(
            name = "tasks:getGeneratedCards",
            args = mapOf("deckId" to deckId)
        ).map { it.getOrThrow() }.first()
    }
}
