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
    val success: Boolean,
    @SerialName("status_code")
    val statusCode: Int,
    val message: String,
    val data: T?,
    val error: NetworkError?,
    val timestamp: String
)

@Serializable
data class NetworkError(
    val type: String,
    val detail: NetworkErrorDetail
)

@Serializable
data class NetworkErrorDetail(
    val message: String,
    val code: String
)

@Serializable
data class NetworkKnowledgeBase(
    val id: String,
    val name: String,
    @SerialName("user_id")
    val userId: String,
    val description: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("document_count")
    val documentCount: Long
)

@Serializable
data class NetworkDocument(
    val id: String,
    @SerialName("knowledge_base_id")
    val knowledgeBaseId: String,
    val filename: String,
    val description: String?,
    @SerialName("original_filename")
    val originalFilename: String,
    @SerialName("file_path")
    val filePath: String,
    @SerialName("file_size")
    val fileSize: Long,
    @SerialName("file_type")
    val fileType: String,
    @SerialName("mime_type")
    val mimeType: String,
    val status: NetworkDocumentStatus,
    @SerialName("processing_error")
    val processingError: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("uploaded_by")
    val uploadedBy: String?,
    @SerialName("total_chunks")
    val totalChunks: Int,
    @SerialName("processed_at")
    val processedAt: String,
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
    val summary: String?,
    @SerialName("summarized_up_to_message_order")
    val summarizedUpToMessageOrder: Int?
)

@Serializable
data class NetworkMessage(
    val id: String,
    val role: NetworkMessageRole,
    val parts: List<NetworkPartText>,
    val metadata: NetworkMessageMetadata,
//    val retrievalContext: RetrievalContext?
)

@Serializable
data class NetworkPartText(
    val type: String,
    val text: String
)

@Serializable
data class RetrievalContext(
    @SerialName("original_query")
    val originalQuery: String,
    @SerialName("contextualized_query")
    val contextualizedQuery: String,
    @SerialName("retrieved_at")
    val retrievedAt: String,
    @SerialName("total_retrieved")
    val totalRetrieved: Int,
    @SerialName("total_cited")
    val totalCited: Int,
    @SerialName("cited_chunks")
    val citedChunks: List<CitedChunk>,
    @SerialName("search_method")
    val searchMethod: String
)

@Serializable
data class CitedChunk(
    val id: String,
    val metadata: String,
    @SerialName("page_content")
    val pageContent: String,
    val type: String
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
    val total: Int,
    val skip: Int,
    val limit: Int,
    @SerialName("has_more")
    val hasMore: Boolean
)

@Serializable
data class GetDocumentsData(
    val items: List<NetworkDocument>,
    val total: Int,
    val skip: Int,
    val limit: Int,
    @SerialName("has_more")
    val hasMore: Boolean
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
    val taskId: String
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
    val processingError: String?,
    @SerialName("processed_at")
    val processedAt: String?,
    @SerialName("total_chunks")
    val totalChunks: Int
)

@Serializable
data class AuthenticationData(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String
)