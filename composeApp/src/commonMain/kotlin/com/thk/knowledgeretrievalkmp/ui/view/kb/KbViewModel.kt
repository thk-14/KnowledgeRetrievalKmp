package com.thk.knowledgeretrievalkmp.ui.view.kb

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
    val baseUrlState: TextFieldState = TextFieldState(),
    val menuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null),
    val showDialogAction: MutableState<KbShowDialogAction?> = mutableStateOf(null)
)

sealed class KbShowDialogAction {
    object CreateKb : KbShowDialogAction()
    object Setting : KbShowDialogAction()
}

class KbViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val kbUiState = KbUiState()
    val profileUri: MutableState<String> = mutableStateOf("")
    val baseUrl: MutableState<String> = mutableStateOf("")

    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    init {
        viewModelScope.launch {
            profileUri.value = repository.getProfileUri() ?: ""
            log("profileUri: ${profileUri.value}")
            baseUrl.value = repository.getBaseUrl()
            log("baseUrl: ${baseUrl.value}")
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

    fun dismissDialog() {
        kbUiState.showDialogAction.value = null
    }

    fun showCreateKbDialog() {
        kbUiState.createKbNameState.clearText()
        kbUiState.createKbDescriptionState.clearText()
        kbUiState.showDialogAction.value = KbShowDialogAction.CreateKb
    }

    fun showSettingDialog() {
        kbUiState.baseUrlState.setTextAndPlaceCursorAtEnd(baseUrl.value)
        kbUiState.showDialogAction.value = KbShowDialogAction.Setting
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

    fun setBaseUrl(value: String) {
        viewModelScope.launch {
            repository.setBaseUrl(value)
            baseUrl.value = value
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