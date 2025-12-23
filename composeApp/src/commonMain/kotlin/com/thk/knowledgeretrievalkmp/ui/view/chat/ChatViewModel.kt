package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.KnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.network.SseData
import com.thk.knowledgeretrievalkmp.ui.view.custom.*
import com.thk.knowledgeretrievalkmp.util.titlecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.Duration

data class ChatUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val kbs: SnapshotStateList<UiKnowledgeBase> = mutableStateListOf(),
    val conversations: SnapshotStateList<UiConversation> = mutableStateListOf(),
    val activeKbId: MutableState<String> = mutableStateOf(""),
    val activeConversationId: MutableState<String> = mutableStateOf(""),
    val chatInputState: TextFieldState = TextFieldState(),
    val renameInputState: TextFieldState = TextFieldState(),
    val agentic: MutableState<Boolean> = mutableStateOf(true),
    val sendEnabled: MutableState<Boolean> = mutableStateOf(true),
    val drawerState: DrawerState = DrawerState(initialValue = DrawerValue.Closed),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null),
    val showDialogAction: MutableState<ChatShowDialogAction?> = mutableStateOf(null)
)

sealed class ChatShowDialogAction {
    data class DeleteConversationConfirmation(
        val conversation: UiConversation
    ) : ChatShowDialogAction()

    data class RenameConversation(
        val conversation: UiConversation
    ) : ChatShowDialogAction()

    data class ShowConversationBottomSheet(
        val conversation: UiConversation
    ) : ChatShowDialogAction()

    data class ShowMessageBottomSheet(
        val header: String,
        val body: String
    ) : ChatShowDialogAction()
}

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val chatUiState = ChatUiState()
    var displayName: MutableState<String> = mutableStateOf("")
    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository
    private var sendMessageJob: Job? = null

    init {
        chatUiState.activeKbId.value = checkNotNull(savedStateHandle.get<String>("initialKnowledgeBaseId"))
        toggleKbActive(chatUiState.activeKbId.value, true)
        viewModelScope.launch {
            displayName.value = repository.getDisplayName() ?: ""
            repository.getKnowledgeBasesInLocalFlow().collect { kbsWithDocuments ->
//                log("kbsWithDocuments: $kbsWithDocuments")
                chatUiState.kbs.clear()
                chatUiState.kbs.addAll(kbsWithDocuments.map { it.toUiKnowledgeBase() })
            }
        }
        viewModelScope.launch {
            repository.getConversationsInLocalFlow().collect { conversationsWithMessages ->
//                log("conversationsWithMessages: $conversationsWithMessages")
                chatUiState.conversations.clear()
                chatUiState.conversations.addAll(conversationsWithMessages.map { it.toUiConversation() })
            }
        }
    }

    fun sendUserRequest() {
        val userRequest = chatUiState.chatInputState.text.toString()
        if (userRequest.isEmpty()) return
        viewModelScope.launch {
            if (chatUiState.activeConversationId.value.isEmpty()) {
                val newConversationId = repository.createConversation(
                    conversationName = userRequest.titlecase()
                )
                if (newConversationId == null) return@launch
                chatUiState.activeConversationId.value = newConversationId
            }
            repository.sendUserRequest(
                kbId = chatUiState.activeKbId.value,
                conversationId = chatUiState.activeConversationId.value,
                userRequest = userRequest,
                agentic = chatUiState.agentic.value
            )
        }
    }

    fun sendUserRequestWithSSE(
        onSseData: (SseData) -> Unit,
        onCompletion: (Duration) -> Unit
    ) {
        val userRequest = chatUiState.chatInputState.text.toString()
        if (userRequest.isEmpty()) return
        sendMessageJob = viewModelScope.launch {
            // FOR TESTING
//            if (chatUiState.activeConversationId.value.isEmpty()) {
//                val newConversationId = Uuid.generateV7().toString()
//                DefaultKnowledgeRetrievalRepository.upsertNetworkConversationInLocal(
//                    NetworkConversation(
//                        conversationId = newConversationId,
//                        isActive = true,
//                        name = userRequest,
//                        summary = "",
//                        summarizedUpToMessageOrder = null
//                    )
//                )
//                chatUiState.activeConversationId.value = newConversationId
//            }
//            val userId = DefaultKnowledgeRetrievalRepository.getUserId() ?: ""
//            val requestNetworkMessage = NetworkMessage(
//                id = Uuid.generateV7().toString(),
//                role = NetworkMessageRole.USER,
//                parts = listOf(
//                    NetworkPartText(
//                        type = "text",
//                        text = userRequest
//                    )
//                ),
//                metadata = NetworkMessageMetadata(
//                    conversationId = chatUiState.activeConversationId.value,
//                    kbId = chatUiState.activeKbId.value,
//                    userId = userId,
//                    appName = ""
//                ),
//            )
//            upsertNetworkMessageInLocal(requestNetworkMessage)
//            val responseNetworkMessage = NetworkMessage(
//                id = Uuid.generateV7().toString(),
//                role = NetworkMessageRole.AGENT,
//                parts = listOf(
//                    NetworkPartText(
//                        type = "text",
//                        text =
//                            """
//## Markdown Formatting Examples
//
//This text demonstrates various formatting options available in Markdown. You can use **bold text** to emphasize key words, or *italic text* for adding a different kindt of emphasis. You can also combine them to create ***bold and italic text***.
//
//---
//
//### Data Table
//
//Tables are useful for organizing data.
//
//| Item | Quantity | Status |
//| :--- | :---: | ---: |
//| Widget A | 150 | In Stock |
//| Gadget B | 0 | Out of Stock |
//| Thing C | 42 | Low Stock |
//
//---
//
//### Using Footnotes
//
//You can add footnotes[test] to provide extra information or citations. This helps keep the main text clean while offering additional context.
//
//[test]: This is the text that will appear in the footnote section at the bottom.
//    """.trimMargin()
//                    )
//                ),
//                metadata = NetworkMessageMetadata(
//                    conversationId = chatUiState.activeConversationId.value,
//                    kbId = chatUiState.activeKbId.value,
//                    userId = userId,
//                    appName = ""
//                ),
//            )
//            upsertNetworkMessageInLocal(responseNetworkMessage)
            // END TESTING


            if (chatUiState.activeConversationId.value.isEmpty()) {
                val newConversationId = repository.createConversation(
                    conversationName = userRequest.titlecase()
                )
                if (newConversationId == null) return@launch
                chatUiState.activeConversationId.value = newConversationId
            }
            repository.collectSSEResponseFlow(
                kbId = chatUiState.activeKbId.value,
                conversationId = chatUiState.activeConversationId.value,
                userRequest = userRequest,
                agentic = chatUiState.agentic.value,
                onSseData = onSseData,
                onCompletion = onCompletion
            )
        }
    }

    fun toggleKbActive(kbId: String, active: Boolean) {
        val loadingText =
            if (active) "Activating knowledge base"
            else "Deactivating knowledge base"
        viewModelScope.launch {
//            chatUiState.showLoadingAction.value = ShowLoadingAction(
//                loadingText = loadingText,
//                loadingAnimation = LottieAnimation.ACTIVATING
//            )
            repository.toggleKnowledgeBaseActive(kbId, active)
            repository.toggleDocumentsActiveForKnowledgeBase(kbId, active)
//            chatUiState.showLoadingAction.value = null
        }
    }

    fun toggleConversationActive(conversationId: String, active: Boolean) {
        val loadingText =
            if (active) "Activating conversation"
            else "Deactivating conversation"
        viewModelScope.launch {
//            chatUiState.showLoadingAction.value = ShowLoadingAction(
//                loadingText = loadingText,
//                loadingAnimation = LottieAnimation.ACTIVATING
//            )
            repository.toggleConversationActive(conversationId, active)
//            chatUiState.showLoadingAction.value = null
        }
    }

    fun activateConversation(conversationId: String) {
        if (chatUiState.activeConversationId.value == conversationId) {
            return
        }
        if (sendMessageJob?.isActive == true) {
            sendMessageJob?.cancel()
            chatUiState.sendEnabled.value = true
            showSnackbar("Message aborted")
        }
        chatUiState.activeConversationId.value = conversationId
        toggleConversationActive(conversationId, true)
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            // FOR TESTING
//            delay(5000)
//            DefaultKnowledgeRetrievalRepository.deleteConversationInLocal(conversationId)
//            if (chatUiState.activeConversationId.value == conversationId) {
//                chatUiState.activeConversationId.value = ""
//            }
            // END TESTING


            chatUiState.showLoadingAction.value = ShowLoadingAction(
                loadingText = "Deleting conversation",
                loadingAnimation = LottieAnimation.DELETING
            )
            val succeed = repository.deleteConversation(conversationId)
            if (succeed) {
                if (chatUiState.activeConversationId.value == conversationId) {
                    chatUiState.activeConversationId.value = ""
                }
            }
            chatUiState.showLoadingAction.value = null
            val message =
                if (succeed) "Delete successfully."
                else "Delete failed."
            showSnackbar(message)
        }
    }

    fun renameConversation(conversationId: String, newName: String) {
        viewModelScope.launch {
            chatUiState.showLoadingAction.value = ShowLoadingAction(
                loadingText = "Renaming conversation",
                loadingAnimation = LottieAnimation.CHANGING
            )
            val succeed = repository.renameConversation(conversationId, newName)
            chatUiState.showLoadingAction.value = null
            val message =
                if (succeed) "Rename successfully."
                else "Rename failed."
            showSnackbar(message)
        }
    }

    fun dismissDialog() {
        chatUiState.showDialogAction.value = null
    }

    fun showConversationBottomSheet(conversation: UiConversation) {
        chatUiState.showDialogAction.value = ChatShowDialogAction.ShowConversationBottomSheet(conversation)
    }

    fun showMessageBottomSheet(header: String, body: String) {
        chatUiState.showDialogAction.value =
            ChatShowDialogAction.ShowMessageBottomSheet(
                header = header,
                body = body
            )
    }

    fun showRenameConversationDialog(conversation: UiConversation) {
        chatUiState.renameInputState.clearText()
        chatUiState.showDialogAction.value = ChatShowDialogAction.RenameConversation(conversation)
    }

    fun showDeleteConversationDialog(conversation: UiConversation) {
        chatUiState.showDialogAction.value = ChatShowDialogAction.DeleteConversationConfirmation(conversation)
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            chatUiState.snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
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