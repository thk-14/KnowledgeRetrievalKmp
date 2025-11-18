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
    val references: List<SseStopReference>? = null,
    val metadata: SseStopMetadata? = null,
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
    @SerialName("search_method")
    val searchMethod: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("kb_id")
    val kbId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("app_name")
    val appName: String,
    @SerialName("finish_reason")
    val finishReason: String,
    @SerialName("model_name")
    val modelName: String,
)

@Serializable
@JsonIgnoreUnknownKeys
data class SseStopReference(
    val id: String?,
    val metadata: SseStopReferenceMetadata,
    @SerialName("page_content")
    val pageContent: String,
    val type: String,
)

@Serializable
@JsonIgnoreUnknownKeys
data class SseStopReferenceMetadata(
    @SerialName("chunk_index")
    val chunkIndex: Int,
    val filename: String,
    @SerialName("kb_id")
    val kbId: String,
    @SerialName("document_id")
    val documentId: String,
    @SerialName("original_filename")
    val originalFileName: String,
    @SerialName("start_index")
    val startIndex: Int,
    @SerialName("end_index")
    val endIndex: Int,
    @SerialName("original_index")
    val originalIndex: Int
)

@Serializable
@JsonIgnoreUnknownKeys
data class SseStatusMetadata(
    @SerialName("sources_count")
    val sourcesCount: Int
)