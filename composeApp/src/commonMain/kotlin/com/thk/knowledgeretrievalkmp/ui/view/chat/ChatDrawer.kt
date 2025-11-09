package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.back
import knowledgeretrievalkmp.composeapp.generated.resources.chat
import knowledgeretrievalkmp.composeapp.generated.resources.menu
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatDrawer(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    onNavigateToKnowledgeBase: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Surface(
        color = White,
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(start = Dimens.padding_horizontal)
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            chatViewModel.chatUiState.drawerState.close()
                        }
                    }
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.menu),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.top_bar_icon_size),
                    )
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray50
            )
            NavigationDrawerItem(
                label = {
                    Text(
                        text = "New Chat",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                icon = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.chat),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.top_bar_icon_size),
                    )
                },
                selected = false,
                onClick = {
                    chatViewModel.chatUiState.activeConversationId.value = ""
                    coroutineScope.launch {
                        chatViewModel.chatUiState.drawerState.close()
                    }
                },
                modifier = Modifier.padding(
                    start = Dimens.padding_horizontal
                )
            )
            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Back To Knowledge Base",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                icon = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.back),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.top_bar_icon_size),
                    )
                },
                selected = false,
                onClick = onNavigateToKnowledgeBase,
                modifier = Modifier.padding(
                    start = Dimens.padding_horizontal
                )
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray50
            )
            Text(
                text = "Recent",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier.padding(
                    start = Dimens.padding_horizontal,
                    top = 10.dp,
                )
            )
            LazyColumn(
                modifier = Modifier.padding(
                    start = Dimens.padding_horizontal
                )
            ) {
                items(
                    items = chatViewModel.chatUiState.conversations,
                    key = { it.conversation.value.ConversationId }
                ) { conversation ->
                    val interactionSource = remember { MutableInteractionSource() }
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collectLatest { interaction ->
                            if (interaction is PressInteraction.Press) {
                                // long-press duration
                                delay(500)
                                // If the coroutine is still active, it means the press was held
                                chatViewModel.chatUiState.showDialogAction.value =
                                    ChatShowDialogAction.ShowConversationBottomSheet(conversation)
                            }
                        }
                    }
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = conversation.conversation.value.Name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.4f)
                            )
                        },
                        selected =
                            conversation.conversation.value.ConversationId == chatViewModel.chatUiState.activeConversationId.value,
                        onClick = {
                            chatViewModel.chatUiState.activeConversationId.value =
                                conversation.conversation.value.ConversationId
                        },
                        interactionSource = interactionSource
                    )
                }
            }
        }
    }
}