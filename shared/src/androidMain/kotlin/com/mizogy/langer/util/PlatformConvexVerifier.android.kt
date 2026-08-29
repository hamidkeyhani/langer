package com.mizogy.langer.util

import com.mizogy.langer.integrations.ConvexService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual class PlatformConvexVerifier actual constructor(convexUrl: String) {
    private val convexService = ConvexService(convexUrl)

    actual fun subscribeToTask(taskId: String): Flow<TestAppState> {
        return convexService.subscribeToTask(taskId).map { appState ->
            TestAppState(
                taskId = appState.taskId,
                taskStatus = appState.taskStatus,
                extractedMarkdown = appState.extractedMarkdown,
                devinSessionId = appState.devinSessionId,
                devinLogs = appState.devinLogs,
                generatedDeckId = appState.generatedDeckId
            )
        }
    }

    actual suspend fun updateTaskState(
        taskId: String,
        status: String,
        markdown: String?,
        sessionId: String?
    ) {
        convexService.updateTaskState(
            taskId = taskId,
            status = status,
            markdown = markdown,
            sessionId = sessionId
        )
    }

    actual suspend fun triggerGeneration(url: String, taskId: String) {
        convexService.triggerGeneration(url, taskId)
    }

    actual suspend fun getGeneratedDeck(deckId: String): TestDeck? {
        val deck = convexService.getGeneratedDeck(deckId) ?: return null
        return TestDeck(
            id = deck.id,
            name = deck.name,
            description = deck.description,
            category = deck.category
        )
    }

    actual suspend fun getGeneratedCards(deckId: String): List<TestCard> {
        return convexService.getGeneratedCards(deckId).map { card ->
            TestCard(
                id = card.id,
                deckId = card.deckId,
                word = card.word,
                phonetic = card.phonetic,
                meaning = card.meaning,
                example = card.example,
                imageUrl = card.imageUrl,
                audioUrl = card.audioUrl
            )
        }
    }
}
