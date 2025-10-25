package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.KnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.ui.view.custom.UiKnowledgeBase
import com.thk.knowledgeretrievalkmp.ui.view.custom.toUiKnowledgeBase
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.launch

data class ChatUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val knowledgeBase: MutableState<UiKnowledgeBase> = mutableStateOf(UiKnowledgeBase()),
    val chatInputState: TextFieldState = TextFieldState(),
    val renameInputState: TextFieldState = TextFieldState(),
    val kbMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val documentMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val showDocumentDeleteOption: MutableState<Boolean> = mutableStateOf(false),
    val webSearch: MutableState<Boolean> = mutableStateOf(false),
    val showDialogAction: MutableState<ChatShowDialogAction?> = mutableStateOf(null),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null)
)

sealed class ChatShowDialogAction {
    object DeleteKbConfirmation : ChatShowDialogAction()
    object RenameKB : ChatShowDialogAction()
    data class DeleteDocumentConfirmation(val document: Document) : ChatShowDialogAction()
}

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val knowledgeBaseId = checkNotNull(savedStateHandle.get<String>("knowledgeBaseId"))
    var displayName: MutableState<String> = mutableStateOf("")
    val chatUiState = ChatUiState()

    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    init {
        viewModelScope.launch {
            displayName.value = repository.getDisplayName() ?: ""
            repository.getKnowledgeBaseWithIdInLocalFlow(knowledgeBaseId)
                .collect { newKb ->
                    log("newKb: $newKb")
                    if (newKb != null) {
                        newKb.conversation?.messages?.sortBy { it.CreatedAt }
                        chatUiState.knowledgeBase.value = newKb.toUiKnowledgeBase()
                    }
                }
        }
        viewModelScope.launch {
            chatUiState.showLoadingAction.value = ShowLoadingAction("Fetching knowledge base ...")
            fetchKnowledgeBaseWithConversation()

            chatUiState.showLoadingAction.value = ShowLoadingAction("Activating knowledge base ...")
            toggleKnowledgeBaseActive(true)
            toggleDocumentsActiveForKnowledgeBase(true)
            toggleConversationActiveForKnowledgeBase(true)

            chatUiState.showLoadingAction.value = null
        }
    }

    fun renameKnowledgeBase(
        newName: String,
        onRenameFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val succeed = repository.renameKnowledgeBase(knowledgeBaseId, newName)
        log("renameKnowledgeBase succeed: $succeed")
        onRenameFinish(succeed)
    }

    fun deleteKnowledgeBase(onDeleteFinish: (Boolean) -> Unit) = viewModelScope.launch {
        val succeed = repository.deleteKnowledgeBaseWithConversation(knowledgeBaseId)
        log("deleteKnowledgeBase succeed: $succeed")
        onDeleteFinish(succeed)
    }

    fun uploadDocument(
        fileName: String,
        mimeType: String,
        uri: String,
        file: ByteArray,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ) = viewModelScope.launch {
        val succeed = repository.uploadDocument(
            knowledgeBaseId = knowledgeBaseId,
            fileName = fileName,
            mimeType = mimeType,
            uri = uri,
            file = file,
            onUploadFinish = onUploadFinish,
            onUploadFailed = onUploadFailed
        )
        log("uploadDocument succeed: $succeed")
    }

    fun deleteDocument(
        documentId: String,
        onDeleteFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val succeed = repository.deleteDocument(documentId)
        log("deleteDocument succeed: $succeed")
        onDeleteFinish(succeed)
    }

    fun sendUserRequest() {
        val userRequest = chatUiState.chatInputState.text.toString()
        if (userRequest.isEmpty()) return
        viewModelScope.launch {
            repository.sendUserRequest(
                kbId = knowledgeBaseId,
                conversationId = chatUiState.knowledgeBase.value.kb.value.ConversationId ?: "",
                userRequest = userRequest,
                webSearch = chatUiState.webSearch.value
            )
        }
    }

    fun sendUserRequestWithSSE() {
        val userRequest = chatUiState.chatInputState.text.toString()
        if (userRequest.isEmpty()) return
        viewModelScope.launch {
            repository.collectSSEResponseFlow(
                kbId = knowledgeBaseId,
                conversationId = chatUiState.knowledgeBase.value.kb.value.ConversationId ?: "",
                userRequest = userRequest,
                webSearch = chatUiState.webSearch.value
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            toggleKnowledgeBaseActive(false)
            toggleDocumentsActiveForKnowledgeBase(false)
            toggleConversationActiveForKnowledgeBase(false)
        }
    }

    private suspend fun fetchKnowledgeBaseWithConversation() {
        val succeed = repository.fetchKnowledgeBaseWithDocuments(knowledgeBaseId)
        log("fetchKnowledgeBaseWithConversations succeed: $succeed")
    }

    private suspend fun toggleKnowledgeBaseActive(active: Boolean) {
        val succeed = repository.toggleKnowledgeBaseActive(knowledgeBaseId, active)
        log("setKnowledgeBaseActive $active succeed: $succeed")
    }

    private suspend fun toggleDocumentsActiveForKnowledgeBase(active: Boolean) {
        val succeed = repository.toggleDocumentsActiveForKnowledgeBase(knowledgeBaseId, active)
        log("setDocumentsActiveForKnowledgeBase $active succeed: $succeed")
    }

    private suspend fun toggleConversationActiveForKnowledgeBase(active: Boolean) {
        val succeed = repository.toggleConversationActiveForKnowledgeBase(knowledgeBaseId, active)
        log("setConversationActiveForKnowledgeBase $active succeed: $succeed")
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                ChatViewModel(savedStateHandle)
            }
        }
    }
}