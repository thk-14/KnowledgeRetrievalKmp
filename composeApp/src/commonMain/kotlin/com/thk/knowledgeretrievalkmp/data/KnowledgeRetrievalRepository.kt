package com.thk.knowledgeretrievalkmp.data

import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocumentsAndConversation
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import kotlinx.coroutines.flow.Flow

interface KnowledgeRetrievalRepository {
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

    suspend fun logout(): Boolean

    suspend fun refreshToken(): Boolean

    suspend fun fetchKnowledgeBasesWithDocuments(): Boolean

    suspend fun fetchKnowledgeBaseWithDocuments(kbId: String): Boolean

    suspend fun createKnowledgeBaseWithConversation(kbName: String, kbDescription: String): Boolean

    suspend fun deleteKnowledgeBaseWithConversation(kbId: String): Boolean

    suspend fun toggleKnowledgeBaseActive(kbId: String, active: Boolean): Boolean

    suspend fun renameKnowledgeBase(kbId: String, newName: String): Boolean

    suspend fun uploadDocument(
        knowledgeBaseId: String,
        tenantId: String? = null,
        fileName: String,
        mimeType: String,
        uri: String,
        file: ByteArray,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ): Boolean

    suspend fun deleteDocument(documentId: String): Boolean

    suspend fun toggleDocumentsActiveForKnowledgeBase(kbId: String, active: Boolean): Boolean

    suspend fun toggleConversationActiveForKnowledgeBase(kbId: String, active: Boolean): Boolean

    suspend fun sendUserRequest(
        kbId: String,
        conversationId: String,
        userRequest: String,
        webSearch: Boolean
    ): Boolean

    suspend fun collectSSEResponseFlow(
        kbId: String,
        conversationId: String,
        userRequest: String,
        webSearch: Boolean
    )

    // FOR LOCAL DATABASE
    suspend fun getAllKnowledgeBasesInLocalFlow(): Flow<List<KnowledgeBase>>

    suspend fun getKnowledgeBaseWithIdInLocalFlow(kbId: String): Flow<KbWithDocumentsAndConversation?>

}

