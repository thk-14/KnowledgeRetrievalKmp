package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thk.knowledgeretrievalkmp.ui.view.custom.*

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onNavigateToKnowledgeBase: () -> Unit,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = chatViewModel.chatUiState.snackBarHostState)
        }
    ) {
        ModalNavigationDrawer(
            drawerState = chatViewModel.chatUiState.drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ChatDrawer(
                    chatViewModel = chatViewModel,
                    onNavigateToKnowledgeBase = onNavigateToKnowledgeBase
                )
            }
        ) {
            ChatMainScreen(
                modifier = modifier,
                chatViewModel = chatViewModel
            )
        }
    }

    when (chatViewModel.chatUiState.showDialogAction.value) {
        is ChatShowDialogAction.ShowConversationBottomSheet -> {
            val conversation =
                (chatViewModel.chatUiState.showDialogAction.value as ChatShowDialogAction.ShowConversationBottomSheet).conversation
            ConversationBottomSheet(
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                },
                onDelete = {
                    chatViewModel.chatUiState.showDialogAction.value =
                        ChatShowDialogAction.DeleteConversationConfirmation(conversation)
                },
                onRename = {
                    chatViewModel.chatUiState.renameInputState.clearText()
                    chatViewModel.chatUiState.showDialogAction.value =
                        ChatShowDialogAction.RenameConversation(conversation)
                }
            )
        }

        is ChatShowDialogAction.ShowMessageBottomSheet -> {
            val sheetContent =
                (chatViewModel.chatUiState.showDialogAction.value as ChatShowDialogAction.ShowMessageBottomSheet)
            MessageBottomSheet(
                header = sheetContent.header,
                body = sheetContent.body,
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                }
            )
        }

        is ChatShowDialogAction.DeleteConversationConfirmation -> {
            val conversation =
                (chatViewModel.chatUiState.showDialogAction.value as ChatShowDialogAction.DeleteConversationConfirmation).conversation
            DeleteConfirmationDialog(
                title = "Delete Conversation",
                content = "Remove this conversation and all its contents",
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                },
                onConfirm = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                    chatViewModel.deleteConversation(conversation.conversation.value.ConversationId)
                }
            )
        }

        is ChatShowDialogAction.RenameConversation -> {
            val conversation =
                (chatViewModel.chatUiState.showDialogAction.value as ChatShowDialogAction.RenameConversation).conversation
            RenameDialog(
                title = "Edit Name",
                textFieldLabel = "Name",
                textFieldPlaceholder = conversation.conversation.value.Name,
                renameTextState = chatViewModel.chatUiState.renameInputState,
                onDismiss = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                },
                onConfirm = {
                    chatViewModel.chatUiState.showDialogAction.value = null
                    chatViewModel.renameConversation(
                        conversation.conversation.value.ConversationId,
                        chatViewModel.chatUiState.renameInputState.text.toString()
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
        loadingText = chatViewModel.chatUiState.showLoadingAction.value?.loadingText,
        loadingAnimation = chatViewModel.chatUiState.showLoadingAction.value?.loadingAnimation
    )
}