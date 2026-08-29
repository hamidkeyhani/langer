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
data class ScrapeRequest(val url: String)

@Serializable
data class ScrapeResponse(
    val url: String,
    val markdown: String? = null,
    val title: String? = null,
    val error: String? = null
)

@Serializable
data class BrandResponse(
    val name: String? = null,
    val colors: List<String>? = null,
    val logoUrl: String? = null
)

class ContextDevClient(
    private val apiKey: String,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) {
    private val baseUrl = "https://api.context.dev/v1"

    /**
     * Scrapes a website and returns its LLM-ready markdown representation.
     */
    suspend fun scrapeUrl(url: String): Result<ScrapeResponse> = runCatching {
        httpClient.get("$baseUrl/web/scrape/markdown") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            parameter("url", url)
        }.body()
    }

    /**
     * Retrieves brand assets, logos, and styling info for a domain.
     */
    suspend fun getBrandInfo(domain: String): Result<BrandResponse> = runCatching {
        httpClient.get("$baseUrl/brand/retrieve") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            parameter("domain", domain)
        }.body()
    }
}
