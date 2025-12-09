package com.thk.knowledgeretrievalkmp.ui.view.kb

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
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
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.launch

data class KbUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val knowledgeBases: SnapshotStateList<KnowledgeBase> = mutableStateListOf(),
    val createKbNameState: TextFieldState = TextFieldState(),
    val createKbDescriptionState: TextFieldState = TextFieldState(),
    val showKBCreateDialog: MutableState<Boolean> = mutableStateOf(false),
    val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null)
)

class KbViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val kbUiState = KbUiState()
    var profileUri: MutableState<String> = mutableStateOf("")

    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    init {
        viewModelScope.launch {
            profileUri.value = repository.getProfileUri() ?: ""
            log("profileUri: ${profileUri.value}")
            repository.getKnowledgeBasesInLocalFlow().collect { kbsWithDocuments ->
                kbUiState.knowledgeBases.clear()
                kbUiState.knowledgeBases.addAll(kbsWithDocuments.map { it.kb })
            }
        }
        if (!repository.isDataFetched) {
            fetchData()
            repository.isDataFetched = true
        }
    }

    fun logout(onLogoutFinish: (Boolean) -> Unit) = viewModelScope.launch {
        val succeed = repository.logout()
        log("logout succeed: $succeed")
        onLogoutFinish(succeed)
    }

    fun createKnowledgeBase(
        name: String,
        description: String,
        onCreateKbFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        // FOR TESTING
//        delay(5000)
//        val userId = DefaultKnowledgeRetrievalRepository.getUserId() ?: ""
//        DefaultKnowledgeRetrievalRepository.upsertNetworkKnowledgeBaseInLocal(
//            NetworkKnowledgeBase(
//                id = Uuid.generateV7().toString(),
//                name = name,
//                userId = userId,
//                description = description,
//                createdAt = "",
//                updatedAt = "",
//                isActive = true,
//                documentCount = 0
//            )
//        )
//        val succeed = true
        // END TESTING


        val succeed = repository.createKnowledgeBase(name, description)
        log("createKnowledgeBase succeed: $succeed")
        onCreateKbFinish(succeed)
    }

    fun dismissKbCreateDialog() {
        kbUiState.createKbNameState.clearText()
        kbUiState.createKbDescriptionState.clearText()
        kbUiState.showKBCreateDialog.value = false
    }

    fun showKbCreateDialog() {
        kbUiState.createKbNameState.clearText()
        kbUiState.createKbDescriptionState.clearText()
        kbUiState.showKBCreateDialog.value = true
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            kbUiState.snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            fetchKnowledgeBasesWithDocuments()
            fetchConversationsWithMessages()
        }
    }

    private suspend fun fetchKnowledgeBasesWithDocuments() {
        kbUiState.showLoadingAction.value = ShowLoadingAction(
            loadingText = "Fetching knowledge bases",
            loadingAnimation = LottieAnimation.FETCHING
        )
        val succeed = repository.fetchKnowledgeBasesWithDocuments()
        kbUiState.showLoadingAction.value = null
    }

    private fun fetchConversationsWithMessages() {
        repository.fetchConversationsWithMessagesGlobally()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                KbViewModel(savedStateHandle)
            }
        }
    }
}