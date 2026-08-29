package com.mizogy.langer.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class PlatformConvexVerifier actual constructor(convexUrl: String) {
    private val mockState = MutableStateFlow(TestAppState("mock-id", "Mock Status (JS - Simulated)"))

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
            taskStatus = "$status (JS Simulated)",
            extractedMarkdown = markdown,
            devinSessionId = sessionId
        )
    }
}
