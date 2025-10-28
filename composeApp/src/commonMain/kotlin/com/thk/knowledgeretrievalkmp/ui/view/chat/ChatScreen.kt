package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.FullScreenLoader
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import knowledgeretrievalkmp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    onBackPressed: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val coroutineScope = rememberCoroutineScope()
    val kbRenameSuccess = stringResource(Res.string.kb_rename_success)
    val kbRenameFailed = stringResource(Res.string.kb_rename_failed)
    val kbDeleteFailed = stringResource(Res.string.kb_delete_failed)
    val documentDeleteSuccess = stringResource(Res.string.document_delete_success)
    val documentDeleteFailed = stringResource(Res.string.document_delete_failed)
    val renameKbLoadingText = stringResource(Res.string.LS_rename_kb)
    val deleteKbLoadingText = stringResource(Res.string.LS_delete_kb)
    val deleteDocumentLoadingText = stringResource(Res.string.LS_delete_document)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = chatViewModel.chatUiState.snackBarHostState)
        }
    ) {
        ChatMainScreen(
            modifier = modifier
                .background(White)
                .fillMaxSize(),
            chatViewModel = chatViewModel,
            onBackPressed = onBackPressed,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }

    when (chatViewModel.chatUiState.showDialogAction.value) {

        is ChatShowDialogAction.DeleteKbConfirmation -> {
            DeleteConfirmationDialog(
                title = "Delete Knowledge Base",
                content = "Remove this knowledge base and all its contents",
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                },
                onConfirm = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                    chatViewModel.chatUiState.showLoadingAction.value = ShowLoadingAction(deleteKbLoadingText)
                    chatViewModel.deleteKnowledgeBase(
                        onDeleteFinish = { succeed ->
                            chatViewModel.chatUiState.showLoadingAction.value = null
                            if (succeed) {
                                onBackPressed()
                            } else {
                                chatViewModel.showSnackbar(kbDeleteFailed)
                            }
                        }
                    )
                }
            )
        }

        is ChatShowDialogAction.DeleteDocumentConfirmation -> {
            val deleteDocument =
                (chatViewModel.chatUiState.showDialogAction.value as ChatShowDialogAction.DeleteDocumentConfirmation).document
            DeleteConfirmationDialog(
                title = stringResource(
                    Res.string.chat_delete_document_title,
                    deleteDocument.FileName
                ),
                content = stringResource(Res.string.chat_delete_document_content),
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                },
                onConfirm = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                    chatViewModel.chatUiState.showLoadingAction.value = ShowLoadingAction(deleteDocumentLoadingText)
                    chatViewModel.deleteDocument(
                        documentId = deleteDocument.DocumentId,
                        onDeleteFinish = { succeed ->
                            chatViewModel.chatUiState.showLoadingAction.value = null
                            chatViewModel.showSnackbar(
                                if (succeed) documentDeleteSuccess else documentDeleteFailed
                            )
                        }
                    )
                }
            )
        }

        is ChatShowDialogAction.RenameKB -> {
            val emptyNameWarning =
                stringResource(Res.string.kb_name_empty_warning)
            val sameNameWarning =
                stringResource(Res.string.kb_rename_with_same_name_warning)
            RenameDialog(
                title = "Edit name",
                textFieldLabel = "Name",
                textFieldPlaceholder = chatViewModel.chatUiState.knowledgeBase.value.kb.value.Name,
                renameTextState = chatViewModel.chatUiState.renameInputState,
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                },
                onConfirm = ConfirmRename@{
                    val newName = chatViewModel.chatUiState.renameInputState.text.toString()
                    if (newName.isEmpty()) {
                        chatViewModel.showSnackbar(emptyNameWarning)
                        return@ConfirmRename
                    }
                    if (newName == chatViewModel.chatUiState.knowledgeBase.value.kb.value.Name) {
                        chatViewModel.showSnackbar(sameNameWarning)
                        return@ConfirmRename
                    }
                    chatViewModel.chatUiState.showDialogAction.value = null
                    chatViewModel.chatUiState.showLoadingAction.value = ShowLoadingAction(renameKbLoadingText)
                    chatViewModel.renameKnowledgeBase(
                        newName = newName,
                        onRenameFinish = { succeed ->
                            chatViewModel.chatUiState.showLoadingAction.value = null
                            chatViewModel.showSnackbar(
                                if (succeed) kbRenameSuccess else kbRenameFailed
                            )
                        }
                    )
                }
            )
        }

        else -> {
            // Do nothing
        }
    }

    FullScreenLoader(
        visible = chatViewModel.chatUiState.showLoadingAction.value != null,
        text = chatViewModel.chatUiState.showLoadingAction.value?.loadingText ?: ""
    )
}