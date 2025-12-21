package com.thk.knowledgeretrievalkmp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Request
@Serializable
data class RegisterUserRequest(
    val email: String,
    @SerialName("username")
    val userName: String,
    val password: String,
    @SerialName("full_name")
    val fullName: String
)

@Serializable
data class LoginUserRequest(
    val email: String,
    val password: String,
    @SerialName("remember_me")
    val remember: Boolean
)

@Serializable
data class LoginUserWithGoogleRequest(
    @SerialName("id_token")
    val idToken: String
)

@Serializable
data class ExchangeGoogleAuthCodeRequest(
    val code: String
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class GetKnowledgeBasesRequest(
    @SerialName("user_id")
    val userId: String,
    val skip: Int,
    val limit: Int
)

@Serializable
data class CreateKnowledgeBaseRequest(
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String
)

@Serializable
data class DeleteKnowledgeBaseRequest(
    val id: String,
    @SerialName("user_id")
    val userId: String
)

@Serializable
data class UpdateKnowledgeBaseRequest(
    val name: String,
    val description: String,
    @SerialName("is_active")
    val isActive: Boolean
)

@Serializable
data class UpdateConversationRequest(
    @SerialName("conversation_id")
    val conversationId: String,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean
)

@Serializable
data class AskRequest(
    val message: NetworkMessage,
    val agentic: Boolean,
    @SerialName("web_search")
    val webSearch: Boolean
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

// Response
@Serializable
data class NetworkResponse<T>(
    val success: Boolean? = null,
    @SerialName("status_code")
    val statusCode: Int? = null,
    val message: String? = null,
    val data: T?,
    val error: NetworkError? = null,
    val timestamp: String? = null
)

@Serializable
data class NetworkError(
    val type: String? = null,
    val detail: NetworkErrorDetail? = null
)

@Serializable
data class NetworkErrorDetail(
    val message: String? = null,
    val code: String? = null
)

@Serializable
data class NetworkKnowledgeBase(
    val id: String,
    val name: String,
    @SerialName("user_id")
    val userId: String,
    val description: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("document_count")
    val documentCount: Long? = null
)

@Serializable
data class NetworkDocument(
    val id: String,
    @SerialName("knowledge_base_id")
    val knowledgeBaseId: String,
    val filename: String,
    val description: String? = null,
    @SerialName("original_filename")
    val originalFileName: String,
    @SerialName("file_path")
    val filePath: String? = null,
    @SerialName("file_size")
    val fileSize: Long? = null,
    @SerialName("file_type")
    val fileType: String? = null,
    @SerialName("mime_type")
    val mimeType: String,
    val status: NetworkDocumentStatus,
    @SerialName("processing_error")
    val processingError: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("uploaded_by")
    val uploadedBy: String? = null,
    @SerialName("total_chunks")
    val totalChunks: Int? = null,
    @SerialName("processed_at")
    val processedAt: String? = null,
    @SerialName("is_inactive")
    val isInactive: Boolean
)

@Serializable
enum class NetworkDocumentStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("processing")
    PROCESSING,

    @SerialName("finished")
    FINISHED,

    @SerialName("failed")
    FAILED,

    @SerialName("timeout")
    TIMEOUT
}

@Serializable
data class NetworkConversation(
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("is_active")
    val isActive: Boolean,
    val name: String,
    val summary: String? = null,
    @SerialName("summarized_up_to_message_order")
    val summarizedUpToMessageOrder: Int? = null
)

@Serializable
data class NetworkMessage(
    val id: String,
    val role: NetworkMessageRole,
    val parts: List<NetworkPartText>,
    val metadata: NetworkMessageMetadata,
    @SerialName("retrieval_context")
    val retrievalContext: RetrievalContext? = null
)

@Serializable
data class NetworkPartText(
    val type: String,
    val text: String
)

@Serializable
data class RetrievalContext(
    @SerialName("original_query")
    val originalQuery: String? = null,
    @SerialName("contextualized_query")
    val contextualizedQuery: String? = null,
    @SerialName("retrieved_at")
    val retrievedAt: String? = null,
    @SerialName("total_retrieved")
    val totalRetrieved: Int? = null,
    @SerialName("total_cited")
    val totalCited: Int? = null,
    @SerialName("cited_chunks")
    val citedChunks: List<CitedChunk>,
    @SerialName("search_method")
    val searchMethod: String? = null
)

@Serializable
data class CitedChunk(
    val metadata: CitedChunkMetadata,
    @SerialName("page_content")
    val pageContent: String
)

@Serializable
data class CitedChunkMetadata(
    @SerialName("kb_id")
    val kbId: String? = null,
    @SerialName("document_id")
    val documentId: String? = null,
    @SerialName("filename")
    val fileName: String? = null,
    @SerialName("original_filename")
    val originalFileName: String,
    @SerialName("start_index")
    val startIndex: Int? = null,
    @SerialName("end_index")
    val endIndex: Int? = null,
    @SerialName("chunk_index")
    val chunkIndex: Int,
    @SerialName("original_index")
    val originalIndex: Int? = null,
    val favicon: String? = null
)

@Serializable
data class NetworkMessageMetadata(
    @SerialName("conversation_id")
    val conversationId: String = "",
    @SerialName("kb_id")
    val kbId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("app_name")
    val appName: String = "",
    @SerialName("finish_reason")
    val finishReason: String? = null,
    @SerialName("search_method")
    val searchMethod: String? = null,
    @SerialName("message_order")
    val messageOrder: Long? = null
)

@Serializable
enum class NetworkMessageRole {
    @SerialName("user")
    USER,

    @SerialName("agent")
    AGENT
}

@Serializable
data class NetworkDocumentQa(
    val question: String,
    val answer: String,
    @SerialName("document_id")
    val documentId: String,
    @SerialName("kb_id")
    val kbId: String
)

@Serializable
data class GetKnowledgeBasesData(
    val items: List<NetworkKnowledgeBase>?,
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null,
    @SerialName("has_more")
    val hasMore: Boolean? = null
)

@Serializable
data class GetDocumentsData(
    val items: List<NetworkDocument>,
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null,
    @SerialName("has_more")
    val hasMore: Boolean? = null
)

@Serializable
data class UploadDocumentData(
    val id: String,
    @SerialName("filename")
    val fileName: String,
    @SerialName("original_filename")
    val originalFileName: String,
    val status: NetworkDocumentStatus,
    @SerialName("task_id")
    val taskId: String? = null
)

@Serializable
data class GetDocumentStatusData(
    val id: String,
    @SerialName("filename")
    val fileName: String,
    @SerialName("original_filename")
    val originalFileName: String,
    val status: NetworkDocumentStatus,
    @SerialName("processing_error")
    val processingError: String? = null,
    @SerialName("processed_at")
    val processedAt: String? = null,
    @SerialName("total_chunks")
    val totalChunks: Int? = null
)

@Serializable
data class AuthenticationData(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("user_id")
    val userId: String? = null
)