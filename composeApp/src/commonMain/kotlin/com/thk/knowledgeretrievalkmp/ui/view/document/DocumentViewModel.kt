package com.thk.knowledgeretrievalkmp.ui.view.document

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.KnowledgeRetrievalRepository
import kotlinx.coroutines.launch

class DocumentViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val documentId = checkNotNull(savedStateHandle.get<String>("documentId"))
    val content = mutableStateOf("")
    val title = mutableStateOf("")
    val snackBarHostState: SnackbarHostState = SnackbarHostState()

    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    init {
        viewModelScope.launch {
            title.value = repository.getDocumentName(documentId)
            content.value = repository.getDocumentContent(documentId)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                DocumentViewModel(savedStateHandle)
            }
        }
    }
}