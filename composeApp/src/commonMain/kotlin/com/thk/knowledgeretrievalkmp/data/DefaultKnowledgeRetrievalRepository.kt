package com.thk.knowledgeretrievalkmp.data

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import com.thk.knowledgeretrievalkmp.Configs
import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocumentsAndConversation
import com.thk.knowledgeretrievalkmp.data.network.*
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.db.KnowledgeBaseQueries
import com.thk.knowledgeretrievalkmp.db.Message
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.Uuid

object DefaultKnowledgeRetrievalRepository : KnowledgeRetrievalRepository {

    private val sessionManager = SessionManager(AppContainer.dataStore)
    private val dispatcher = Dispatchers.Default
    private val apiService: NetworkApiService = AppContainer.apiService
    private val dbQueries: KnowledgeBaseQueries by lazy {
        AppContainer.db.knowledgeBaseQueries
    }

    override suspend fun getProfileUri() = sessionManager.getProfileUri()
    override suspend fun getDisplayName() = sessionManager.getDisplayName()
    override suspend fun getUserId(): String? = sessionManager.getUserId()

    private const val APP_NAME = "KnowledgeRetrievalApp"

    // FOR NETWORK

    override suspend fun loginWithGoogle(
        userId: String,
        displayName: String,
        profileUri: String,
        idToken: String
    ): Boolean {
        try {
            withContext(dispatcher) {
                sessionManager.apply {
                    setUserId(userId)
                    setDisplayName(displayName)
                    setProfileUri(profileUri)
                }
                // TODO send googleIdToken to server
//                val response = apiService.loginUserWithGoogle(
//                    LoginUserWithGoogleRequest(
//                        idToken = idToken
//                    )
//                )?.data ?: return@withContext
            }
            log("Login with google success")
            return true
        } catch (exception: Exception) {
            log("Login with google failed: ${exception.message}")
            return false
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        userName: String,
        fullName: String
    ): Boolean {
        val registerUserRequest = RegisterUserRequest(
            email = email,
            userName = userName,
            password = password,
            fullName = fullName
        )
        try {
            withContext(dispatcher) {
                apiService.registerUser(registerUserRequest)
            }
            log("Signup success")
            return true
        } catch (exception: Exception) {
            log("Signup failed: ${exception.message}")
            return false
        }
    }

    override suspend fun loginUser(email: String, password: String): Boolean {
        val loginUserRequest = LoginUserRequest(
            email = email,
            password = password
        )
        try {
            var succeed = false
            withContext(dispatcher) {
                val newTokens = apiService.loginUser(loginUserRequest)?.data ?: return@withContext
                sessionManager.setAccessToken(newTokens.accessToken)
                sessionManager.setRefreshToken(newTokens.refreshToken)
                sessionManager.setUserId(loginUserRequest.email)
                succeed = true
            }
            log("Login succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Login failed: ${exception.message}")
            return false
        }
    }

    override suspend fun logout(): Boolean {
        try {
            withContext(dispatcher) {
                val refreshToken = sessionManager.getRefreshToken()
                if (refreshToken != null) {
                    apiService.logoutUser(LogoutRequest(refreshToken))
                }
                sessionManager.clearSession()
            }
            log("Logout success")
            return true
        } catch (exception: Exception) {
            log("Logout failed: ${exception.message}")
            return false
        }
    }

    override suspend fun refreshToken(): Boolean {
        val refreshToken = sessionManager.getRefreshToken() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                val newTokens = apiService.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )?.data ?: return@withContext
                sessionManager.setAccessToken(newTokens.accessToken)
                sessionManager.setRefreshToken(newTokens.refreshToken)
                succeed = true
            }
            log("Refresh token succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Refresh token failed: ${exception.message}")
            sessionManager.clearSession()
            return false
        }
    }

    override suspend fun fetchKnowledgeBasesWithDocuments(): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                // fetch knowledge bases
                val networkKnowledgeBases = apiService.getKnowledgeBases(
                    GetKnowledgeBasesRequest(
                        userId = userId,
                        skip = 0,
                        limit = Configs.MAX_KB_PER_USER
                    )
                )?.data?.items ?: return@withContext
                log("get networkKnowledgeBases: $networkKnowledgeBases")
                networkKnowledgeBases.forEach { networkKnowledgeBase ->
                    // upsert knowledge base in local
                    upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase)
                    // fetch documents
                    val networkDocuments = apiService.getDocuments(
                        knowledgeBaseId = networkKnowledgeBase.id,
                        skip = 0,
                        limit = Configs.MAX_DOCUMENTS_PER_KB
                    )?.data?.items ?: return@forEach
                    log("get networkDocuments: $networkDocuments")
                    // upsert documents in local
                    networkDocuments.forEach { networkDocument ->
                        upsertNetworkDocumentInLocal(networkDocument)
                    }
                }
                // clean up kb
                val localKnowledgeBases = dbQueries.getKnowledgeBasesWithUserId(userId).awaitAsList()
                localKnowledgeBases.map { it.KbId }.forEach { kbId ->
                    if (kbId !in networkKnowledgeBases.map { it.id }) {
                        deleteKnowledgeBaseInLocal(kbId)
                    }
                }
                succeed = true
            }
            log("Fetch knowledge bases succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Fetch knowledge bases failed: ${exception.message}")
            return false
        }
    }

    override suspend fun fetchKnowledgeBaseWithDocuments(kbId: String): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                try {
                    // fetch then upsert knowledge base
                    val networkKnowledgeBase =
                        apiService.getKnowledgeBaseById(kbId)?.data ?: return@withContext
                    log("get networkKnowledgeBase: $networkKnowledgeBase")
                    upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase)
                    // fetch then upsert documents
                    val networkDocuments = apiService.getDocuments(
                        knowledgeBaseId = kbId,
                        skip = 0,
                        limit = Configs.MAX_DOCUMENTS_PER_KB
                    )?.data?.items ?: return@withContext
                    log("get networkDocuments: $networkDocuments")
                    networkDocuments.forEach { networkDocument ->
                        upsertNetworkDocumentInLocal(networkDocument)
                    }
                    // clean up documents
                    val localDocuments = dbQueries.getDocumentsWithKbId(kbId).awaitAsList()
                    localDocuments.map { it.DocumentId }.forEach { documentId ->
                        if (documentId !in networkDocuments.map { it.id }) {
                            dbQueries.deleteDocumentWithId(documentId)
                        }
                    }
                } catch (exception: Exception) {
                    // Silently failed here since we have fetched all kb and documents
                    log("Fetch knowledge base and documents failed: ${exception.message}")
                }
                // if there is no conversation tied to this kb
                val localKnowledgeBase =
                    dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()
                        ?: return@withContext
                if (localKnowledgeBase.ConversationId == null) {
                    // find related conversation in server
                    val networkConversations = apiService.getConversations(
                        userId = userId
                    )?.data
                    val relatedNetworkConversation = networkConversations?.find {
                        it.name == kbId
                    }
                    val relatedConversationId =
                        if (relatedNetworkConversation != null) {
                            log("find relatedNetworkConversation: $relatedNetworkConversation")
                            // upsert related conversation in local
                            upsertNetworkConversationInLocal(relatedNetworkConversation)
                            // fetch messages for related conversation
                            val networkMessages = apiService.getMessagesForConversation(
                                conversationId = relatedNetworkConversation.conversationId,
                                userId = userId
                            )?.data
                            // upsert messages in local
                            networkMessages?.forEach { networkMessage ->
                                upsertNetworkMessageInLocal(networkMessage)
                            }
                            relatedNetworkConversation.conversationId
                        } else {
                            log("No relatedNetworkConversation found")
                            // if not found then create new one
                            val networkConversation = apiService.createConversation(
                                conversationName = kbId,
                                userId = userId
                            )?.data ?: return@withContext
                            log("create networkConversation: $networkConversation")
                            // upsert new conversation in local
                            upsertNetworkConversationInLocal(networkConversation)
                            networkConversation.conversationId
                        }
                    // update knowledge base in local
                    upsertLocalKnowledgeBaseInLocal(
                        localKnowledgeBase.copy(ConversationId = relatedConversationId)
                    )
                } else {
                    // kb has conversation
                    if (Configs.fetchMessagesFromServer) {
                        // clear messages in local
                        dbQueries.deleteMessagesWithConversationId(localKnowledgeBase.ConversationId)
                        // fetch messages for conversation
                        val networkMessages = apiService.getMessagesForConversation(
                            conversationId = localKnowledgeBase.ConversationId,
                            userId = userId
                        )?.data
                        // upsert messages in local
                        networkMessages?.forEach { networkMessage ->
                            upsertNetworkMessageInLocal(networkMessage)
                        }
                    }
                }
                succeed = true
            }
            log("Fetch knowledge base with conversations succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Fetch knowledge base with conversations failed: ${exception.message}")
            return false
        }
    }

    override suspend fun createKnowledgeBaseWithConversation(
        kbName: String,
        kbDescription: String
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                // create knowledge base in server
                val networkKnowledgeBase = apiService.createKnowledgeBase(
                    createKnowledgeBaseRequest = CreateKnowledgeBaseRequest(
                        userId = userId,
                        name = kbName,
                        description = kbDescription
                    )
                )?.data ?: return@withContext
                upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase)
                log("create networkKnowledgeBase: $networkKnowledgeBase")
                // create a conversation corresponding to the knowledge base
                val networkConversation = apiService.createConversation(
                    conversationName = networkKnowledgeBase.id,
                    userId = userId
                )?.data ?: return@withContext
                upsertNetworkConversationInLocal(networkConversation)
                updateConversationIdForKnowledgeBaseInLocal(networkKnowledgeBase.id, networkConversation.conversationId)
                log("create networkConversation: $networkConversation")
                succeed = true
            }
            log("Create knowledge base succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Create knowledge base failed: ${exception.message}")
            return false
        }
    }

    override suspend fun deleteKnowledgeBaseWithConversation(kbId: String): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                // manually delete related conversation in server
                val localKnowledgeBase =
                    dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()
                        ?: return@withContext
                if (localKnowledgeBase.ConversationId != null) {
                    try {
                        apiService.deleteConversation(
                            conversationId = localKnowledgeBase.ConversationId,
                            userId = userId
                        )
                        deleteConversationInLocal(localKnowledgeBase.ConversationId)
                    } catch (exception: Exception) {
                        log("Delete conversation failed: ${exception.message}")
                    }
                }
                // delete knowledge base in server
                apiService.deleteKnowledgeBase(
                    id = kbId,
                    deleteKnowledgeBaseRequest = DeleteKnowledgeBaseRequest(
                        id = kbId,
                        userId = userId
                    )
                )
                deleteKnowledgeBaseInLocal(kbId)
                succeed = true
            }
            log("Delete knowledge base succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Delete knowledge base failed: ${exception.message}")
            return false
        }
    }

    override suspend fun toggleKnowledgeBaseActive(
        kbId: String,
        active: Boolean
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                val localKnowledgeBase =
                    dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()
                        ?: return@withContext
                if (localKnowledgeBase.IsActive == active) {
                    succeed = true
                    return@withContext
                }
                val updateKnowledgeBaseRequest = UpdateKnowledgeBaseRequest(
                    id = kbId,
                    userId = userId,
                    name = localKnowledgeBase.Name,
                    description = localKnowledgeBase.Description,
                    isActive = active
                )
                val networkKnowledgeBase = apiService.updateKnowledgeBase(
                    id = kbId,
                    updateKnowledgeBaseRequest = updateKnowledgeBaseRequest
                )?.data ?: return@withContext
                upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase)
                succeed = true
            }
            log("toggleKnowledgeBaseActive $active succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("toggleKnowledgeBaseActive $active failed: ${exception.message}")
            return false
        }
    }

    override suspend fun renameKnowledgeBase(
        kbId: String,
        newName: String
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                val localKnowledgeBase =
                    dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()
                        ?: return@withContext
                if (localKnowledgeBase.Name == newName) {
                    succeed = true
                    return@withContext
                }
                val updateKnowledgeBaseRequest = UpdateKnowledgeBaseRequest(
                    id = kbId,
                    userId = userId,
                    name = newName,
                    description = localKnowledgeBase.Description,
                    isActive = localKnowledgeBase.IsActive
                )
                val networkKnowledgeBase = apiService.updateKnowledgeBase(
                    id = kbId,
                    updateKnowledgeBaseRequest = updateKnowledgeBaseRequest
                )?.data ?: return@withContext
                upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase)
                log("update networkKnowledgeBase: $networkKnowledgeBase")
                succeed = true
            }
            log("Rename knowledge base succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Rename knowledge base failed: ${exception.message}")
            return false
        }
    }

    override suspend fun toggleDocumentsActiveForKnowledgeBase(
        kbId: String,
        active: Boolean
    ): Boolean {
        try {
            var succeed = false
            withContext(dispatcher) {
                val localDocuments = dbQueries.getDocumentsWithKbId(kbId).awaitAsList()
                localDocuments.forEach { localDocument ->
                    launch(dispatcher) {
                        if (localDocument.Status != NetworkDocumentStatus.FINISHED) {
                            checkDocumentStatusUntilFinished(
                                documentId = localDocument.DocumentId
                            )
                            toggleDocumentActive(
                                documentId = localDocument.DocumentId,
                                active = active
                            )
                        } else {
                            if (localDocument.IsInactive != !active) {
                                toggleDocumentActive(
                                    documentId = localDocument.DocumentId,
                                    active = active
                                )
                            }
                        }
                    }
                }
                succeed = true
            }
            log("toggleDocumentsActiveForKnowledgeBase $active succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("toggleDocumentsActiveForKnowledgeBase $active failed: ${exception.message}")
            return false
        }
    }

    override suspend fun toggleConversationActiveForKnowledgeBase(
        kbId: String,
        active: Boolean
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                val conversationId =
                    dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()?.ConversationId ?: return@withContext
                val localConversation =
                    dbQueries.getConversationWithId(conversationId).awaitAsOneOrNull() ?: return@withContext
                if (localConversation.IsActive != active) {
                    val networkConversation = apiService.updateConversation(
                        conversationId = conversationId,
                        userId = userId,
                        active = active,
                        name = localConversation.Name
                    )?.data ?: return@withContext
                    upsertNetworkConversationInLocal(networkConversation)
                }
                succeed = true
            }
            log("Set conversation for knowledge base active $active succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Set conversation for knowledge base active $active failed: ${exception.message}")
            return false
        }
    }

    override suspend fun uploadDocument(
        knowledgeBaseId: String,
        tenantId: String?,
        fileName: String,
        mimeType: String,
        uri: String,
        file: ByteArray,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ): Boolean {
        try {
            withContext(dispatcher) {
                val newDocumentData = apiService.uploadDocument(
                    knowledgeBaseId = knowledgeBaseId,
                    tenantId = tenantId,
                    fileName = fileName,
                    mimeType = mimeType,
                    file = file
                )?.data ?: return@withContext
                val localDocument = Document(
                    DocumentId = newDocumentData.id,
                    KbId = knowledgeBaseId,
                    FileName = fileName,
                    MimeType = mimeType,
                    Status = newDocumentData.status,
                    Uri = uri,
                    IsInactive = null,
                    Description = null,
                    FilePath = null,
                    FileSize = null,
                    FileType = null,
                    ProcessingError = null,
                    CreatedAt = null,
                    UpdatedAt = null,
                    UploadedBy = null,
                    ProcessedAt = null
                )
                upsertLocalDocumentInLocal(localDocument)
                withContext(Dispatchers.Main) {
                    onUploadFinish()
                }
                launch(dispatcher) {
                    // check and wait until finish processing
                    checkDocumentStatusUntilFinished(
                        documentId = newDocumentData.id
                    )
                    // set document active
                    toggleDocumentActive(
                        documentId = newDocumentData.id,
                        active = true
                    )
                }
            }
            log("Upload document finish")
            return true
        } catch (exception: Exception) {
            withContext(Dispatchers.Main) {
                onUploadFailed()
            }
            log("Upload document failed: ${exception.message}")
            return false
        }
    }

    override suspend fun deleteDocument(kbId: String, documentId: String): Boolean {
        try {
            withContext(dispatcher) {
                apiService.deleteDocument(documentId)
                dbQueries.deleteDocumentWithId(documentId)
            }
            log("Delete document success")
            return true
        } catch (exception: Exception) {
            log("Delete document failed: ${exception.message}")
            return false
        }
    }

    override suspend fun sendUserRequest(
        kbId: String,
        conversationId: String,
        userRequest: String,
        webSearch: Boolean
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        try {
            var succeed = false
            withContext(dispatcher) {
                val requestNetworkMessage = NetworkMessage(
                    id = Uuid.random().toString(),
                    role = NetworkMessageRole.USER,
                    parts = listOf(
                        NetworkPartText(
                            text = userRequest
                        )
                    ),
                    metadata = NetworkMessageMetadata(
                        conversationId = conversationId,
                        kbId = kbId,
                        userId = userId,
                        appName = APP_NAME
                    )
                )
                upsertNetworkMessageInLocal(requestNetworkMessage)
                val askRequest = AskRequest(
                    message = requestNetworkMessage,
                    agentic = true,
                    webSearch = webSearch
                )
                val networkMessage = apiService.ask(askRequest)?.data ?: return@withContext
                upsertNetworkMessageInLocal(networkMessage)
                log("get networkMessage: $networkMessage")
                succeed = true
            }
            log("Send user request succeed: $succeed")
            return succeed
        } catch (exception: Exception) {
            log("Send user request failed: ${exception.message}")
            return false
        }
    }

    override suspend fun collectSSEResponseFlow(
        kbId: String,
        conversationId: String,
        userRequest: String,
        webSearch: Boolean
    ) {
        val userId = sessionManager.getUserId() ?: return
        val requestNetworkMessage = NetworkMessage(
            id = Uuid.random().toString(),
            role = NetworkMessageRole.USER,
            parts = listOf(
                NetworkPartText(
                    text = userRequest,
                )
            ),
            metadata = NetworkMessageMetadata(
                conversationId = conversationId,
                kbId = kbId,
                userId = userId,
                appName = APP_NAME
            )
        )
        withContext(dispatcher) {
            upsertNetworkMessageInLocal(requestNetworkMessage)
        }
        var content = ""
        val responseLocalMessage = Message(
            MessageId = Uuid.random().toString(),
            ConversationId = conversationId,
            KbId = kbId,
            UserId = userId,
            Role = NetworkMessageRole.AGENT,
            Content = content,
            CreatedAt = Clock.System.now().toEpochMilliseconds()
        )
        withContext(dispatcher) {
            upsertLocalMessageInLocal(responseLocalMessage)
        }
        apiService.askSse(
            askRequest = AskRequest(
                message = requestNetworkMessage,
                agentic = true,
                webSearch = webSearch
            ),
            dispatcher = dispatcher,
            handleEvent = handleSseEvent@{ serverSentEvent ->
                if (serverSentEvent.event == "content_block_delta") {
                    val dataString = serverSentEvent.data ?: return@handleSseEvent
                    val data = Json.decodeFromString<SseContentData>(dataString)
                    content += data.delta.text
                    upsertLocalMessageInLocal(responseLocalMessage.copy(Content = content))
                }
            }
        )
    }

    private suspend fun checkDocumentStatusUntilFinished(
        documentId: String,
        checkInterval: Long = 1000
    ) {
        try {
            val localDocument = dbQueries.getDocumentWithId(documentId).awaitAsOneOrNull() ?: return
            if (localDocument.Status == NetworkDocumentStatus.FINISHED) return
            while (true) {
                val status = apiService.getDocumentStatus(documentId)?.data?.status?.apply {
                    upsertLocalDocumentInLocal(localDocument.copy(Status = this))
                }
                if (status == NetworkDocumentStatus.FINISHED) break
                delay(checkInterval)
            }
            // fetch full document detail
            val networkDocument = apiService.getDocument(documentId)?.data ?: return
            upsertNetworkDocumentInLocal(networkDocument)
            log("checkDocumentStatusUntilFinished $documentId success")
        } catch (exception: Exception) {
            log("checkDocumentStatusUntilFinished $documentId failed: ${exception.message}")
        }
    }

    private suspend fun toggleDocumentActive(
        documentId: String,
        active: Boolean
    ) {
        try {
            val networkDocument = apiService.toggleDocumentActive(
                id = documentId,
                active = active
            )?.data ?: return
            upsertNetworkDocumentInLocal(networkDocument)
            log("toggleDocumentActive $documentId $active success")
        } catch (exception: Exception) {
            log("toggleDocumentActive $documentId $active failed: ${exception.message}")
        }
    }

    // FOR LOCAL DATABASE

    override suspend fun getAllKnowledgeBasesInLocalFlow(): Flow<List<KnowledgeBase>> {
        return dbQueries.getKnowledgeBasesWithUserId(
            sessionManager.getUserId() ?: ""
        ).asFlow().map { it.awaitAsList() }.flowOn(dispatcher)
    }

    override suspend fun getKnowledgeBaseWithIdInLocalFlow(kbId: String): Flow<KbWithDocumentsAndConversation?> {
        return dbQueries.getKnowledgeBaseWithId(kbId)
            .asFlow().map { kbQuery ->
                val kb = kbQuery.awaitAsOneOrNull() ?: return@map null
                val documents = dbQueries.getDocumentsWithKbId(kbId).awaitAsList()
                val conversation =
                    if (kb.ConversationId == null) null else dbQueries.getConversationWithId(kb.ConversationId)
                        .awaitAsOneOrNull()
                KbWithDocumentsAndConversation(
                    kb = kb,
                    documents = documents,
                    conversation = conversation
                )
            }.flowOn(dispatcher)
    }

    private suspend fun upsertLocalKnowledgeBaseInLocal(localKnowledgeBase: KnowledgeBase) {
        dbQueries.upsertKnowledgeBase(
            kbId = localKnowledgeBase.KbId,
            userId = localKnowledgeBase.UserId,
            name = localKnowledgeBase.Name,
            description = localKnowledgeBase.Description,
            createdAt = localKnowledgeBase.CreatedAt,
            updatedAt = localKnowledgeBase.UpdatedAt,
            isActive = localKnowledgeBase.IsActive,
            documentCount = localKnowledgeBase.DocumentCount,
            conversationId = localKnowledgeBase.ConversationId
        )
    }

    private suspend fun upsertLocalDocumentInLocal(localDocument: Document) {
        dbQueries.upsertDocument(
            documentId = localDocument.DocumentId,
            kbId = localDocument.KbId,
            fileName = localDocument.FileName,
            description = localDocument.Description,
            filePath = localDocument.FilePath,
            fileSize = localDocument.FileSize,
            fileType = localDocument.FileType,
            mimeType = localDocument.MimeType,
            status = localDocument.Status,
            processingError = localDocument.ProcessingError,
            createdAt = localDocument.CreatedAt,
            updatedAt = localDocument.UpdatedAt,
            uploadedBy = localDocument.UploadedBy,
            processedAt = localDocument.ProcessedAt,
            isInactive = localDocument.IsInactive,
            uri = localDocument.Uri
        )
    }

    private suspend fun upsertLocalMessageInLocal(localMessage: Message) {
        dbQueries.upsertMessage(
            messageId = localMessage.MessageId,
            conversationId = localMessage.ConversationId,
            kbId = localMessage.KbId,
            userId = localMessage.UserId,
            role = localMessage.Role,
            content = localMessage.Content,
            createdAt = localMessage.CreatedAt
        )
    }

    private suspend fun updateConversationIdForKnowledgeBaseInLocal(kbId: String, conversationId: String) {
        val localKnowledgeBase = dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull() ?: return
        upsertLocalKnowledgeBaseInLocal(
            localKnowledgeBase.copy(ConversationId = conversationId)
        )
    }

    private suspend fun upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase: NetworkKnowledgeBase) {
        val conversationId =
            dbQueries.getKnowledgeBaseWithId(networkKnowledgeBase.id).awaitAsOneOrNull()?.ConversationId
        dbQueries.upsertKnowledgeBase(
            kbId = networkKnowledgeBase.id,
            userId = networkKnowledgeBase.userId,
            name = networkKnowledgeBase.name,
            description = networkKnowledgeBase.description,
            createdAt = networkKnowledgeBase.createdAt,
            updatedAt = networkKnowledgeBase.updatedAt,
            isActive = networkKnowledgeBase.isActive,
            documentCount = networkKnowledgeBase.documentCount,
            conversationId = conversationId
        )
    }

    private suspend fun upsertNetworkDocumentInLocal(networkDocument: NetworkDocument) {
        val localUri = dbQueries.getDocumentWithId(networkDocument.id).awaitAsOneOrNull()?.Uri
        dbQueries.upsertDocument(
            documentId = networkDocument.id,
            kbId = networkDocument.knowledgeBaseId,
            fileName = networkDocument.originalFilename,
            description = networkDocument.description,
            filePath = networkDocument.filePath,
            fileSize = networkDocument.fileSize,
            fileType = networkDocument.fileType,
            mimeType = networkDocument.mimeType,
            status = networkDocument.status,
            processingError = networkDocument.processingError,
            createdAt = networkDocument.createdAt,
            updatedAt = networkDocument.updatedAt,
            uploadedBy = networkDocument.uploadedBy,
            processedAt = networkDocument.processedAt,
            isInactive = networkDocument.isInactive,
            uri = localUri
        )
    }

    private suspend fun upsertNetworkConversationInLocal(networkConversation: NetworkConversation) {
        dbQueries.upsertConversation(
            conversationId = networkConversation.conversationId,
            name = networkConversation.name,
            isActive = networkConversation.isActive
        )
    }

    private suspend fun upsertNetworkMessageInLocal(networkMessage: NetworkMessage) {
        dbQueries.upsertMessage(
            messageId = networkMessage.id,
            conversationId = networkMessage.metadata.conversationId,
            kbId = networkMessage.metadata.kbId,
            userId = networkMessage.metadata.userId,
            role = networkMessage.role,
            content = networkMessage.parts.firstOrNull()?.text ?: "",
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    private suspend fun deleteKnowledgeBaseInLocal(kbId: String) {
        val localKnowledgeBase =
            dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull() ?: return
        // delete conversation and messages
        if (localKnowledgeBase.ConversationId != null) {
            dbQueries.deleteConversationWithId(localKnowledgeBase.ConversationId)
            dbQueries.deleteMessagesWithConversationId(localKnowledgeBase.ConversationId)
        }
        // delete documents
        dbQueries.deleteDocumentsWithKbId(kbId)
        // delete knowledge base
        dbQueries.deleteKnowledgeBaseWithId(kbId)
    }

    private suspend fun deleteConversationInLocal(conversationId: String) {
        // delete conversation and messages
        dbQueries.deleteConversationWithId(conversationId)
        dbQueries.deleteMessagesWithConversationId(conversationId)
        // update related kb
        val localKnowledgeBase =
            dbQueries.getKnowledgeBaseWithConversationId(conversationId).awaitAsOneOrNull() ?: return
        dbQueries.upsertKnowledgeBase(
            kbId = localKnowledgeBase.KbId,
            userId = localKnowledgeBase.UserId,
            name = localKnowledgeBase.Name,
            description = localKnowledgeBase.Description,
            createdAt = localKnowledgeBase.CreatedAt,
            updatedAt = localKnowledgeBase.UpdatedAt,
            isActive = localKnowledgeBase.IsActive,
            documentCount = localKnowledgeBase.DocumentCount,
            conversationId = null
        )
    }
}