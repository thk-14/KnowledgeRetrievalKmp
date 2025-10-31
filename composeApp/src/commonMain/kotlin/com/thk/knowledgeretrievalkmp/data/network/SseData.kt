package com.thk.knowledgeretrievalkmp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    STOP("message_stop")
}

// SSE data

sealed class SseData

@Serializable
data class SseStartData(
    val message: String,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
data class SseStatusData(
    val phase: String,
    val message: String,
    val metadata: SseStatusMetadata? = null,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
data class SseContentData(
    val delta: SseContentDelta,
    val index: Int,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
data class SseStopData(
    val metadata: SseStopMetadata,
    val id: String,
    val timestamp: String
) : SseData()

@Serializable
data class SseContentDelta(
    val type: String,
    val text: String
)

@Serializable
data class SseStopMetadata(
    val route: String,
    @SerialName("response_mode")
    val responseMode: String,
    @SerialName("execution_history")
    val executionHistory: List<String>
)

@Serializable
data class SseStatusMetadata(
    @SerialName("sources_count")
    val sourcesCount: Int
)