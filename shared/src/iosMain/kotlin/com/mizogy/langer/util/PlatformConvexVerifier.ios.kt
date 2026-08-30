package com.mizogy.langer.util

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ConvexRequest<T>(
    val path: String,
    val args: T,
    val format: String = "json"
)

@Serializable
private data class ConvexResponse<T>(
    val status: String,
    val value: T? = null,
    val errorMessage: String? = null
)

@Serializable
private data class ConvexTaskState(
    val taskId: String,
    val taskStatus: String,
    val extractedMarkdown: String? = null,
    val devinSessionId: String? = null,
    val devinLogs: List<String> = emptyList(),
    val generatedDeckId: String? = null
)

@Serializable
private data class IosConvexDeck(
    val id: String,
    val name: String,
    val description: String,
    val category: String
)

@Serializable
private data class IosConvexCard(
    val id: String,
    val deckId: String,
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String,
    val imageUrl: String,
    val audioUrl: String? = null
)

@Serializable
private data class GetTaskStateArgs(val taskId: String)

@Serializable
private data class UpdateStateArgs(
    val taskId: String,
    val status: String,
    val markdown: String?,
    val sessionId: String?
)

@Serializable
private data class TriggerGenerationArgs(
    val url: String,
    val taskId: String
)

@Serializable
private data class GetGeneratedDeckArgs(val deckId: String)

@Serializable
private data class GetGeneratedCardsArgs(val deckId: String)

actual class PlatformConvexVerifier actual constructor(private val convexUrl: String) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    actual fun subscribeToTask(taskId: String): Flow<TestAppState> = flow {
        while (true) {
            try {
                val requestPayload = ConvexRequest(
                    path = "tasks:getTaskState",
                    args = GetTaskStateArgs(taskId)
                )
                val response: ConvexResponse<ConvexTaskState> = httpClient.post("$convexUrl/api/query") {
                    contentType(ContentType.Application.Json)
                    setBody(requestPayload)
                }.body()

                if (response.status == "success" && response.value != null) {
                    val state = response.value
                    emit(
                        TestAppState(
                            taskId = state.taskId,
                            taskStatus = state.taskStatus,
                            extractedMarkdown = state.extractedMarkdown,
                            devinSessionId = state.devinSessionId,
                            devinLogs = state.devinLogs,
                            generatedDeckId = state.generatedDeckId
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(1000) // Poll every 1 second
        }
    }

    actual suspend fun updateTaskState(
        taskId: String,
        status: String,
        markdown: String?,
        sessionId: String?
    ) {
        try {
            val requestPayload = ConvexRequest(
                path = "tasks:updateState",
                args = UpdateStateArgs(taskId, status, markdown, sessionId)
            )
            httpClient.post("$convexUrl/api/mutation") {
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun triggerGeneration(url: String, taskId: String) {
        try {
            val requestPayload = ConvexRequest(
                path = "generator:generateDeckFromUrl",
                args = TriggerGenerationArgs(url, taskId)
            )
            httpClient.post("$convexUrl/api/action") {
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun getGeneratedDeck(deckId: String): TestDeck? {
        return try {
            val requestPayload = ConvexRequest(
                path = "tasks:getGeneratedDeck",
                args = GetGeneratedDeckArgs(deckId)
            )
            val response: ConvexResponse<IosConvexDeck> = httpClient.post("$convexUrl/api/query") {
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }.body()

            if (response.status == "success" && response.value != null) {
                val deck = response.value
                TestDeck(
                    id = deck.id,
                    name = deck.name,
                    description = deck.description,
                    category = deck.category
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun getGeneratedCards(deckId: String): List<TestCard> {
        return try {
            val requestPayload = ConvexRequest(
                path = "tasks:getGeneratedCards",
                args = GetGeneratedCardsArgs(deckId)
            )
            val response: ConvexResponse<List<IosConvexCard>> = httpClient.post("$convexUrl/api/query") {
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }.body()

            if (response.status == "success" && response.value != null) {
                response.value.map { card ->
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
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
