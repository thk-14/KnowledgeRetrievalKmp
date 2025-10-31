package com.thk.knowledgeretrievalkmp.data

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.thk.knowledgeretrievalkmp.Configs
import com.thk.knowledgeretrievalkmp.data.local.db.ConversationWithMessages
import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocumentsAndConversation
import com.thk.knowledgeretrievalkmp.data.network.*
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.db.KnowledgeBaseQueries
import com.thk.knowledgeretrievalkmp.db.Message
import com.thk.knowledgeretrievalkmp.util.generateV7
import com.thk.knowledgeretrievalkmp.util.log
import com.thk.knowledgeretrievalkmp.util.toSseEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

object DefaultKnowledgeRetrievalRepository : KnowledgeRetrievalRepository {

    private val sessionManager = AppContainer.sessionManager
    private val apiService: NetworkApiService = AppContainer.apiService
    private val dbQueries: KnowledgeBaseQueries by lazy {
        AppContainer.db.knowledgeBaseQueries
    }
    private val dispatcher = Dispatchers.Default

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
        withContext(dispatcher) {
            sessionManager.apply {

                // FOR TESTING
                setUserId("hafizh")
                // END FOR TESTING

//                setUserId(userId)
                setDisplayName(displayName)
                setProfileUri(profileUri)
            }
            // TODO send googleIdToken to server
//                val response = apiService.loginUserWithGoogle(
//                    LoginUserWithGoogleRequest(
//                        idToken = idToken
//                    )
//                )?.data ?: return@withContext false
        }
        log("loginWithGoogle success")
        return true
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
        var succeed = false
        withContext(dispatcher) {
            val response = apiService.registerUser(registerUserRequest)?.data ?: return@withContext
            succeed = true
        }
        log("registerUser succeed: $succeed")
        return succeed
    }

    override suspend fun loginUser(email: String, password: String): Boolean {
        val loginUserRequest = LoginUserRequest(
            email = email,
            password = password,
            remember = false
        )
        var succeed = false
        withContext(dispatcher) {
            val newTokens = apiService.loginUser(loginUserRequest)?.data ?: return@withContext
            sessionManager.setAccessToken(newTokens.accessToken)
            sessionManager.setRefreshToken(newTokens.refreshToken)
            sessionManager.setUserId(loginUserRequest.email)
            succeed = true
        }
        log("loginUser succeed: $succeed")
        return succeed
    }

    override suspend fun logout(): Boolean {
        withContext(dispatcher) {
            val refreshToken = sessionManager.getRefreshToken()
            if (refreshToken != null) {
                val response = apiService.logoutUser(LogoutRequest(refreshToken))?.data
                log("logout response: $response")
            }
            sessionManager.clearSession()
        }
        return true
    }

    override suspend fun refreshToken(): Boolean {
        val refreshToken = sessionManager.getRefreshToken() ?: return false
        var succeed = false
        withContext(dispatcher) {
            val newTokens = apiService.refreshToken(
                RefreshTokenRequest(refreshToken)
            )?.data ?: return@withContext
            sessionManager.setAccessToken(newTokens.accessToken)
            sessionManager.setRefreshToken(newTokens.refreshToken)
            succeed = true
        }
        log("refreshToken succeed: $succeed")
        return succeed
    }

    override suspend fun fetchKnowledgeBasesWithDocuments(): Boolean {
        val userId = sessionManager.getUserId() ?: return false
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
    }

    override suspend fun fetchKnowledgeBaseWithDocuments(kbId: String): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        var succeed = false
        withContext(dispatcher) {
            // fetch then upsert knowledge base
            val networkKnowledgeBase =
                apiService.getKnowledgeBaseById(kbId, userId)?.data ?: return@withContext
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
                            upsertNetworkMessageInLocal(
                                networkMessage.copy(
                                    metadata = networkMessage.metadata.copy(
                                        conversationId = relatedNetworkConversation.conversationId,
                                        kbId = kbId,
                                        userId = userId
                                    )
                                )
                            )
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
                        upsertNetworkMessageInLocal(
                            networkMessage.copy(
                                metadata = networkMessage.metadata.copy(
                                    conversationId = localKnowledgeBase.ConversationId,
                                    kbId = kbId,
                                    userId = userId
                                )
                            )
                        )
                    }
                }
            }
            succeed = true
        }
        log("Fetch knowledge base with conversations succeed: $succeed")
        return succeed
    }

    override suspend fun createKnowledgeBaseWithConversation(
        kbName: String,
        kbDescription: String
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
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
    }

    override suspend fun deleteKnowledgeBaseWithConversation(kbId: String): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        var succeed = false
        withContext(dispatcher) {
            // manually delete related conversation in server
            val localKnowledgeBase =
                dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()
                    ?: return@withContext
            if (localKnowledgeBase.ConversationId != null) {
                succeed = apiService.deleteConversation(
                    conversationId = localKnowledgeBase.ConversationId,
                    userId = userId
                )
                if (succeed) {
                    deleteConversationInLocal(localKnowledgeBase.ConversationId)
                }
            }
            // delete knowledge base in server
            succeed = apiService.deleteKnowledgeBase(
                id = kbId,
                deleteKnowledgeBaseRequest = DeleteKnowledgeBaseRequest(
                    id = kbId,
                    userId = userId
                )
            )
            if (succeed) {
                deleteKnowledgeBaseInLocal(kbId)
            }
        }
        log("Delete knowledge base succeed: $succeed")
        return succeed
    }

    override suspend fun toggleKnowledgeBaseActive(
        kbId: String,
        active: Boolean
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
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
    }

    override suspend fun renameKnowledgeBase(
        kbId: String,
        newName: String
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
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
    }

    override suspend fun toggleDocumentsActiveForKnowledgeBase(
        kbId: String,
        active: Boolean
    ): Boolean {
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
    }

    override suspend fun toggleConversationActiveForKnowledgeBase(
        kbId: String,
        active: Boolean
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
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
    }

    override suspend fun uploadDocument(
        knowledgeBaseId: String,
        tenantId: String?,
        fileName: String,
        mimeType: String,
        file: ByteArray,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ): Boolean {
        withContext(dispatcher) {
            val newDocumentData = apiService.uploadDocument(
                knowledgeBaseId = knowledgeBaseId,
                tenantId = tenantId,
                fileName = fileName,
                mimeType = mimeType,
                file = file
            )?.data
            if (newDocumentData == null) {
                withContext(Dispatchers.Main) {
                    onUploadFailed()
                }
                return@withContext
            }
            val localDocument = Document(
                DocumentId = newDocumentData.id,
                KbId = knowledgeBaseId,
                FileName = fileName,
                MimeType = mimeType,
                Status = newDocumentData.status,
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
    }

    override suspend fun deleteDocument(documentId: String): Boolean {
        var succeed = false
        withContext(dispatcher) {
            succeed = apiService.deleteDocument(documentId)
            if (succeed) {
                dbQueries.deleteDocumentWithId(documentId)
            }
        }
        log("deleteDocument succeed: $succeed")
        return true
    }

    override suspend fun sendUserRequest(
        kbId: String,
        conversationId: String,
        userRequest: String,
        webSearch: Boolean
    ): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        var succeed = false
        withContext(dispatcher) {
            val latestMessageOrder =
                dbQueries.getLatestMessageInConversation(conversationId).awaitAsOneOrNull()?.MessageOrder ?: 0
            val requestNetworkMessage = NetworkMessage(
                id = Uuid.generateV7().toString(),
                role = NetworkMessageRole.USER,
                parts = listOf(
                    NetworkPartText(
                        type = "text",
                        text = userRequest
                    )
                ),
                metadata = NetworkMessageMetadata(
                    conversationId = conversationId,
                    kbId = kbId,
                    userId = userId,
                    appName = APP_NAME
                ),
//                retrievalContext = null
            )
            upsertNetworkMessageInLocal(
                requestNetworkMessage.copy(
                    metadata = requestNetworkMessage.metadata.copy(
                        messageOrder = latestMessageOrder + 1
                    )
                )
            )
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
    }

    override suspend fun collectSSEResponseFlow(
        kbId: String,
        conversationId: String,
        userRequest: String,
        webSearch: Boolean,
        onSseData: (SseData) -> Unit
    ) {
        val userId = sessionManager.getUserId() ?: return
        val latestMessageOrder =
            dbQueries.getLatestMessageInConversation(conversationId).awaitAsOneOrNull()?.MessageOrder ?: 0
        val requestNetworkMessage = NetworkMessage(
            id = Uuid.generateV7().toString(),
            role = NetworkMessageRole.USER,
            parts = listOf(
                NetworkPartText(
                    type = "text",
                    text = userRequest
                )
            ),
            metadata = NetworkMessageMetadata(
                conversationId = conversationId,
                kbId = kbId,
                userId = userId,
                appName = APP_NAME
            ),
//            retrievalContext = null
        )
        withContext(dispatcher) {
            upsertNetworkMessageInLocal(
                requestNetworkMessage.copy(
                    metadata = requestNetworkMessage.metadata.copy(
                        messageOrder = latestMessageOrder + 1
                    )
                )
            )
        }
        var content = ""
        var status = ""
        val responseLocalMessage = Message(
            MessageId = Uuid.generateV7().toString(),
            ConversationId = conversationId,
            KbId = kbId,
            UserId = userId,
            Role = NetworkMessageRole.AGENT,
            Content = content,
            Status = status,
            MessageOrder = latestMessageOrder + 2
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
            onCompletion = {
                if (content.isEmpty()) {
                    dbQueries.deleteMessageWithId(responseLocalMessage.MessageId)
                }
            },
            handleEvent = handleSseEvent@{ serverSentEvent ->
                log("serverSentEvent: $serverSentEvent")
                when (serverSentEvent.event?.toSseEvent()) {
                    SseEvent.START -> {
                        val dataString = serverSentEvent.data ?: return@handleSseEvent
                        val data = Json.decodeFromString<SseStartData>(dataString)
                        log("data: $data")
                        onSseData(data)
                    }

                    SseEvent.STATUS -> {
                        val dataString = serverSentEvent.data ?: return@handleSseEvent
                        val data = Json.decodeFromString<SseStatusData>(dataString)
                        log("data: $data")
                        if (data.phase == "analysis") {
                            status = "Analyzing ..."
                        } else {
                            status = data.message
                        }
                        updateMessageStatusInLocal(
                            messageId = responseLocalMessage.MessageId,
                            status = status
                        )
                        onSseData(data)
                    }

                    SseEvent.CONTENT -> {
                        val dataString = serverSentEvent.data ?: return@handleSseEvent
                        val data = Json.decodeFromString<SseContentData>(dataString)
                        content += data.delta.text
                        updateMessageContentInLocal(
                            messageId = responseLocalMessage.MessageId,
                            content = content
                        )
                        onSseData(data)
                    }

                    SseEvent.STOP -> {
                        val dataString = serverSentEvent.data ?: return@handleSseEvent
                        val data = Json.decodeFromString<SseStopData>(dataString)
                        log("data: $data")
                        onSseData(data)
                    }

                    null -> {
                        log("Invalid Sse event: ${serverSentEvent.event}")
                    }
                }
            }
        )
    }

    private suspend fun checkDocumentStatusUntilFinished(
        documentId: String,
        checkInterval: Long = 5000
    ) {
        val localDocument = dbQueries.getDocumentWithId(documentId).awaitAsOneOrNull() ?: return
        if (localDocument.Status == NetworkDocumentStatus.FINISHED) return
        while (true) {
            val status = apiService.getDocumentStatus(documentId)?.data?.status?.apply {
                upsertLocalDocumentInLocal(localDocument.copy(Status = this))
            }
            when (status) {
                NetworkDocumentStatus.FINISHED -> {
                    log("$documentId status: $status")
                    // fetch full document detail
                    val networkDocument = apiService.getDocument(documentId)?.data ?: return
                    upsertNetworkDocumentInLocal(networkDocument)
                    break
                }

                NetworkDocumentStatus.TIMEOUT, NetworkDocumentStatus.FAILED -> {
                    log("$documentId status: $status")
                    deleteDocument(documentId)
                    break
                }

                else -> {
                    log("$documentId status: $status")
                }
            }
            delay(checkInterval)
        }
        log("checkDocumentStatusUntilFinished $documentId success")
    }

    private suspend fun toggleDocumentActive(
        documentId: String,
        active: Boolean
    ) {
        val networkDocument = apiService.toggleDocumentActive(
            id = documentId,
            active = active
        )?.data ?: return
        upsertNetworkDocumentInLocal(networkDocument)
        log("toggleDocumentActive $documentId $active success")
    }

    // FOR LOCAL DATABASE

    override suspend fun getAllKnowledgeBasesInLocalFlow(): Flow<List<KnowledgeBase>> {
        return dbQueries.getKnowledgeBasesWithUserId(
            sessionManager.getUserId() ?: ""
        ).asFlow().mapToList(dispatcher).flowOn(dispatcher)
    }

    override suspend fun getKnowledgeBaseWithIdInLocalFlow(kbId: String): Flow<KbWithDocumentsAndConversation?> {
        val conversationId = dbQueries.getKnowledgeBaseWithId(kbId).awaitAsOneOrNull()?.ConversationId ?: ""
        log("getKnowledgeBaseWithIdInLocalFlow conversationId: $conversationId")
        val kbFlow = dbQueries.getKnowledgeBaseWithId(kbId).asFlow().mapToOneOrNull(dispatcher)
        val documentsFlow = dbQueries.getDocumentsWithKbId(kbId).asFlow().mapToList(dispatcher)
        val conversationFlow = dbQueries.getConversationWithId(conversationId).asFlow().mapToOneOrNull(dispatcher)
        val messagesFlow = dbQueries.getMessagesWithConversationId(conversationId).asFlow().mapToList(dispatcher)

        return combine(kbFlow, documentsFlow, conversationFlow, messagesFlow) { kb, documents, conversation, messages ->
            if (kb == null)
                null
            else
                KbWithDocumentsAndConversation(
                    kb = kb,
                    documents = documents,
                    conversation = conversation?.let {
                        ConversationWithMessages(
                            conversation = it,
                            messages = messages.toMutableList()
                        )
                    }
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
            isInactive = localDocument.IsInactive
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
            status = localMessage.Status,
            messageOrder = localMessage.MessageOrder
        )
    }

    private suspend fun updateMessageContentInLocal(messageId: String, content: String) {
        dbQueries.updateMessageContent(
            messageId = messageId,
            content = content
        )
    }

    private suspend fun updateMessageStatusInLocal(messageId: String, status: String) {
        dbQueries.updateMessageStatus(
            messageId = messageId,
            status = status
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
            isInactive = networkDocument.isInactive
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
            status = "",
            messageOrder = networkMessage.metadata.messageOrder ?: 0
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