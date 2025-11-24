package com.thk.knowledgeretrievalkmp.data

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.thk.knowledgeretrievalkmp.Configs
import com.thk.knowledgeretrievalkmp.data.local.db.ConversationWithMessages
import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocuments
import com.thk.knowledgeretrievalkmp.data.local.db.MessageWithCitations
import com.thk.knowledgeretrievalkmp.data.network.*
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.db.KnowledgeBaseQueries
import com.thk.knowledgeretrievalkmp.db.Message
import com.thk.knowledgeretrievalkmp.util.generateV7
import com.thk.knowledgeretrievalkmp.util.log
import com.thk.knowledgeretrievalkmp.util.titlecase
import com.thk.knowledgeretrievalkmp.util.toSseEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.measureTime
import kotlin.uuid.Uuid

object DefaultKnowledgeRetrievalRepository : KnowledgeRetrievalRepository {
    private val sessionManager = AppContainer.sessionManager
    private val apiService: NetworkApiService = AppContainer.apiService
    private val dbQueries: KnowledgeBaseQueries by lazy {
        AppContainer.db.knowledgeBaseQueries
    }
    private val dispatcher = Dispatchers.Default
    private val coroutineScope = CoroutineScope(
        dispatcher + SupervisorJob() + CoroutineName("KbCoroutine")
    )
    private var initLoginJob: Job? = null
    private var codeExchangeJob: Job? = null
    private var codeExchangeChannel = Channel<String?>().apply { close() }

    override var isDataFetched: Boolean
        get() = sessionManager.isDataFetched
        set(value) {
            sessionManager.isDataFetched = value
        }

    override suspend fun getProfileUri() = sessionManager.getProfileUri()
    override suspend fun getDisplayName() = sessionManager.getDisplayName()
    override suspend fun getUserId(): String? = sessionManager.getUserId()

    private const val APP_NAME = "KMS"

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
                // END TESTING

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

    override fun loginGoogleWithServer(onLoginFinish: (Boolean) -> Unit) {
        if (!codeExchangeChannel.isClosedForSend) {
            // channel is waiting for code exchange
            log("codeExchangeChannel is not closed")
            return
        }
        initLoginJob = coroutineScope.launch {
            initiateLogin()
        }
        codeExchangeJob = coroutineScope.launch {
            var succeed = false
            codeExchangeChannel = Channel()
            val code = codeExchangeChannel.receive()
            log("Google Auth code received: $code")
            codeExchangeChannel.close()
            if (code != null) {
                succeed = handelCodeExchange(code)
            }
            onLoginFinish(succeed)
            log("loginGoogleWithServer succeed: $succeed")
        }
    }

    private suspend fun handelCodeExchange(code: String): Boolean {
        log("handelCodeExchange code: $code")
        val authenticationData = apiService.exchangeGoogleAuthCode(
            ExchangeGoogleAuthCodeRequest(code)
        )?.data ?: return false
        sessionManager.setAccessToken(authenticationData.accessToken)
        sessionManager.setRefreshToken(authenticationData.refreshToken)
        sessionManager.setUserId(authenticationData.userId ?: "")
        log("handelCodeExchange success")
        return true
    }

    override suspend fun exchangeGoogleAuthCode(code: String?) {
        log("is channel closed: ${codeExchangeChannel.isClosedForSend}")
        withContext(dispatcher) {
            if (!codeExchangeChannel.isClosedForSend) {
                // channel is waiting for code exchange
                codeExchangeChannel.send(code)
                log("Google Auth code sent")
                codeExchangeJob?.join()
                initLoginJob?.cancel()
            } else {
                // channel is closed, self-handle
                if (code != null) {
                    handelCodeExchange(code)
                }
            }
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
                // clean up documents
                val localDocuments = dbQueries.getDocumentsWithKbId(networkKnowledgeBase.id).awaitAsList()
                localDocuments.map { it.DocumentId }.forEach { documentId ->
                    if (documentId !in networkDocuments.map { it.id }) {
                        dbQueries.deleteDocumentWithId(documentId)
                    }
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
            succeed = true
        }
        log("Fetch knowledge base with documents succeed: $succeed")
        return succeed
    }

    override suspend fun fetchConversationsWithMessages(): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        var succeed = false
        withContext(dispatcher) {
            // fetch conversations
            val networkConversations = apiService.getConversations(userId)?.data ?: return@withContext
            log("get networkConversations: $networkConversations")
            networkConversations.forEach { networkConversation ->
                // upsert networkConversation in local
                upsertNetworkConversationInLocal(networkConversation)
                // fetch messages
                val networkMessages =
                    apiService.getMessagesForConversation(networkConversation.conversationId, userId)?.data
                        ?: return@forEach
                log("get networkMessages: $networkMessages")
                // clear messages in local
                clearConversationInLocal(networkConversation.conversationId)
                // upsert messages in local
                networkMessages.forEach { networkMessage ->
                    upsertNetworkMessageInLocal(
                        networkMessage.copy(
                            metadata = networkMessage.metadata.copy(
                                userId = userId,
                                conversationId = networkConversation.conversationId
                            )
                        )
                    )
                }
            }
            // clean up conversations
            val localConversations = dbQueries.getConversationsWithuserId(userId).awaitAsList()
            localConversations.map { it.ConversationId }.forEach { conversationId ->
                if (conversationId !in networkConversations.map { it.conversationId }) {
                    deleteConversationInLocal(conversationId)
                }
            }
            succeed = true
        }
        log("Fetch conversations succeed: $succeed")
        return succeed
    }

    override suspend fun createKnowledgeBase(
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
            succeed = true
        }
        log("Create knowledge base succeed: $succeed")
        return succeed
    }

    override suspend fun deleteKnowledgeBase(kbId: String): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        var succeed = false
        withContext(dispatcher) {
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

    override suspend fun uploadDocument(
        knowledgeBaseId: String,
        tenantId: String?,
        fileName: String,
        mimeType: String,
        file: ByteArray,
        onUpload: (Float) -> Unit,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ): Boolean {
        withContext(dispatcher) {
            val newDocumentData = apiService.uploadDocument(
                knowledgeBaseId = knowledgeBaseId,
                tenantId = tenantId,
                fileName = fileName,
                mimeType = mimeType,
                file = file,
                onUpload = onUpload
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
            upsertLocalDocument(localDocument)
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

    override suspend fun createConversation(conversationName: String): String? {
        val userId = sessionManager.getUserId() ?: return null
        var conversationId: String? = null
        withContext(dispatcher) {
            val networkConversation =
                apiService.createConversation(conversationName, userId)?.data ?: return@withContext
            upsertNetworkConversationInLocal(networkConversation)
            conversationId = networkConversation.conversationId
        }
        log("createConversation conversationId: $conversationId")
        return conversationId
    }

    override suspend fun deleteConversation(conversationId: String): Boolean {
        val userId = sessionManager.getUserId() ?: return false
        var succeed = false
        withContext(dispatcher) {
            succeed = apiService.deleteConversation(conversationId, userId)
            if (succeed) {
                deleteConversationInLocal(conversationId)
            }
        }
        log("deleteConversation succeed: $succeed")
        return succeed
    }

    override suspend fun renameConversation(conversationId: String, newName: String): Boolean {
        var succeed = false
        withContext(dispatcher) {
            val localConversation =
                dbQueries.getConversationWithId(conversationId).awaitAsOneOrNull() ?: return@withContext
            if (localConversation.Name == newName) {
                succeed = true
                return@withContext
            }
            val networkConversation = apiService.updateConversation(
                UpdateConversationRequest(
                    conversationId = conversationId,
                    isActive = localConversation.IsActive,
                    name = newName
                )
            )?.data ?: return@withContext
            upsertNetworkConversationInLocal(networkConversation)
            succeed = true
        }
        log("renameConversation succeed: $succeed")
        return succeed
    }

    override suspend fun toggleConversationActive(conversationId: String, active: Boolean): Boolean {
        var succeed = false
        withContext(dispatcher) {
            val localConversation =
                dbQueries.getConversationWithId(conversationId).awaitAsOneOrNull() ?: return@withContext
            if (localConversation.IsActive == active) {
                succeed = true
                return@withContext
            }
            val networkConversation = apiService.updateConversation(
                UpdateConversationRequest(
                    conversationId = conversationId,
                    name = localConversation.Name,
                    isActive = active
                )
            )?.data ?: return@withContext
            upsertNetworkConversationInLocal(networkConversation)
            succeed = true
        }
        log("toggleConversationActive succeed: $succeed")
        return succeed
    }

    override suspend fun sendUserRequest(
        kbId: String,
        conversationId: String,
        userRequest: String,
        agentic: Boolean
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
                )
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
                agentic = agentic,
                webSearch = false
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
        agentic: Boolean,
        onSseData: (SseData) -> Unit
    ): Duration {
        val userId = sessionManager.getUserId() ?: return Duration.ZERO
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
            )
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
        var statusPhase: String? = null
        var statusMessage: String? = null
        val responseLocalMessage = Message(
            MessageId = Uuid.generateV7().toString(),
            ConversationId = conversationId,
            KbId = kbId,
            UserId = userId,
            Role = NetworkMessageRole.AGENT,
            Content = content,
            MessageOrder = latestMessageOrder + 2,
            StatusPhase = statusPhase,
            StatusMessage = statusMessage
        )
        withContext(dispatcher) {
            upsertLocalMessage(responseLocalMessage)
        }
        val processDuration = measureTime {
            apiService.askSse(
                askRequest = AskRequest(
                    message = requestNetworkMessage,
                    agentic = agentic,
                    webSearch = false
                ),
                dispatcher = dispatcher,
                onCompletion = {
                    statusPhase = null
                    statusMessage = null
                    updateMessageStatusInLocal(
                        messageId = responseLocalMessage.MessageId,
                        statusPhase = statusPhase,
                        statusMessage = statusMessage
                    )
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
                            statusPhase = "Initiating"
                            statusMessage = ""
                            updateMessageStatusInLocal(
                                messageId = responseLocalMessage.MessageId,
                                statusPhase = statusPhase,
                                statusMessage = statusMessage
                            )
                            onSseData(data)
                        }

                        SseEvent.STATUS -> {
                            val dataString = serverSentEvent.data ?: return@handleSseEvent
                            val data = Json.decodeFromString<SseStatusData>(dataString)
                            log("data: $data")
                            if (data.phase == statusPhase) {
                                // same phase
                                statusMessage += data.message
                            } else {
                                // different phase
                                statusMessage = data.message
                            }
                            statusPhase = data.phase
                            updateMessageStatusInLocal(
                                messageId = responseLocalMessage.MessageId,
                                statusPhase = statusPhase?.titlecase(),
                                statusMessage = statusMessage?.trimMargin()
                            )
                            onSseData(data)
                        }

                        SseEvent.CONTENT -> {
                            val dataString = serverSentEvent.data ?: return@handleSseEvent
                            val data = Json.decodeFromString<SseContentData>(dataString)
                            log("data: $data")
                            content += data.delta.text
                            updateMessageContentInLocal(
                                messageId = responseLocalMessage.MessageId,
                                content = content
                            )
                            if (statusPhase != "Answering") {
                                statusPhase = "Answering"
                                statusMessage = ""
                                updateMessageStatusInLocal(
                                    messageId = responseLocalMessage.MessageId,
                                    statusPhase = statusPhase,
                                    statusMessage = statusMessage
                                )
                            }
                            onSseData(data)
                        }

                        SseEvent.STOP -> {
                            val dataString = serverSentEvent.data ?: return@handleSseEvent
                            val data = Json.decodeFromString<SseStopData>(dataString)
                            log("data: $data")
                            data.references?.forEach { reference ->
                                dbQueries.upsertCitation(
                                    messageId = responseLocalMessage.MessageId,
                                    originalIndex = reference.metadata.originalIndex.toLong(),
                                    kbId = reference.metadata.kbId,
                                    documentId = reference.metadata.documentId,
                                    fileName = reference.metadata.filename,
                                    originalFileName = reference.metadata.originalFileName,
                                    chunkIndex = reference.metadata.chunkIndex?.toLong(),
                                    startIndex = reference.metadata.startIndex?.toLong(),
                                    endIndex = reference.metadata.endIndex?.toLong(),
                                    pageContent = reference.pageContent
                                )
                            }
                            onSseData(data)
                        }

                        SseEvent.ERROR -> {
                            val dataString = serverSentEvent.data ?: return@handleSseEvent
                            val data = Json.decodeFromString<SseErrorData>(dataString)
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
        return processDuration
    }

    private suspend fun checkDocumentStatusUntilFinished(
        documentId: String,
        checkInterval: Long = 5000
    ) {
        val localDocument = dbQueries.getDocumentWithId(documentId).awaitAsOneOrNull() ?: return
        when (localDocument.Status) {
            NetworkDocumentStatus.FINISHED -> return
            NetworkDocumentStatus.TIMEOUT, NetworkDocumentStatus.FAILED -> {
                deleteDocument(documentId)
                return
            }

            else -> {
                // do nothing
            }
        }
        while (true) {
            val status = apiService.getDocumentStatus(documentId)?.data?.status?.apply {
                upsertLocalDocument(localDocument.copy(Status = this))
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

    override suspend fun getKnowledgeBasesInLocalFlow(): Flow<List<KbWithDocuments>> {
        val userId = sessionManager.getUserId() ?: return emptyFlow()
        val kbsFlow = dbQueries.getKnowledgeBasesWithUserId(userId).asFlow().mapToList(dispatcher)
        val documentsFlow = dbQueries.getDocumentsWithUserId(userId).asFlow().mapToList(dispatcher)
        return combine(kbsFlow, documentsFlow) { kbs, documents ->
            val kbsWithDocuments = mutableListOf<KbWithDocuments>()
            kbs.forEach { kb ->
                kbsWithDocuments.add(
                    KbWithDocuments(
                        kb = kb,
                        documents = documents.filter { it.KbId == kb.KbId }
                    )
                )
            }
            kbsWithDocuments
        }.flowOn(dispatcher)
    }

    override suspend fun getKnowledgeBaseWithIdInLocalFlow(kbId: String): Flow<KbWithDocuments?> {
        val kbFlow = dbQueries.getKnowledgeBaseWithId(kbId).asFlow().mapToOneOrNull(dispatcher)
        val documentsFlow = dbQueries.getDocumentsWithKbId(kbId).asFlow().mapToList(dispatcher)

        return combine(kbFlow, documentsFlow) { kb, documents ->
            if (kb == null)
                null
            else
                KbWithDocuments(
                    kb = kb,
                    documents = documents
                )
        }.flowOn(dispatcher)
    }

    override suspend fun getConversationsInLocalFlow(): Flow<List<ConversationWithMessages>> {
        val userId = sessionManager.getUserId() ?: return emptyFlow()
        val conversationsFlow = dbQueries.getConversationsWithuserId(userId).asFlow().mapToList(dispatcher)
        val messagesFlow = dbQueries.getMessagesWithUserId(userId).asFlow().mapToList(dispatcher)
        val citationsFlow = dbQueries.getCitationsWithUserId(userId).asFlow().mapToList(dispatcher)
        return combine(conversationsFlow, messagesFlow, citationsFlow) { conversations, messages, citations ->
            val conversationsWithMessages = mutableListOf<ConversationWithMessages>()
            conversations.forEach { conversation ->
                conversationsWithMessages.add(
                    ConversationWithMessages(
                        conversation = conversation,
                        messagesWithCitations = messages
                            .filter { it.ConversationId == conversation.ConversationId }
                            .sortedBy { it.MessageOrder }
                            .map { message ->
                                MessageWithCitations(
                                    message = message,
                                    citations = citations
                                        .filter { it.MessageId == message.MessageId }
                                )
                            }
                    )
                )
            }
            conversationsWithMessages
        }.flowOn(dispatcher)
    }

    private suspend fun upsertLocalKnowledgeBase(localKnowledgeBase: KnowledgeBase) {
        dbQueries.upsertKnowledgeBase(
            kbId = localKnowledgeBase.KbId,
            userId = localKnowledgeBase.UserId,
            name = localKnowledgeBase.Name,
            description = localKnowledgeBase.Description,
            createdAt = localKnowledgeBase.CreatedAt,
            updatedAt = localKnowledgeBase.UpdatedAt,
            isActive = localKnowledgeBase.IsActive,
            documentCount = localKnowledgeBase.DocumentCount
        )
    }

    suspend fun upsertLocalDocument(localDocument: Document) {
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

    private suspend fun upsertLocalMessage(localMessage: Message) {
        dbQueries.upsertMessage(
            messageId = localMessage.MessageId,
            conversationId = localMessage.ConversationId,
            kbId = localMessage.KbId,
            userId = localMessage.UserId,
            role = localMessage.Role,
            content = localMessage.Content,
            messageOrder = localMessage.MessageOrder,
            statusPhase = localMessage.StatusPhase,
            statusMessage = localMessage.StatusMessage
        )
    }

    private suspend fun updateMessageContentInLocal(messageId: String, content: String) {
        dbQueries.updateMessageContent(
            messageId = messageId,
            content = content
        )
    }

    private suspend fun updateMessageStatusInLocal(
        messageId: String,
        statusPhase: String?,
        statusMessage: String?
    ) {
        dbQueries.updateMessageStatus(
            messageId = messageId,
            statusPhase = statusPhase,
            statusMessage = statusMessage
        )
    }

    suspend fun upsertNetworkKnowledgeBaseInLocal(networkKnowledgeBase: NetworkKnowledgeBase) {
        dbQueries.upsertKnowledgeBase(
            kbId = networkKnowledgeBase.id,
            userId = networkKnowledgeBase.userId,
            name = networkKnowledgeBase.name,
            description = networkKnowledgeBase.description,
            createdAt = networkKnowledgeBase.createdAt,
            updatedAt = networkKnowledgeBase.updatedAt,
            isActive = networkKnowledgeBase.isActive,
            documentCount = networkKnowledgeBase.documentCount
        )
    }

    private suspend fun upsertNetworkDocumentInLocal(networkDocument: NetworkDocument) {
        dbQueries.upsertDocument(
            documentId = networkDocument.id,
            kbId = networkDocument.knowledgeBaseId,
            fileName = networkDocument.originalFileName,
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

    suspend fun upsertNetworkConversationInLocal(networkConversation: NetworkConversation) {
        dbQueries.upsertConversation(
            conversationId = networkConversation.conversationId,
            userId = sessionManager.getUserId() ?: "",
            name = networkConversation.name,
            isActive = networkConversation.isActive
        )
    }

    suspend fun upsertNetworkMessageInLocal(networkMessage: NetworkMessage) {
        dbQueries.upsertMessage(
            messageId = networkMessage.id,
            conversationId = networkMessage.metadata.conversationId,
            kbId = networkMessage.metadata.kbId,
            userId = networkMessage.metadata.userId,
            role = networkMessage.role,
            content = networkMessage.parts.firstOrNull()?.text ?: "",
            messageOrder = networkMessage.metadata.messageOrder ?: 0,
            statusPhase = null,
            statusMessage = null
        )
        networkMessage.retrievalContext?.citedChunks?.forEach { citedChunk ->
            dbQueries.upsertCitation(
                messageId = networkMessage.id,
                originalIndex = citedChunk.metadata.originalIndex.toLong(),
                kbId = citedChunk.metadata.kbId,
                documentId = citedChunk.metadata.documentId,
                fileName = citedChunk.metadata.fileName,
                originalFileName = citedChunk.metadata.originalFileName,
                chunkIndex = citedChunk.metadata.chunkIndex?.toLong(),
                startIndex = citedChunk.metadata.startIndex?.toLong(),
                endIndex = citedChunk.metadata.endIndex?.toLong(),
                pageContent = citedChunk.pageContent
            )
        }
    }

    suspend fun deleteKnowledgeBaseInLocal(kbId: String) {
        dbQueries.deleteDocumentsWithKbId(kbId)
        dbQueries.deleteKnowledgeBaseWithId(kbId)
    }

    suspend fun deleteConversationInLocal(conversationId: String) {
        dbQueries.deleteCitationsWithConversationId(conversationId)
        dbQueries.deleteMessagesWithConversationId(conversationId)
        dbQueries.deleteConversationWithId(conversationId)
    }

    suspend fun clearConversationInLocal(conversationId: String) {
        dbQueries.deleteCitationsWithConversationId(conversationId)
        dbQueries.deleteMessagesWithConversationId(conversationId)
    }
}