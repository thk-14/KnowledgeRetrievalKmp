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
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.launch

data class KbUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val knowledgeBases: SnapshotStateList<KnowledgeBase> = mutableStateListOf(),
    val createKBNameState: TextFieldState = TextFieldState(),
    val createKBDescriptionState: TextFieldState = TextFieldState(),
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
            log("profileUri: $profileUri")
            repository.getAllKnowledgeBasesInLocalFlow().collect {
                log("kb: $it")
                kbUiState.knowledgeBases.clear()
                kbUiState.knowledgeBases.addAll(it)
            }
        }
        viewModelScope.launch {
            fetchKnowledgeBases()
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
        val succeed = repository.createKnowledgeBaseWithConversation(name, description)
        log("createKnowledgeBase succeed: $succeed")
        onCreateKbFinish(succeed)
    }

    fun dismissKbCreateDialog() {
        kbUiState.createKBNameState.clearText()
        kbUiState.createKBDescriptionState.clearText()
        kbUiState.showKBCreateDialog.value = false
    }

    fun showKbCreateDialog() {
        kbUiState.createKBNameState.clearText()
        kbUiState.createKBDescriptionState.clearText()
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

    private suspend fun fetchKnowledgeBases() {
        kbUiState.showLoadingAction.value = ShowLoadingAction("Fetching knowledge bases ...")
        val succeed = repository.fetchKnowledgeBasesWithDocuments()
        kbUiState.showLoadingAction.value = null
        log("fetch KnowledgeBases succeed: $succeed")
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