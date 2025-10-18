package com.thk.knowledgeretrievalkmp.data.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal expect val httpClientEngine: HttpClientEngineFactory<*>

val httpClient = HttpClient(httpClientEngine) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(SSE) {
    }
    install(Logging) {
        level = LogLevel.ALL
    }
}

