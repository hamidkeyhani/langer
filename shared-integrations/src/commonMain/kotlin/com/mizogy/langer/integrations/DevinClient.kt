package com.mizogy.langer.integrations

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CreateSessionRequest(
    val prompt: String,
    val title: String? = null
)

@Serializable
data class DevinSession(
    val id: String,
    val status: String,
    val webUrl: String? = null
)

@Serializable
data class DevinMessageRequest(
    val message: String
)

class DevinClient(
    private val apiKey: String,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) {
    private val baseUrl = "https://api.devin.ai/v3"

    /**
     * Starts an autonomous coding session with Devin
     */
    suspend fun createSession(prompt: String, title: String? = null): Result<DevinSession> = runCatching {
        httpClient.post("$baseUrl/sessions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(CreateSessionRequest(prompt, title))
        }.body()
    }

    /**
     * Sends a direct message/instruction to an active Devin session
     */
    suspend fun sendMessage(sessionId: String, message: String): Result<Unit> = runCatching {
        httpClient.post("$baseUrl/sessions/$sessionId/messages") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(DevinMessageRequest(message))
        }
    }

    /**
     * Polls the status of an active session
     */
    suspend fun getSessionStatus(sessionId: String): Result<DevinSession> = runCatching {
        httpClient.get("$baseUrl/sessions/$sessionId") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }.body()
    }
}
