package com.thk.knowledgeretrievalkmp.ui.view.detail

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.*
import knowledgeretrievalkmp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory),
    onBackPressed: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToDocument: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
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
            SnackbarHost(hostState = detailViewModel.detailUiState.snackBarHostState)
        }
    ) {
        DetailMainScreen(
            modifier = modifier
                .background(White)
                .fillMaxSize(),
            detailViewModel = detailViewModel,
            onBackPressed = onBackPressed,
            onNavigateToChat = onNavigateToChat,
            onNavigateToDocument = onNavigateToDocument,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }

    when (detailViewModel.detailUiState.showDialogAction.value) {

        is DetailShowDialogAction.DeleteKbConfirmation -> {
            DeleteConfirmationDialog(
                title = "Delete Knowledge Base",
                content = "Remove this knowledge base and all its contents",
                onDismiss = {
                    detailViewModel.dismissDialog()
                },
                onConfirm = {
                    detailViewModel.dismissDialog()
                    detailViewModel.detailUiState.showLoadingAction.value =
                        ShowLoadingAction(
                            loadingText = deleteKbLoadingText,
                            loadingAnimation = LottieAnimation.DELETING
                        )
                    detailViewModel.deleteKnowledgeBase(
                        onDeleteFinish = { succeed ->
                            detailViewModel.detailUiState.showLoadingAction.value = null
                            if (succeed) {
                                onBackPressed()
                            } else {
                                detailViewModel.showSnackbar(kbDeleteFailed)
                            }
                        }
                    )
                }
            )
        }

        is DetailShowDialogAction.DeleteDocumentConfirmation -> {
            val deleteDocument =
                (detailViewModel.detailUiState.showDialogAction.value as DetailShowDialogAction.DeleteDocumentConfirmation).document
            DeleteConfirmationDialog(
                title = stringResource(
                    Res.string.chat_delete_document_title,
                    deleteDocument.FileName
                ),
                content = stringResource(Res.string.chat_delete_document_content),
                onDismiss = {
                    detailViewModel.dismissDialog()
                },
                onConfirm = {
                    detailViewModel.dismissDialog()
                    detailViewModel.detailUiState.showLoadingAction.value =
                        ShowLoadingAction(
                            loadingText = deleteDocumentLoadingText,
                            loadingAnimation = LottieAnimation.DELETING
                        )
                    detailViewModel.deleteDocument(
                        documentId = deleteDocument.DocumentId,
                        onDeleteFinish = { succeed ->
                            detailViewModel.detailUiState.showLoadingAction.value = null
                            detailViewModel.showSnackbar(
                                if (succeed) documentDeleteSuccess else documentDeleteFailed
                            )
                        }
                    )
                }
            )
        }

        is DetailShowDialogAction.RenameKb -> {
            val emptyNameWarning =
                stringResource(Res.string.kb_name_empty_warning)
            val sameNameWarning =
                stringResource(Res.string.kb_rename_with_same_name_warning)
            RenameDialog(
                title = "Edit name",
                textFieldLabel = "Name",
                textFieldPlaceholder = detailViewModel.detailUiState.knowledgeBase.value.kb.value.Name,
                renameTextState = detailViewModel.detailUiState.renameInputState,
                onDismiss = {
                    detailViewModel.dismissDialog()
                },
                onConfirm = ConfirmRename@{
                    val newName = detailViewModel.detailUiState.renameInputState.text.toString()
                    if (newName.isEmpty()) {
                        detailViewModel.showSnackbar(emptyNameWarning)
                        return@ConfirmRename
                    }
                    if (newName == detailViewModel.detailUiState.knowledgeBase.value.kb.value.Name) {
                        detailViewModel.showSnackbar(sameNameWarning)
                        return@ConfirmRename
                    }
                    detailViewModel.dismissDialog()
                    detailViewModel.detailUiState.showLoadingAction.value =
                        ShowLoadingAction(
                            loadingText = renameKbLoadingText,
                            loadingAnimation = LottieAnimation.CHANGING
                        )
                    detailViewModel.renameKnowledgeBase(
                        newName = newName,
                        onRenameFinish = { succeed ->
                            detailViewModel.detailUiState.showLoadingAction.value = null
                            detailViewModel.showSnackbar(
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
        visible = detailViewModel.detailUiState.showLoadingAction.value != null,
        loadingText = detailViewModel.detailUiState.showLoadingAction.value?.loadingText,
        loadingAnimation = detailViewModel.detailUiState.showLoadingAction.value?.loadingAnimation
    )
}