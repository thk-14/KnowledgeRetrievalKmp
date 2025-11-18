package com.thk.knowledgeretrievalkmp.data.network

import com.thk.knowledgeretrievalkmp.data.AppContainer
import com.thk.knowledgeretrievalkmp.util.log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.sse.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

class NetworkApiService {
    private val client = HttpClient(getHttpClientEngine()) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(SSE) {
        }
        install(Auth) {
            bearer {
                loadTokens {
                    // FOR TESTING
                    val accessToken = "hapt68"
                    // END TESTING

//                    val accessToken = AppContainer.sessionManager.getAccessToken()
                    val refreshToken = AppContainer.sessionManager.getRefreshToken()
                    if (accessToken != null)
                        BearerTokens(accessToken, refreshToken)
                    else
                        null
                }
                refreshTokens refreshTokenHandler@{
                    val refreshToken = AppContainer.sessionManager.getRefreshToken() ?: return@refreshTokenHandler null
                    val tokens = refreshToken(
                        RefreshTokenRequest(refreshToken)
                    )?.data ?: return@refreshTokenHandler null
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                }
            }
        }
        install(DefaultRequest) {
            header("ngrok-skip-browser-warning", true)
        }
//        install(HttpTimeout) {
//            requestTimeoutMillis = 30000
//            connectTimeoutMillis = 20000
//            socketTimeoutMillis = 20000
//        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    private val baseUrl = "https://smart-kind-macaque.ngrok-free.app"

    // Authentication

    suspend fun registerUser(registerUserRequest: RegisterUserRequest): NetworkResponse<String>? = try {
        client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerUserRequest)
        }.body()
    } catch (exception: Exception) {
        log("registerUser failed: ${exception.message}")
        null
    }

    suspend fun loginUser(loginUserRequest: LoginUserRequest): NetworkResponse<AuthenticationData>? = try {
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(loginUserRequest)
        }.body()
    } catch (exception: Exception) {
        log("loginUser failed: ${exception.message}")
        null
    }

    suspend fun loginUserWithGoogle(loginUserWithGoogleRequest: LoginUserWithGoogleRequest): NetworkResponse<AuthenticationData>? =
        try {
            client.post("$baseUrl/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(loginUserWithGoogleRequest)
            }.body()
        } catch (exception: Exception) {
            log("loginUserWithGoogle failed: ${exception.message}")
            null
        }

    suspend fun logoutUser(logoutRequest: LogoutRequest): NetworkResponse<String>? = try {
        client.post("$baseUrl/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(logoutRequest)
        }.body()
    } catch (exception: Exception) {
        log("logoutUser failed: ${exception.message}")
        null
    }

    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): NetworkResponse<AuthenticationData>? = try {
        client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(refreshTokenRequest)
        }.body()
    } catch (exception: Exception) {
        log("refreshToken failed: ${exception.message}")
        null
    }

    // Knowledge Base

    suspend fun getKnowledgeBases(getKnowledgeBasesRequest: GetKnowledgeBasesRequest): NetworkResponse<GetKnowledgeBasesData>? =
        try {
            client.post("$baseUrl/kb/user") {
                contentType(ContentType.Application.Json)
                setBody(getKnowledgeBasesRequest)
            }.body()
        } catch (exception: Exception) {
            log("getKnowledgeBases failed: ${exception.message}")
            null
        }

    suspend fun createKnowledgeBase(createKnowledgeBaseRequest: CreateKnowledgeBaseRequest): NetworkResponse<NetworkKnowledgeBase>? =
        try {
            client.post("$baseUrl/kb/create") {
                contentType(ContentType.Application.Json)
                setBody(createKnowledgeBaseRequest)
            }.body()
        } catch (exception: Exception) {
            log("createKnowledgeBase failed: ${exception.message}")
            null
        }

    suspend fun getKnowledgeBaseById(id: String, userId: String): NetworkResponse<NetworkKnowledgeBase>? = try {
        client.get("$baseUrl/kb/$id") {
            parameter("user_id", userId)
        }.body()
    } catch (exception: Exception) {
        log("getKnowledgeBaseById failed: ${exception.message}")
        null
    }

    suspend fun updateKnowledgeBase(
        id: String,
        updateKnowledgeBaseRequest: UpdateKnowledgeBaseRequest
    ): NetworkResponse<NetworkKnowledgeBase>? = try {
        client.patch("$baseUrl/kb/$id") {
            contentType(ContentType.Application.Json)
            setBody(updateKnowledgeBaseRequest)
        }.body()
    } catch (exception: Exception) {
        log("updateKnowledgeBase failed: ${exception.message}")
        null
    }

    suspend fun deleteKnowledgeBase(id: String, deleteKnowledgeBaseRequest: DeleteKnowledgeBaseRequest): Boolean = try {
        client.delete("$baseUrl/kb/$id") {
            contentType(ContentType.Application.Json)
            setBody(deleteKnowledgeBaseRequest)
        }.status == HttpStatusCode.OK
    } catch (exception: Exception) {
        log("deleteKnowledgeBase failed: ${exception.message}")
        false
    }

    suspend fun getKnowledgeBaseQa(id: String, limit: Int = 25): NetworkResponse<List<NetworkDocumentQa>>? = try {
        client.get("$baseUrl/kb/$id/qa") {
            parameter("limit", limit)
        }.body()
    } catch (exception: Exception) {
        log("getKnowledgeBaseQa failed: ${exception.message}")
        null
    }

    // Document

    suspend fun uploadDocument(
        knowledgeBaseId: String,
        tenantId: String? = null,
        fileName: String,
        mimeType: String,
        key: String = "file",
        file: ByteArray
    ): NetworkResponse<UploadDocumentData>? = try {
        client.submitFormWithBinaryData(
            url = "$baseUrl/documents/upload_and_process",
            formData = formData {
                append(
                    key = key,
                    value = file,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    }
                )
            }
        ) {
            url {
                parameter("knowledge_base_id", knowledgeBaseId)
                parameter("tenant_id", tenantId)
            }
        }.body()
    } catch (exception: Exception) {
        log("uploadDocument failed: ${exception.message}")
        null
    }

    suspend fun getDocumentStatus(id: String): NetworkResponse<GetDocumentStatusData>? = try {
        client.get("$baseUrl/documents/$id/status").body()
    } catch (exception: Exception) {
        log("getDocumentStatus failed: ${exception.message}")
        null
    }

    suspend fun getDocuments(
        knowledgeBaseId: String,
        skip: Int = 0,
        limit: Int = 10,
        createdBy: String? = null
    ): NetworkResponse<GetDocumentsData>? = try {
        client.get("$baseUrl/documents") {
            parameter("knowledge_base_id", knowledgeBaseId)
            parameter("skip", skip)
            parameter("limit", limit)
            parameter("created_by", createdBy)
        }.body()
    } catch (exception: Exception) {
        log("getDocuments failed: ${exception.message}")
        null
    }

    suspend fun getDocument(id: String): NetworkResponse<NetworkDocument>? = try {
        client.get("$baseUrl/documents/$id").body()
    } catch (exception: Exception) {
        log("getDocument failed: ${exception.message}")
        null
    }

    suspend fun deleteDocument(id: String): Boolean = try {
        client.delete("$baseUrl/documents/$id").status == HttpStatusCode.OK
    } catch (exception: Exception) {
        log("deleteDocument failed: ${exception.message}")
        false
    }

    suspend fun toggleDocumentActive(id: String, active: Boolean): NetworkResponse<NetworkDocument>? = try {
        client.patch("$baseUrl/documents/$id/active") {
            parameter("active", active)
        }.body()
    } catch (exception: Exception) {
        log("toggleDocumentActive failed: ${exception.message}")
        null
    }

    suspend fun getDocumentQa(id: String): NetworkResponse<List<NetworkDocumentQa>>? = try {
        client.get("$baseUrl/documents/$id/qa").body()
    } catch (exception: Exception) {
        log("getDocumentQa failed: ${exception.message}")
        null
    }

    // Conversation and Messages

    suspend fun getConversations(userId: String): NetworkResponse<List<NetworkConversation>>? = try {
        client.get("$baseUrl/conversation/list") {
            parameter("user_id", userId)
        }.body()
    } catch (exception: Exception) {
        log("getConversations failed: ${exception.message}")
        null
    }

    suspend fun createConversation(conversationName: String, userId: String): NetworkResponse<NetworkConversation>? =
        try {
            client.post("$baseUrl/conversation/create") {
                parameter("app_name", conversationName)
                parameter("user_id", userId)
            }.body()
        } catch (exception: Exception) {
            log("createConversation failed: ${exception.message}")
            null
        }

    suspend fun getMessagesForConversation(
        conversationId: String,
        userId: String
    ): NetworkResponse<List<NetworkMessage>>? = try {
        client.get("$baseUrl/message/list") {
            parameter("conversation_id", conversationId)
            parameter("user_id", userId)
        }.body()
    } catch (exception: Exception) {
        log("getMessagesForConversation failed: ${exception.message}")
        null
    }

    suspend fun deleteConversation(conversationId: String, userId: String): Boolean = try {
        client.delete("$baseUrl/conversation/delete") {
            parameter("conversation_id", conversationId)
            parameter("user_id", userId)
        }.status == HttpStatusCode.OK
    } catch (exception: Exception) {
        log("deleteConversation failed: ${exception.message}")
        false
    }

    suspend fun updateConversation(
        updateConversationRequest: UpdateConversationRequest
    ): NetworkResponse<NetworkConversation>? = try {
        client.patch("$baseUrl/conversation/update") {
            contentType(ContentType.Application.Json)
            setBody(updateConversationRequest)
        }.body()
    } catch (exception: Exception) {
        log("updateConversation failed: ${exception.message}")
        null
    }

    // Chat

    suspend fun ask(askRequest: AskRequest): NetworkResponse<NetworkMessage>? = try {
        client.post("$baseUrl/chat/ask") {
            contentType(ContentType.Application.Json)
            setBody(askRequest)
        }.body()
    } catch (exception: Exception) {
        log("ask failed: ${exception.message}")
        null
    }

    suspend fun askSse(
        askRequest: AskRequest,
        dispatcher: CoroutineDispatcher,
        handleEvent: suspend (ServerSentEvent) -> Unit,
        onCompletion: suspend () -> Unit
    ) = try {
        client.sse(
            urlString = "$baseUrl/chat/ask/sse",
            request = {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(askRequest)
            }
        ) {
            log("SSE connection established")
            incoming
                .flowOn(dispatcher)
                .catch { exception ->
                    log("Handle SSE failed: ${exception.message}")
                }
                .onCompletion {
                    onCompletion()
                }
                .onEach {
                    delay(50)
                }
                .collect { event ->
                    handleEvent(event)
                }
        }
    } catch (exception: Exception) {
        onCompletion()
        log("askSse failed: ${exception.message}")
    }

}
