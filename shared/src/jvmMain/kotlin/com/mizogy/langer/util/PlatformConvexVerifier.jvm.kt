package com.mizogy.langer.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class PlatformConvexVerifier actual constructor(convexUrl: String) {
    private val mockState = MutableStateFlow(TestAppState("mock-id", "Mock Status (JVM - Simulated)"))

    actual fun subscribeToTask(taskId: String): Flow<TestAppState> {
        return mockState
    }

    actual suspend fun updateTaskState(
        taskId: String,
        status: String,
        markdown: String?,
        sessionId: String?
    ) {
        mockState.value = TestAppState(
            taskId = taskId,
            taskStatus = "$status (JVM Simulated)",
            extractedMarkdown = markdown,
            devinSessionId = sessionId
        )
    }

    actual suspend fun triggerGeneration(url: String, taskId: String) {
        mockState.value = TestAppState(
            taskId = taskId,
            taskStatus = "AI Generation Triggered (JVM Simulated)",
            extractedMarkdown = "Generating from URL: $url",
            devinSessionId = "devin-mock-session"
        )
    }

    actual suspend fun getGeneratedDeck(deckId: String): TestDeck? {
        return TestDeck(
            id = deckId,
            name = "Vocab: JVM Web Preview",
            description = "Simulated JVM vocabulary deck",
            category = "Brainstorm"
        )
    }

    actual suspend fun getGeneratedCards(deckId: String): List<TestCard> {
        return listOf(
            TestCard(
                id = "mock-jvm-1",
                deckId = deckId,
                word = "abundant",
                phonetic = "ə'bʌndənt",
                meaning = "Existing or available in large quantities; overflowing.",
                example = "The website contains abundant useful reference resources.",
                imageUrl = "https://images.unsplash.com/photo-1546410531-bb4caa6b424d"
            )
        )
    }
}
