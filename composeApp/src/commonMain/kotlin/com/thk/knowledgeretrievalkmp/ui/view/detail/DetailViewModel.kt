package com.thk.knowledgeretrievalkmp.ui.view.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarDuration
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
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.ui.view.custom.UiKnowledgeBase
import com.thk.knowledgeretrievalkmp.ui.view.custom.toUiKnowledgeBase
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.launch

data class DetailUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val knowledgeBase: MutableState<UiKnowledgeBase> = mutableStateOf(UiKnowledgeBase()),
    val renameInputState: TextFieldState = TextFieldState(),
    val kbMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val documentMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    val showDocumentDeleteOption: MutableState<Boolean> = mutableStateOf(false),
    val showDialogAction: MutableState<DetailShowDialogAction?> = mutableStateOf(null),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null)
)

sealed class DetailShowDialogAction {
    object DeleteKbConfirmation : DetailShowDialogAction()
    object RenameKb : DetailShowDialogAction()
    data class DeleteDocumentConfirmation(val document: Document) : DetailShowDialogAction()
}

class DetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val knowledgeBaseId = checkNotNull(savedStateHandle.get<String>("knowledgeBaseId"))
    val detailUiState = DetailUiState()

    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    init {
        viewModelScope.launch {
//            fetchKnowledgeBaseWithDocuments()
            repository.getKnowledgeBaseWithIdInLocalFlow(knowledgeBaseId)
                .collect { newKb ->
                    if (newKb != null) {
                        detailUiState.knowledgeBase.value = newKb.toUiKnowledgeBase()
                    }
                }
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
        // FOR TESTING
//        delay(5000)
//        DefaultKnowledgeRetrievalRepository.deleteKnowledgeBaseInLocal(knowledgeBaseId)
//        val succeed = true
        // END TESTING


        val succeed = repository.deleteKnowledgeBase(knowledgeBaseId)
        log("deleteKnowledgeBase succeed: $succeed")
        onDeleteFinish(succeed)
    }

    fun uploadDocument(
        fileName: String,
        mimeType: String,
        file: ByteArray,
        onUpload: (Float) -> Unit,
        onUploadFinish: () -> Unit,
        onUploadFailed: () -> Unit
    ) = viewModelScope.launch {
        // FOR TESTING
//        delay(5000)
//        val localDocument = Document(
//            DocumentId = Uuid.generateV7().toString(),
//            KbId = knowledgeBaseId,
//            FileName = fileName,
//            MimeType = mimeType,
//            Status = NetworkDocumentStatus.FINISHED,
//            IsInactive = null,
//            Description = null,
//            FilePath = null,
//            FileSize = null,
//            FileType = null,
//            ProcessingError = null,
//            CreatedAt = null,
//            UpdatedAt = null,
//            UploadedBy = null,
//            ProcessedAt = null
//        )
//        upsertLocalDocument(localDocument)
//        val succeed = true
//        onUploadFinish()
        // END TESTING


        val succeed = repository.uploadDocument(
            knowledgeBaseId = knowledgeBaseId,
            fileName = fileName,
            mimeType = mimeType,
            file = file,
            onUpload = onUpload,
            onUploadFinish = onUploadFinish,
            onUploadFailed = onUploadFailed
        )
    }

    fun deleteDocument(
        documentId: String,
        onDeleteFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        // FOR TESTING
//        delay(5000)
//        AppContainer.db.knowledgeBaseQueries.deleteDocumentWithId(documentId)
//        val succeed = true
        // END TESTING


        val succeed = repository.deleteDocument(documentId)
        onDeleteFinish(succeed)
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            detailUiState.snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    private suspend fun fetchKnowledgeBaseWithDocuments() {
        detailUiState.showLoadingAction.value = ShowLoadingAction(
            loadingText = "Fetching knowledge base",
            loadingAnimation = LottieAnimation.FETCHING
        )
        val succeed = repository.fetchKnowledgeBaseWithDocuments(knowledgeBaseId)
        detailUiState.showLoadingAction.value = null
        log("fetchKnowledgeBaseWithDocuments succeed: $succeed")
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                DetailViewModel(savedStateHandle)
            }
        }
    }
}