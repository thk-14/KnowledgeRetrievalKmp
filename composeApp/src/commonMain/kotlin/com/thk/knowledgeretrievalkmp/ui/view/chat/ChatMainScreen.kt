package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.view.chat.navigation.ChatBottomNavBar
import com.thk.knowledgeretrievalkmp.ui.view.chat.navigation.ChatNavGraph
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import knowledgeretrievalkmp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatMainScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    onBackPressed: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val navController = rememberNavController()

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
                ChatTopBar(
                    chatViewModel = chatViewModel,
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
        },
        bottomBar = {
            ChatBottomNavBar(
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    ) { paddingValues ->
        ChatNavGraph(
            modifier = Modifier
                .padding(horizontal = Dimens.padding_horizontal)
                .padding(paddingValues)
                .fillMaxSize(),
            navController = navController,
            chatViewModel = chatViewModel
        )
    }
}

@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
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
            Image(
                imageVector = vectorResource(Res.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .size(Dimens.top_bar_icon_size)
                    .clickable { onBackPressed() }
            )
            with(sharedTransitionScope) {
                // Title
                Text(
                    text = chatViewModel.chatUiState.knowledgeBase.value.kb.value.Name,
                    color = Black,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(
                            key = chatViewModel.chatUiState.knowledgeBase.value.kb.value.KbId
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
                    .size(Dimens.top_bar_icon_size)
                    .clickable {
                        chatViewModel.chatUiState.apply {
                            kbMenuExpanded.value = !kbMenuExpanded.value
                        }
                    }
            )
            DropdownMenu(
                expanded = chatViewModel.chatUiState.kbMenuExpanded.value,
                onDismissRequest = {
                    chatViewModel.chatUiState.kbMenuExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.rename_btn))
                    },
                    onClick = {
                        chatViewModel.chatUiState.kbMenuExpanded.value = false
                        chatViewModel.chatUiState.renameInputState.clearText()
                        chatViewModel.chatUiState.showDialogAction.value =
                            ChatShowDialogAction.RenameKB
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.delete_btn))
                    },
                    onClick = {
                        chatViewModel.chatUiState.kbMenuExpanded.value = false
                        chatViewModel.chatUiState.showDialogAction.value =
                            ChatShowDialogAction.DeleteKbConfirmation
                    }
                )
            }
        }
    }
}