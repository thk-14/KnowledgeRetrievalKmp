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
    val progress: Float,
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
    @SerialName("search_method")
    val searchMethod: String,
    @SerialName("app_name")
    val appName: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("kb_id")
    val kbId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("finish_reason")
    val finishReason: String,
    @SerialName("model_name")
    val modelName: String,
    @SerialName("token_usage")
    val tokenUsage: SseTokenUsage,
)

@Serializable
data class SseTokenUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)