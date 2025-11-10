package com.thk.knowledgeretrievalkmp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

// SSE event

@Serializable
enum class SseEvent(val value: String) {
    @SerialName("message_start")
    START("message_start"),

    @SerialName("status")
    STATUS("status"),

    @SerialName("content_block_delta")
    CONTENT("content_block_delta"),

    @SerialName("message_stop")
    STOP("message_stop"),

    @SerialName("error")
    ERROR("error")
}

// SSE data

sealed class SseData

@Serializable
@JsonIgnoreUnknownKeys
data class SseStartData(
    val message: String,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
@JsonIgnoreUnknownKeys
data class SseStatusData(
    val phase: String,
    val message: String,
    val metadata: SseStatusMetadata? = null,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
@JsonIgnoreUnknownKeys
data class SseContentData(
    val delta: SseContentDelta,
    val index: Int,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
@JsonIgnoreUnknownKeys
data class SseStopData(
    val metadata: SseStopMetadata,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
@JsonIgnoreUnknownKeys
data class SseErrorData(
    val type: String,
    val message: String,
    val code: Int,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
@JsonIgnoreUnknownKeys
data class SseContentDelta(
    val type: String,
    val text: String
)

@Serializable
@JsonIgnoreUnknownKeys
data class SseStopMetadata(
    val routes: List<String>,
    @SerialName("response_mode")
    val responseMode: String,
    @SerialName("execution_history")
    val executionHistory: List<String>
)

@Serializable
@JsonIgnoreUnknownKeys
data class SseStatusMetadata(
    @SerialName("sources_count")
    val sourcesCount: Int
)