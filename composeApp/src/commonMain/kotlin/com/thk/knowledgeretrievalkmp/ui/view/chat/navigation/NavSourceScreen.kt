package com.thk.knowledgeretrievalkmp.ui.view.chat.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.thk.knowledgeretrievalkmp.data.network.NetworkDocumentStatus
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Blue
import com.thk.knowledgeretrievalkmp.ui.theme.Gray
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.chat.ChatShowDialogAction
import com.thk.knowledgeretrievalkmp.ui.view.chat.ChatViewModel
import com.thk.knowledgeretrievalkmp.ui.view.custom.*
import com.thk.knowledgeretrievalkmp.util.log
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import knowledgeretrievalkmp.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun NavSourceScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val documentUploadFinish = stringResource(Res.string.document_upload_finish)
    val documentUploadFailed = stringResource(Res.string.document_upload_failed)
    val uploadDocumentLoadingText = stringResource(Res.string.LS_upload_document)

    Scaffold(
        topBar = {
            NavSourceTopBar(
                chatViewModel = chatViewModel,
                modifier = Modifier.padding(
                    start = Dimens.content_padding_horizontal,
                    end = Dimens.content_padding_horizontal,
                    top = 10.dp
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val stateFlow = FileKit.openFilePicker(
                            mode = FileKitMode.SingleWithState,
                            type = FileKitType.File(FileExtension.entries.map { it.extension })
                        )
                        stateFlow.collect { state ->
                            when (state) {
                                is FileKitPickerState.Started -> log("Selection started with ${state.total} files")
                                is FileKitPickerState.Progress -> log("Processing: ${state.processed.size()} / ${state.total}")
                                is FileKitPickerState.Completed -> {
                                    log("Completed: ${state.result.size()} files selected")
                                    val file = state.result
                                    log("File selected: $file")
                                    chatViewModel.chatUiState.showLoadingAction.value =
                                        ShowLoadingAction(uploadDocumentLoadingText)
                                    chatViewModel.uploadDocument(
                                        fileName = file.name,
                                        mimeType = file.mimeType().toString(),
                                        uri = "",
                                        file = file.readBytes(),
                                        onUploadFinish = {
                                            chatViewModel.chatUiState.showLoadingAction.value = null
                                            coroutineScope.launch {
                                                chatViewModel.chatUiState.snackBarHostState.showSnackbar(
                                                    message = documentUploadFinish,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        },
                                        onUploadFailed = {
                                            chatViewModel.chatUiState.showLoadingAction.value = null
                                            coroutineScope.launch {
                                                chatViewModel.chatUiState.snackBarHostState.showSnackbar(
                                                    message = documentUploadFailed,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    )
                                }

                                is FileKitPickerState.Cancelled -> log("Selection cancelled")
                            }
                        }
                    }
                },
                containerColor = Black,
                contentColor = White
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.upload),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.top_bar_icon_size),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(
                items = chatViewModel.chatUiState.knowledgeBase.value.documents,
                key = { it.DocumentId }
            ) { document ->
                DocumentItem(
                    document = document,
                    showDocumentDeleteOption = chatViewModel.chatUiState.showDocumentDeleteOption.value,
                    modifier = Modifier.animateItem(),
                    onDocumentDelete = {
                        chatViewModel.chatUiState.showDocumentDeleteOption.value = false
                        chatViewModel.chatUiState.showDialogAction.value =
                            ChatShowDialogAction.DeleteDocumentConfirmation(document)
                    }
                )
            }
        }
    }
}

@Composable
fun NavSourceTopBar(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Sources",
            fontSize = 20.sp,
            color = Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.offset(x = (-10).dp)) {
            Icon(
                imageVector = vectorResource(Res.drawable.menu),
                contentDescription = null,
                modifier = Modifier
                    .size(Dimens.top_bar_icon_size)
                    .clickable {
                        chatViewModel.chatUiState.apply {
                            documentMenuExpanded.value = !documentMenuExpanded.value
                        }
                    }
            )
            DropdownMenu(
                expanded = chatViewModel.chatUiState.documentMenuExpanded.value,
                onDismissRequest = {
                    chatViewModel.chatUiState.documentMenuExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.delete_btn))
                    },
                    onClick = {
                        chatViewModel.chatUiState.documentMenuExpanded.value = false
                        chatViewModel.chatUiState.showDocumentDeleteOption.value = true
                    }
                )
            }
        }
    }
}

@Composable
fun DocumentItem(
    modifier: Modifier = Modifier,
    document: Document,
    showDocumentDeleteOption: Boolean,
    onDocumentDelete: () -> Unit
) {
    val screenWidth = LocalWindowSize.current.width

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.content_padding_horizontal,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isActive = !(document.IsInactive ?: true)
            val iconColor by animateColorAsState(
                if (isActive) Blue else Gray
            )
            val textColor by animateColorAsState(
                if (isActive) Black else Gray
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable {
                    // Open document
//                    val viewIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                        .setDataAndType(document.uri, document.mimeType)
//                        .apply {
//                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, document.uri)
//                        }
//                    context.startActivity(viewIntent)
                }
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.document),
                    contentDescription = null,
                    modifier = Modifier
                        .size(Dimens.top_bar_icon_size),
                    tint = iconColor
                )
                Text(
                    text = document.FileName,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 20.sp,
                    modifier = Modifier.sizeIn(
                        maxWidth = 0.6 * screenWidth
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            when (document.Status) {
                NetworkDocumentStatus.PENDING, NetworkDocumentStatus.PROCESSING -> {
                    InfiniteLoadingCircle(
                        size = Dimens.top_bar_icon_size,
                        modifier = Modifier.offset(x = (-10).dp)
                    )
                }

                else -> {
                    if (showDocumentDeleteOption) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.delete),
                            contentDescription = null,
                            modifier = Modifier
                                .size(Dimens.top_bar_icon_size)
                                .offset(x = (-10).dp)
                                .clickable {
                                    onDocumentDelete()
                                }
                        )
                    }
                }
            }
        }
    }
}