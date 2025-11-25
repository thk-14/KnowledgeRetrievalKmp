package com.thk.knowledgeretrievalkmp.ui.view.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.floor

@Composable
fun DocumentSection(
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val documentUploadFinish = stringResource(Res.string.document_upload_finish)
    val documentUploadFailed = stringResource(Res.string.document_upload_failed)
    val uploadDocumentLoadingText = stringResource(Res.string.LS_upload_document)
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DocumentSectionTopBar(
                detailViewModel = detailViewModel,
                modifier = Modifier.padding(
                    start = Dimens.content_padding_horizontal,
                    end = Dimens.content_padding_horizontal,
                    top = 10.dp
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = fabExpanded) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = !fabExpanded
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
                                                detailViewModel.detailUiState.showLoadingAction.value =
                                                    ShowLoadingAction(
                                                        loadingText = uploadDocumentLoadingText,
                                                        loadingAnimation = LottieAnimation.UPLOADING
                                                    )
                                                detailViewModel.uploadDocument(
                                                    fileName = file.name,
                                                    mimeType = file.mimeType().toString(),
                                                    file = file.readBytes(),
                                                    onUpload = { progress ->
                                                        val percentage = floor(progress * 10000) / 100
                                                        detailViewModel.detailUiState.showLoadingAction.value =
                                                            ShowLoadingAction(
                                                                loadingText = "$uploadDocumentLoadingText ${percentage}%",
                                                                loadingAnimation = LottieAnimation.UPLOADING
                                                            )
                                                    },
                                                    onUploadFinish = {
                                                        detailViewModel.detailUiState.showLoadingAction.value = null
                                                        detailViewModel.showSnackbar(documentUploadFinish)
                                                    },
                                                    onUploadFailed = {
                                                        detailViewModel.detailUiState.showLoadingAction.value = null
                                                        detailViewModel.showSnackbar(documentUploadFailed)
                                                    }
                                                )
                                            }

                                            is FileKitPickerState.Cancelled -> log("Selection cancelled")
                                        }
                                    }
                                }
                            },
                            containerColor = White,
                            contentColor = Black
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.upload),
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.top_bar_icon_size),
                            )
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = !fabExpanded
                                onNavigateToChat(detailViewModel.knowledgeBaseId)
                            },
                            containerColor = White,
                            contentColor = Black
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.chat),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(Dimens.top_bar_icon_size)
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = White,
                    contentColor = Black
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.expandable),
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimens.fab_icon_size)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(
                items = detailViewModel.detailUiState.knowledgeBase.value.documents,
                key = { it.DocumentId }
            ) { document ->
                DocumentItem(
                    document = document,
                    showDocumentDeleteOption = detailViewModel.detailUiState.showDocumentDeleteOption.value,
                    modifier = Modifier.animateItem(),
                    onDocumentDelete = {
                        detailViewModel.detailUiState.showDocumentDeleteOption.value = false
                        detailViewModel.detailUiState.showDialogAction.value =
                            DetailShowDialogAction.DeleteDocumentConfirmation(document)
                    }
                )
            }
        }
    }
}

@Composable
fun DocumentSectionTopBar(
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel
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
                    .clip(RoundedCornerShape(10.dp))
                    .size(Dimens.top_bar_icon_size)
                    .clickable {
                        detailViewModel.detailUiState.apply {
                            documentMenuExpanded.value = !documentMenuExpanded.value
                        }
                    }
            )
            DropdownMenu(
                expanded = detailViewModel.detailUiState.documentMenuExpanded.value,
                onDismissRequest = {
                    detailViewModel.detailUiState.documentMenuExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.delete_btn))
                    },
                    onClick = {
                        detailViewModel.detailUiState.documentMenuExpanded.value = false
                        detailViewModel.detailUiState.showDocumentDeleteOption.value = true
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
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
                modifier = Modifier
//                    .clickable {}
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