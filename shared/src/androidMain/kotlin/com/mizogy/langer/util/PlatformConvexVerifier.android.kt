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
                devinLogs = appState.devinLogs
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
}
