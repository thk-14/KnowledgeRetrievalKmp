package com.thk.knowledgeretrievalkmp.ui.view.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.thk.knowledgeretrievalkmp.ui.view.custom.ColumnWithScrollbar
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.FileExtension
import com.thk.knowledgeretrievalkmp.ui.view.custom.InfiniteLoadingCircle
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import knowledgeretrievalkmp.composeapp.generated.resources.LS_upload_document
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.chat
import knowledgeretrievalkmp.composeapp.generated.resources.delete
import knowledgeretrievalkmp.composeapp.generated.resources.delete_btn
import knowledgeretrievalkmp.composeapp.generated.resources.document
import knowledgeretrievalkmp.composeapp.generated.resources.document_upload_failed
import knowledgeretrievalkmp.composeapp.generated.resources.document_upload_finish
import knowledgeretrievalkmp.composeapp.generated.resources.expandable
import knowledgeretrievalkmp.composeapp.generated.resources.menu
import knowledgeretrievalkmp.composeapp.generated.resources.upload
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
        ColumnWithScrollbar(
            boxModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            detailViewModel.detailUiState.knowledgeBase.value.documents.forEach { document ->
                DocumentItem(
                    document = document,
                    showDocumentDeleteOption = detailViewModel.detailUiState.showDocumentDeleteOption.value,
                    onDocumentDelete = {
                        detailViewModel.detailUiState.showDocumentDeleteOption.value = false
                        detailViewModel.detailUiState.showDialogAction.value =
                            DetailShowDialogAction.DeleteDocumentConfirmation(document)
                    }
                )
            }
            Spacer(Modifier)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                .offset(x = -Dimens.fab_icon_size)
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