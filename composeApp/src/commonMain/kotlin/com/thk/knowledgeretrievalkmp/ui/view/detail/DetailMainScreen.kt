package com.thk.knowledgeretrievalkmp.ui.view.detail

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import knowledgeretrievalkmp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun DetailMainScreen(
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel,
    onBackPressed: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToDocument: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                DetailTopBar(
                    detailViewModel = detailViewModel,
                    onBackPressed = onBackPressed,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    modifier = Modifier
                        .padding(horizontal = Dimens.padding_horizontal)
                        .fillMaxWidth()
                        .height(Dimens.top_bar_height)
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Gray50
                )
            }
        }
    ) { paddingValues ->
        DocumentSection(
            detailViewModel = detailViewModel,
            onNavigateToChat = onNavigateToChat,
            onNavigateToDocument = onNavigateToDocument,
            modifier = Modifier
                .padding(horizontal = Dimens.padding_horizontal)
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@Composable
fun DetailTopBar(
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel,
    onBackPressed: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onBackPressed
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(Dimens.top_bar_icon_size)
                )
            }
            with(sharedTransitionScope) {
                // Title
                Text(
                    text = detailViewModel.detailUiState.knowledgeBase.value.kb.value.Name,
                    color = Black,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(
                            key = detailViewModel.detailUiState.knowledgeBase.value.kb.value.KbId
                        ),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
            }
        }
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
                            kbMenuExpanded.value = !kbMenuExpanded.value
                        }
                    }
            )
            DropdownMenu(
                expanded = detailViewModel.detailUiState.kbMenuExpanded.value,
                onDismissRequest = {
                    detailViewModel.detailUiState.kbMenuExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.rename_btn))
                    },
                    onClick = {
                        detailViewModel.detailUiState.kbMenuExpanded.value = false
                        detailViewModel.showRenameKbDialog()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                )
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.delete_btn))
                    },
                    onClick = {
                        detailViewModel.detailUiState.kbMenuExpanded.value = false
                        detailViewModel.showDeleteKbDialog()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}