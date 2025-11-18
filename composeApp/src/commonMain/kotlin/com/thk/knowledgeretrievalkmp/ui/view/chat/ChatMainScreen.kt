package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.rememberMarkdownState
import com.thk.knowledgeretrievalkmp.data.local.db.MessageWithCitations
import com.thk.knowledgeretrievalkmp.data.network.NetworkMessageRole
import com.thk.knowledgeretrievalkmp.data.network.SseErrorData
import com.thk.knowledgeretrievalkmp.data.network.SseStartData
import com.thk.knowledgeretrievalkmp.data.network.SseStopData
import com.thk.knowledgeretrievalkmp.db.Citation
import com.thk.knowledgeretrievalkmp.db.Message
import com.thk.knowledgeretrievalkmp.ui.theme.*
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.InfiniteLoadingCircle
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.UiKnowledgeBase
import knowledgeretrievalkmp.composeapp.generated.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatMainScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val activeKb by remember {
        derivedStateOf {
            chatViewModel.chatUiState.kbs.firstOrNull {
                it.kb.value.KbId == chatViewModel.chatUiState.activeKbId.value
            }
        }
    }
    val activeConversation by remember {
        derivedStateOf {
            chatViewModel.chatUiState.conversations.firstOrNull {
                it.conversation.value.ConversationId == chatViewModel.chatUiState.activeConversationId.value
            }
        }
    }

    fun scrollToLast() {
        coroutineScope.launch(Dispatchers.Main) {
            val numMessages = activeConversation?.messagesWithCitations?.size ?: return@launch
            val lastIndex = numMessages - 1
            if (lastIndex >= 0) {
                lazyListState.animateScrollToItem(lastIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                modifier = modifier.padding(start = Dimens.padding_horizontal),
                title = activeConversation?.conversation?.value?.Name ?: "",
                onDrawerOpen = {
                    coroutineScope.launch {
                        chatViewModel.chatUiState.drawerState.open()
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomBar(
                textFieldState = chatViewModel.chatUiState.chatInputState,
                numSource = activeKb?.documents?.size ?: 0,
                webSearch = chatViewModel.chatUiState.webSearch.value,
                kbs = chatViewModel.chatUiState.kbs,
                activeKb = activeKb,
                onActiveKbChange = { kbId ->
                    chatViewModel.chatUiState.activeKbId.value = kbId
                    chatViewModel.toggleKbActive(
                        chatViewModel.chatUiState.activeKbId.value,
                        true
                    )
                },
                onWebSearchChange = {
                    chatViewModel.chatUiState.webSearch.value = !chatViewModel.chatUiState.webSearch.value
                    val webSearchStatusText =
                        if (chatViewModel.chatUiState.webSearch.value) "On" else "Off"
                    chatViewModel.showSnackbar(
                        "Web search: $webSearchStatusText"
                    )
                },
                onSendMessage = {
                    chatViewModel.sendUserRequestWithSSE { sseData ->
                        when (sseData) {
                            is SseStartData -> {
                                scrollToLast()
                            }

                            is SseStopData -> {
                                scrollToLast()
                            }

                            is SseErrorData -> {
                                chatViewModel.showSnackbar("An error occurred.")
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    }
                    // clear chat input text
                    chatViewModel.chatUiState.chatInputState.clearText()
                    focusManager.clearFocus()
                    scrollToLast()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        },
        modifier = modifier
    ) { contentPadding ->
        if (activeConversation == null || activeConversation!!.messagesWithCitations.isEmpty()) {
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
        } else {
            SelectionContainer {
                ChatContent(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize(),
                    lazyListState = lazyListState,
                    messagesWithCitations = activeConversation!!.messagesWithCitations,
                    onCitationClick = { citation ->
                        chatViewModel.chatUiState.showDialogAction.value =
                            ChatShowDialogAction.ShowMessageBottomSheet(
                                header = citation.OriginalFileName,
                                body = citation.PageContent
                            )
                    }
                )
            }
        }
    }
}

@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    title: String,
    onDrawerOpen: () -> Unit,
) {
    val screenWidth = LocalWindowSize.current.width
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(30.dp),
        modifier = modifier
    ) {
        IconButton(
            onClick = onDrawerOpen
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.menu),
                contentDescription = null,
                modifier = Modifier.size(Dimens.top_bar_icon_size),
            )
        }
        Text(
            text = title,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sizeIn(maxWidth = screenWidth * 0.7f)
        )
    }
}

@Composable
fun ChatBottomBar(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    numSource: Int,
    webSearch: Boolean,
    kbs: SnapshotStateList<UiKnowledgeBase>,
    activeKb: UiKnowledgeBase?,
    onActiveKbChange: (String) -> Unit,
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
            numSource = numSource,
            webSearch = webSearch,
            kbs = kbs,
            activeKb = activeKb,
            onActiveKbChange = onActiveKbChange,
            onSendMessage = onSendMessage,
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
    kbs: SnapshotStateList<UiKnowledgeBase>,
    activeKb: UiKnowledgeBase?,
    onActiveKbChange: (String) -> Unit,
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
        Column {
            // Text Field line
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
            // Kb line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(
                    start = 10.dp,
                    bottom = 10.dp
                )
            ) {
                var isExpanded by remember { mutableStateOf(false) }
                Text(
                    text = "Knowledge Base: ",
                    color = Gray50
                )
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = {
                        isExpanded = it
                    }
                ) {
                    Text(
                        text = activeKb?.kb?.value?.Name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth(0.5f)
                            .height(35.dp)
                            .border(
                                width = 1.dp,
                                color = Black,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(
                                start = 5.dp,
                                top = 5.dp
                            )
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        shape = MaterialTheme.shapes.medium,
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        matchAnchorWidth = true
                    ) {
                        kbs.forEach { kb ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = kb.kb.value.Name
                                    )
                                },
                                onClick = {
                                    onActiveKbChange(kb.kb.value.KbId)
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                modifier = Modifier.border(
                                    width = 1.dp,
                                    color = Black,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                    .fillMaxWidth()
                                    .background(
                                        color = White
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    messagesWithCitations: SnapshotStateList<MessageWithCitations>,
    lazyListState: LazyListState,
    onCitationClick: (Citation) -> Unit
) {
    val screenHeight = LocalWindowSize.current.height

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(0.02 * screenHeight)
    ) {
        items(
            items = messagesWithCitations,
            key = { it.message.MessageId })
        { messageWithCitations ->
            val message = messageWithCitations.message
            when (message.Role) {
                NetworkMessageRole.USER -> UserMessage(
                    message = message,
//                    modifier = Modifier.animateItem()
                )

                NetworkMessageRole.AGENT ->
                    if (message.Content.isNotEmpty())
                        ServerMessage(
                            message = message,
                            onCitationClick = { citationIndex ->
                                val citation = messageWithCitations.citations.firstOrNull {
                                    it.OriginalIndex == citationIndex.toLong()
                                }
                                if (citation != null) {
                                    onCitationClick(citation)
                                }
                            }
//                            modifier = Modifier.animateItem()
                        )
                    else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
//                            TypingDots(dotSize = 8.dp)
                            InfiniteLoadingCircle(size = 30.dp, strokeWidth = 3.dp)
                            Text(
                                text = message.Status,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
//                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ServerMessage(
    modifier: Modifier = Modifier,
    message: Message,
    onCitationClick: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Start
    ) {
        val markdownState = rememberMarkdownState(
            content = message.Content,
            retainState = true
        )
        Markdown(
            markdownState = markdownState,
            colors = markdownColor(
                codeBackground = White,
                tableBackground = White,
                inlineCodeBackground = White
            ),
            imageTransformer = Coil3ImageTransformerImpl,
            modifier = Modifier.padding(8.dp).sizeIn(minHeight = 50.dp),
            components = markdownComponents(
                paragraph = { model ->
                    val regex =
                        """(\[[^\]]*\])|(\*\*\*(.*?)\*\*\*)|(\*\*(.*?)\*\*)|(\*(.*?)\*)|([^\[\*]+)""".toRegex()
                    val matches = regex.findAll(
                        model.content.substring(
                            model.node.startOffset,
                            model.node.endOffset
                        )
                    )
                    val text = buildAnnotatedString {
                        matches.forEach { match ->
                            when {
                                match.value.startsWith("[") -> {
                                    // citation
                                    val linkListener = LinkInteractionListener {
                                        val citationIndex = match.value.drop(1).dropLast(1).toIntOrNull()
                                        if (citationIndex != null) onCitationClick(citationIndex)
                                    }
                                    withLink(
                                        link = LinkAnnotation.Url(
                                            url = match.value,
                                            // Optional: Apply styles for different states (focused, hovered, pressed)
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    color = Blue
                                                ),
                                                focusedStyle = SpanStyle(
                                                    color = Blue
                                                ),
                                                hoveredStyle = SpanStyle(
                                                    color = Blue,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ),
                                            linkInteractionListener = linkListener
                                        )
                                    ) {
                                        append(match.value)
                                    }
                                }

                                match.value.startsWith("***") -> {
                                    // bold and italic
                                    val content = match.value.drop(3).dropLast(3)
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic
                                        )
                                    ) {
                                        append(content)
                                    }
                                }

                                match.value.startsWith("**") -> {
                                    // bold
                                    val content = match.value.drop(2).dropLast(2)
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(content)
                                    }
                                }

                                match.value.startsWith("*") -> {
                                    // italic
                                    val content = match.value.drop(1).dropLast(1)
                                    withStyle(
                                        style = SpanStyle(
                                            fontStyle = FontStyle.Italic
                                        )
                                    ) {
                                        append(content)
                                    }
                                }

                                else -> {
                                    // normal
                                    append(match.value)
                                }
                            }
                        }
                    }
                    Text(
                        text = text
                    )
                }
            )
        )
    }
}