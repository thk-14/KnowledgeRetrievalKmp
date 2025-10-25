package com.thk.knowledgeretrievalkmp.ui.view.chat.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.thk.knowledgeretrievalkmp.data.network.NetworkMessageRole
import com.thk.knowledgeretrievalkmp.db.Message
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.theme.LightGreen
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.chat.ChatViewModel
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.TypingDots
import knowledgeretrievalkmp.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun NavChatScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    Scaffold(
        bottomBar = {
            NavChatBottomBar(
                textFieldState = chatViewModel.chatUiState.chatInputState,
                numSource = chatViewModel.chatUiState.knowledgeBase.value.documents.size,
                webSearch = chatViewModel.chatUiState.webSearch.value,
                onWebSearchChange = {
                    chatViewModel.chatUiState.webSearch.value = !chatViewModel.chatUiState.webSearch.value
                    val webSearchStatusText =
                        if (chatViewModel.chatUiState.webSearch.value) "On" else "Off"
                    coroutineScope.launch {
                        chatViewModel.chatUiState.snackBarHostState.showSnackbar(
                            message = "Web search: $webSearchStatusText",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onSendMessage = {
                    chatViewModel.sendUserRequestWithSSE()
                    // clear chat input text
                    chatViewModel.chatUiState.chatInputState.clearText()
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        val lastIndex =
                            chatViewModel.chatUiState.knowledgeBase.value.conversation.value.messages.size - 1
                        if (lastIndex >= 0) {
                            lazyListState.animateScrollToItem(lastIndex)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        },
        modifier = modifier
    ) { contentPadding ->
        if (chatViewModel.chatUiState.knowledgeBase.value.conversation.value.messages.isNotEmpty()) {
            ChatContent(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                lazyListState = lazyListState,
                messages = chatViewModel.chatUiState.knowledgeBase.value.conversation.value.messages
            )
        } else {
            // Placeholder
            Row(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hello ${chatViewModel.displayName.value}",
                    fontSize = 40.sp,
                    lineHeight = 40.sp
                )
            }
        }
    }
}

@Composable
fun NavChatBottomBar(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    numSource: Int,
    webSearch: Boolean,
    onSendMessage: () -> Unit,
    onWebSearchChange: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatTextField(
            textFieldState = textFieldState,
            onSendMessage = onSendMessage,
            numSource = numSource,
            webSearch = webSearch,
            onWebSearchChange = onWebSearchChange
        )
    }
}

@Composable
fun ChatTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    numSource: Int,
    webSearch: Boolean,
    onSendMessage: () -> Unit,
    onWebSearchChange: () -> Unit
) {
    val screenWidth = LocalWindowSize.current.width
    OutlinedCard(
        border = BorderStroke(1.dp, Black),
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val webSearchColor by animateColorAsState(
                    if (webSearch) LightGreen else Gray50
                )
                Icon(
                    imageVector = vectorResource(Res.drawable.world),
                    contentDescription = null,
                    tint = webSearchColor,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onWebSearchChange()
                        }
                )
                TextField(
                    state = textFieldState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    placeholder = {
                        Text(stringResource(Res.string.chat_placeholder, numSource))
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .width(0.5 * screenWidth),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.document),
                        contentDescription = null,
                        tint = Gray50,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = numSource.toString(),
                        fontSize = 15.sp,
                        color = Gray50
                    )
                }

                Button(
                    onClick = onSendMessage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightGreen,
                        contentColor = White
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 5.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.send),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    messages: SnapshotStateList<Message>,
    lazyListState: LazyListState
) {
    val screenHeight = LocalWindowSize.current.height

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(0.02 * screenHeight)
    ) {
        items(
            items = messages,
            key = { it.MessageId })
        { message ->
            when (message.Role) {
                NetworkMessageRole.USER -> UserMessage(
                    message = message,
                    modifier = Modifier.animateItem()
                )

                NetworkMessageRole.AGENT ->
                    if (message.Content.isNotEmpty())
                        ServerMessage(
                            message = message,
                            modifier = Modifier.animateItem()
                        )
                    else {
                        TypingDots()
                    }
            }
        }
    }
}

@Composable
fun UserMessage(
    modifier: Modifier = Modifier,
    message: Message,
) {
    val screenWidth = LocalWindowSize.current.width

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = LightGreen,
                contentColor = White
            ),
            modifier = Modifier
                .sizeIn(maxWidth = screenWidth / 2)
                .wrapContentWidth()
                .padding(end = 5.dp)
        ) {
            Text(
                text = message.Content,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ServerMessage(
    modifier: Modifier = Modifier,
    message: Message
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = message.Content,
            fontSize = 20.sp,
            color = Black,
            modifier = Modifier.padding(8.dp)
        )
    }
}