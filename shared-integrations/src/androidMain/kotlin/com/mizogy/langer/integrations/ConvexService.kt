package com.mizogy.langer.integrations

import dev.convex.android.ConvexClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class AppState(
    val taskId: String,
    val taskStatus: String,
    val extractedMarkdown: String? = null,
    val devinSessionId: String? = null,
    val devinLogs: List<String> = emptyList()
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
}
