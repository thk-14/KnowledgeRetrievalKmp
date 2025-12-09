package com.thk.knowledgeretrievalkmp.data

import com.thk.knowledgeretrievalkmp.data.local.db.ConversationWithMessages
import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocuments
import com.thk.knowledgeretrievalkmp.data.network.SseData
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface KnowledgeRetrievalRepository {
    var isDataFetched: Boolean
    suspend fun getProfileUri(): String?
    suspend fun getDisplayName(): String?
    suspend fun getUserId(): String?

    // FOR NETWORK
    suspend fun registerUser(
        email: String,
        password: String,
        userName: String,
        fullName: String
    ): Boolean

    suspend fun loginUser(email: String, password: String): Boolean

    suspend fun loginWithGoogle(
        userId: String,
        displayName: String,
        profileUri: String,
        idToken: String
    ): Boolean

    fun loginGoogleWithServer(onLoginFinish: (Boolean) -> Unit)

    suspend fun exchangeGoogleAuthCode(code: String?)

    suspend fun logout(): Boolean

    suspend fun refreshToken(): Boolean

    suspend fun fetchKnowledgeBasesWithDocuments(): Boolean

    suspend fun fetchKnowledgeBaseWithDocuments(kbId: String): Boolean

    fun fetchConversationsWithMessagesGlobally()

    suspend fun createKnowledgeBase(kbName: String, kbDescription: String): Boolean

    suspend fun deleteKnowledgeBase(kbId: String): Boolean

    suspend fun toggleKnowledgeBaseActive(kbId: String, active: Boolean): Boolean

    suspend fun renameKnowledgeBase(kbId: String, newName: String): Boolean

    suspend fun uploadDocument(
        knowledgeBaseId: String,
        tenantId: String? = null,
        fileName: String,
        mimeType: String,
        file: ByteArray,
        onUpload: (Float) -> Unit,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ): Boolean

    suspend fun deleteDocument(documentId: String): Boolean

    suspend fun toggleDocumentsActiveForKnowledgeBase(kbId: String, active: Boolean): Boolean

    /**
     * @return conversation Id if success, else null
     */
    suspend fun createConversation(conversationName: String): String?

    suspend fun deleteConversation(conversationId: String): Boolean

    suspend fun renameConversation(conversationId: String, newName: String): Boolean

    suspend fun toggleConversationActive(conversationId: String, active: Boolean): Boolean

    suspend fun sendUserRequest(
        kbId: String,
        conversationId: String,
        userRequest: String,
        agentic: Boolean
    ): Boolean

    suspend fun collectSSEResponseFlow(
        kbId: String,
        conversationId: String,
        userRequest: String,
        agentic: Boolean,
        onSseData: (SseData) -> Unit,
        onCompletion: (Duration) -> Unit
    )

    // FOR LOCAL DATABASE
    suspend fun getKnowledgeBasesInLocalFlow(): Flow<List<KbWithDocuments>>

    suspend fun getKnowledgeBaseWithIdInLocalFlow(kbId: String): Flow<KbWithDocuments?>

    suspend fun getConversationsInLocalFlow(): Flow<List<ConversationWithMessages>>

}

