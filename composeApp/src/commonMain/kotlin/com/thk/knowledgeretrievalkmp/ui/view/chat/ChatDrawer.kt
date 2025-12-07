package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.theme.LightBlue
import com.thk.knowledgeretrievalkmp.ui.theme.White50
import com.thk.knowledgeretrievalkmp.ui.theme.WhiteBlue
import com.thk.knowledgeretrievalkmp.ui.view.custom.ColumnWithScrollbar
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.back
import knowledgeretrievalkmp.composeapp.generated.resources.chat
import knowledgeretrievalkmp.composeapp.generated.resources.menu
import knowledgeretrievalkmp.composeapp.generated.resources.morevert
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatDrawer(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    showDrawerButton: Boolean = true,
    showConversationMenuButton: Boolean = false,
    onNavigateToKnowledgeBase: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Surface(
        color = White50,
        shadowElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(
                top = 5.dp,
                end = 5.dp
            )
        ) {
            if (showDrawerButton) {
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
            }
            NavigationDrawerItem(
                label = {
                    Text(
                        text = "New Chat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                icon = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.chat),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.drawer_icon_size),
                    )
                },
                selected = false,
                onClick = {
                    chatViewModel.chatUiState.activeConversationId.value = ""
                    coroutineScope.launch {
                        chatViewModel.chatUiState.drawerState.close()
                    }
                },
                modifier = Modifier
                    .padding(
                        start = Dimens.padding_horizontal
                    )
                    .height(30.dp)
            )
            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Knowledge Base",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                icon = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.back),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.drawer_icon_size),
                    )
                },
                selected = false,
                onClick = onNavigateToKnowledgeBase,
                modifier = Modifier
                    .padding(
                        start = Dimens.padding_horizontal
                    )
                    .height(30.dp)
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray50
            )
            Text(
                text = "Recent",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier.padding(
                    start = Dimens.padding_horizontal,
                    top = 10.dp,
                )
            )
            ColumnWithScrollbar(
                boxModifier = Modifier.padding(
                    start = Dimens.padding_horizontal
                )
            ) {
                chatViewModel.chatUiState.conversations.forEach { conversation ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    var isMenuExpanded by remember { mutableStateOf(false) }
                    val selected =
                        conversation.conversation.value.ConversationId == chatViewModel.chatUiState.activeConversationId.value
                    val showMenuButton = isHovered || isMenuExpanded || selected

                    if (!showConversationMenuButton) {
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
                    }

                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = conversation.conversation.value.Name,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        badge = {
                            if (showConversationMenuButton) {
                                Box {
                                    if (showMenuButton) {
                                        IconButton(
                                            onClick = { isMenuExpanded = true },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = vectorResource(Res.drawable.morevert),
                                                contentDescription = null
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isMenuExpanded,
                                        onDismissRequest = { isMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text("Rename")
                                            },
                                            onClick = {
                                                isMenuExpanded = false
                                                chatViewModel.chatUiState.renameInputState.clearText()
                                                chatViewModel.chatUiState.showDialogAction.value =
                                                    ChatShowDialogAction.RenameConversation(conversation)
                                            },
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text("Delete")
                                            },
                                            onClick = {
                                                isMenuExpanded = false
                                                chatViewModel.chatUiState.showDialogAction.value =
                                                    ChatShowDialogAction.DeleteConversationConfirmation(conversation)
                                            },
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                            }
                        },
                        selected = selected,
                        onClick = {
                            chatViewModel.activateConversation(conversation.conversation.value.ConversationId)
                        },
                        interactionSource = interactionSource,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = WhiteBlue,
                            selectedTextColor = LightBlue
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}