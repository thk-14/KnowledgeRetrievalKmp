package com.thk.knowledgeretrievalkmp.ui.view.kb

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.CreateKbDialog
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.FullScreenLoader
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import knowledgeretrievalkmp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun KbScreen(
    modifier: Modifier = Modifier,
    kbViewModel: KbViewModel = viewModel(factory = KbViewModel.Factory),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToAuthentication: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val createKbLoadingText = stringResource(Res.string.LS_create_kb)
    val kbCreateSuccess = stringResource(Res.string.kb_create_success)
    val kbCreateFailed = stringResource(Res.string.kb_create_failed)
    val nameEmptyWarning = stringResource(Res.string.kb_name_empty_warning)
    val descriptionEmptyWarning = stringResource(Res.string.kb_description_empty_warning)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = kbViewModel.kbUiState.snackBarHostState)
        }
    ) {
        KbMainScreen(
            modifier = modifier
                .background(White)
                .fillMaxSize()
                .padding(
                    horizontal = Dimens.padding_horizontal
                ),
            kbViewModel = kbViewModel,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToChat = onNavigateToChat,
            onNavigateToAuthentication = onNavigateToAuthentication,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }

    if (kbViewModel.kbUiState.showKBCreateDialog.value) {
        CreateKbDialog(
            createKbNameState = kbViewModel.kbUiState.createKbNameState,
            createKbDescriptionState = kbViewModel.kbUiState.createKbDescriptionState,
            onDismiss = {
                kbViewModel.dismissKbCreateDialog()
            },
            onConfirm = onConfirmButtonClick@{
                val name =
                    kbViewModel.kbUiState.createKbNameState.text.toString()
                val description =
                    kbViewModel.kbUiState.createKbDescriptionState.text.toString()
                if (name.isEmpty()) {
                    kbViewModel.showSnackbar(nameEmptyWarning)
                    return@onConfirmButtonClick
                }
                if (description.isEmpty()) {
                    kbViewModel.showSnackbar(descriptionEmptyWarning)
                    return@onConfirmButtonClick
                }
                kbViewModel.dismissKbCreateDialog()
                kbViewModel.kbUiState.showLoadingAction.value = ShowLoadingAction(createKbLoadingText)
                kbViewModel.createKnowledgeBase(
                    name = name,
                    description = description,
                    onCreateKbFinish = { succeed ->
                        kbViewModel.kbUiState.showLoadingAction.value = null
                        if (succeed) {
                            kbViewModel.showSnackbar(kbCreateSuccess)
                        } else {
                            kbViewModel.showSnackbar(kbCreateFailed)
                        }
                    }
                )
            }
        )
    }

    FullScreenLoader(
        visible = kbViewModel.kbUiState.showLoadingAction.value != null,
        text = kbViewModel.kbUiState.showLoadingAction.value?.loadingText ?: ""
    )
}