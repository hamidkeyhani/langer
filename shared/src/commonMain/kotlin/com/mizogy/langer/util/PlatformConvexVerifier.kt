package com.mizogy.langer.util

import kotlinx.coroutines.flow.Flow

data class TestAppState(
    val taskId: String,
    val taskStatus: String,
    val extractedMarkdown: String? = null,
    val devinSessionId: String? = null,
    val devinLogs: List<String> = emptyList()
)

expect class PlatformConvexVerifier(convexUrl: String) {
    fun subscribeToTask(taskId: String): Flow<TestAppState>
    suspend fun updateTaskState(
        taskId: String,
        status: String,
        markdown: String?,
        sessionId: String?
    )
}
