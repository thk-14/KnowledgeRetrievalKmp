package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.rememberMarkdownState
import com.thk.knowledgeretrievalkmp.data.local.db.MessageWithCitations
import com.thk.knowledgeretrievalkmp.data.network.NetworkMessageRole
import com.thk.knowledgeretrievalkmp.data.network.SseErrorData
import com.thk.knowledgeretrievalkmp.db.Citation
import com.thk.knowledgeretrievalkmp.db.Message
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Blue
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.theme.LightGreen
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.theme.White80
import com.thk.knowledgeretrievalkmp.ui.view.custom.ColumnWithScrollbar
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import com.thk.knowledgeretrievalkmp.ui.view.custom.UiKnowledgeBase
import com.thk.knowledgeretrievalkmp.ui.view.custom.sizeIn
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.agent
import knowledgeretrievalkmp.composeapp.generated.resources.chat_placeholder
import knowledgeretrievalkmp.composeapp.generated.resources.document
import knowledgeretrievalkmp.composeapp.generated.resources.menu
import knowledgeretrievalkmp.composeapp.generated.resources.send
import knowledgeretrievalkmp.composeapp.generated.resources.square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.DurationUnit

@Composable
fun ChatMainScreen(
    modifier: Modifier = Modifier,
    showDrawerButton: Boolean = true,
    chatViewModel: ChatViewModel
) {
    val scrollState = rememberScrollState()
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
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                modifier = Modifier.padding(horizontal = Dimens.padding_horizontal),
                title = activeConversation?.conversation?.value?.Name ?: "",
                showDrawerButton =
                    if (showDrawerButton) chatViewModel.chatUiState.drawerState.isClosed
                    else false,
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
                agentic = chatViewModel.chatUiState.agentic.value,
                sendEnabled = chatViewModel.chatUiState.sendEnabled.value,
                kbs = chatViewModel.chatUiState.kbs,
                activeKb = activeKb,
                onActiveKbChange = { kbId ->
                    chatViewModel.chatUiState.activeKbId.value = kbId
                    chatViewModel.toggleKbActive(kbId, true)
                },
                onAgenticChange = {
                    chatViewModel.chatUiState.agentic.value = !chatViewModel.chatUiState.agentic.value
                    val agenticStatusText =
                        if (chatViewModel.chatUiState.agentic.value) "On" else "Off"
                    chatViewModel.showSnackbar(
                        "Agentic: $agenticStatusText"
                    )
                },
                onSendMessage = sendSseMessage@{
                    if (!chatViewModel.chatUiState.sendEnabled.value) {
                        return@sendSseMessage
                    }
                    chatViewModel.chatUiState.sendEnabled.value = false
                    scrollToLast()
                    chatViewModel.sendUserRequestWithSSE(
                        onSseData = { sseData ->
                            when (sseData) {
                                is SseErrorData -> {
                                    chatViewModel.showSnackbar("An error occurred.")
                                }

                                else -> {
                                    // do nothing
                                }
                            }
                        },
                        onCompletion = { processDuration ->
                            chatViewModel.chatUiState.sendEnabled.value = true
                            scrollToLast()
                            chatViewModel.showSnackbar(
                                "Message processed in ${processDuration.toString(DurationUnit.SECONDS, 3)}"
                            )
                        }
                    )
                    // clear chat input text
                    chatViewModel.chatUiState.chatInputState.clearText()
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        bottom = 5.dp
                    ),
            )
        },
        modifier = modifier.requiredSizeIn(
            minWidth = 200.dp,
            minHeight = 200.dp
        )
    ) { contentPadding ->
        if (activeConversation == null || activeConversation!!.messagesWithCitations.isEmpty()) {
            // Placeholder
            Row(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = Dimens.padding_horizontal)
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
            ChatContent(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = Dimens.padding_horizontal)
                    .fillMaxSize(),
                scrollState = scrollState,
                messagesWithCitations = activeConversation!!.messagesWithCitations,
                onCitationClick = { citation ->
                    chatViewModel.showMessageBottomSheet(
                        header = citation.OriginalFileName,
                        body = citation.PageContent
                    )
                }
            )
        }
    }
}

@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    title: String,
    showDrawerButton: Boolean,
    onDrawerOpen: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        IconButton(
            onClick = onDrawerOpen,
            modifier = Modifier.alpha(
                if (showDrawerButton) 1.0f else 0f
            )
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.menu),
                contentDescription = null,
                modifier = Modifier.size(Dimens.top_bar_icon_size)
            )
        }
        Text(
            text = title,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(
                    end = 10.dp
                )
        )
    }
}

@Composable
fun ChatBottomBar(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    numSource: Int,
    agentic: Boolean,
    sendEnabled: Boolean,
    kbs: SnapshotStateList<UiKnowledgeBase>,
    activeKb: UiKnowledgeBase?,
    onActiveKbChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAgenticChange: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatTextField(
            textFieldState = textFieldState,
            numSource = numSource,
            agentic = agentic,
            sendEnabled = sendEnabled,
            kbs = kbs,
            activeKb = activeKb,
            onActiveKbChange = onActiveKbChange,
            onSendMessage = onSendMessage,
            onAgenticChange = onAgenticChange,
            modifier = Modifier.requiredSizeIn(
                minWidth = 300.dp,
                maxHeight = 300.dp
            )
        )
    }
}

@Composable
fun ChatTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    numSource: Int,
    agentic: Boolean,
    sendEnabled: Boolean,
    kbs: SnapshotStateList<UiKnowledgeBase>,
    activeKb: UiKnowledgeBase?,
    onActiveKbChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAgenticChange: () -> Unit
) {
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
                    val agenticColor by animateColorAsState(
                        if (agentic) LightGreen else Gray50
                    )
                    Icon(
                        imageVector = vectorResource(Res.drawable.agent),
                        contentDescription = null,
                        tint = agenticColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .size(40.dp)
                            .clickable {
                                onAgenticChange()
                            }
                    )
                    TextField(
                        state = textFieldState,
                        lineLimits = TextFieldLineLimits.MultiLine(),
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.chat_placeholder, numSource),
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxWidth(0.5f)
                            .onPreviewKeyEvent { event ->
                                if (event.key.keyCode == Key.Enter.keyCode && event.type == KeyEventType.KeyDown) {
                                    if (event.isShiftPressed) {
                                        textFieldState.edit {
                                            append("\n")
                                        }
                                    } else {
                                        onSendMessage()
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        onKeyboardAction = {
                            onSendMessage()
                        }
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
                            containerColor = if (sendEnabled) LightGreen else White80,
                            contentColor = if (sendEnabled) White else Gray50
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 5.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = vectorResource(
                                if (sendEnabled) Res.drawable.send else Res.drawable.square
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(
                                if (sendEnabled) 20.dp else 15.dp
                            )
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
                    color = Gray50,
                    fontSize = 15.sp
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
                        fontSize = 15.sp,
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth(0.5f)
                            .height(30.dp)
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
                                        text = kb.kb.value.Name,
                                        fontSize = 15.sp
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
    scrollState: ScrollState,
    messagesWithCitations: SnapshotStateList<MessageWithCitations>,
    onCitationClick: (Citation) -> Unit
) {
    ColumnWithScrollbar(
        scrollState = scrollState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        boxModifier = modifier
    ) {
        messagesWithCitations.forEach { messageWithCitations ->
            val message = messageWithCitations.message
            when (message.Role) {
                NetworkMessageRole.USER ->
                    SelectionContainer {
                        UserMessage(
                            message = message
                        )
                    }

                NetworkMessageRole.AGENT ->
                    SelectionContainer {
                        Column {
                            if (message.StatusPhase != null) {
                                ServerMessageStatusHeader(
                                    statusPhase = message.StatusPhase,
                                    statusMessage = message.StatusMessage ?: ""
                                )
                            }
                            ServerMessageBody(
                                content = message.Content,
                                onCitationClick = { citationIndex ->
                                    val citation = messageWithCitations.citations.firstOrNull {
                                        it.CitationIndex == citationIndex.toLong()
                                    }
                                    if (citation != null) {
                                        onCitationClick(citation)
                                    }
                                }
                            )
                        }
                    }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }

//    LazyColumn(
//        modifier = modifier,
//        state = lazyListState,
//        verticalArrangement = Arrangement.spacedBy(0.02 * screenHeight)
//    ) {
//        items(
//            items = messagesWithCitations,
//            key = { it.message.MessageId })
//        { messageWithCitations ->
//            val message = messageWithCitations.message
//            when (message.Role) {
//                NetworkMessageRole.USER ->
//                    UserMessage(
//                        message = message
//                    )
//
//                NetworkMessageRole.AGENT ->
//                    Column {
//                        if (message.StatusPhase != null) {
//                            ServerMessageStatusHeader(
//                                statusPhase = message.StatusPhase,
//                                statusMessage = message.StatusMessage ?: ""
//                            )
//                        }
//                        ServerMessageBody(
//                            content = message.Content,
//                            onCitationClick = { citationIndex ->
//                                val citation = messageWithCitations.citations.firstOrNull {
//                                    it.OriginalIndex == citationIndex.toLong()
//                                }
//                                if (citation != null) {
//                                    onCitationClick(citation)
//                                }
//                            }
//                        )
//                    }
//            }
//        }
//
//        item {
//            Spacer(
//                modifier = Modifier.height(20.dp)
//            )
//        }
//    }
}

@Composable
fun UserMessage(
    modifier: Modifier = Modifier,
    message: Message,
) {
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
                .sizeIn(maxWidth = 0.5f)
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
fun ServerMessageStatusHeader(
    modifier: Modifier = Modifier,
    statusPhase: String,
    statusMessage: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
//        TypingDots(dotSize = 8.dp)
//        InfiniteLoadingCircle(size = 30.dp, strokeWidth = 3.dp)
        LottieAnimation(
            lottieFilePath = LottieAnimation.THINKING.lottieFilePath,
            speed = 3f,
            modifier = Modifier.size(40.dp)
        )
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = statusPhase,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AnimatedVisibility(
                visible = statusMessage.isNotEmpty()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    VerticalDivider(
                        thickness = 1.dp,
                        color = Gray50
                    )
                    val markdownState = rememberMarkdownState(
                        content = statusMessage,
                        retainState = true
                    )
                    Markdown(
                        markdownState = markdownState,
                        colors = markdownColor(
                            codeBackground = White,
                            tableBackground = White,
                            inlineCodeBackground = White
                        ),
                        imageTransformer = Coil3ImageTransformerImpl
                    )
                }
            }
        }
    }
}

@Composable
fun ServerMessageBody(
    modifier: Modifier = Modifier,
    content: String,
    onCitationClick: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Start
    ) {
        val markdownState = rememberMarkdownState(
            content = content,
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
                        """(\[([^\]]*[a-zA-Z][^\]]*)\]\[(\d+)\])|(\[[^\]]*\])|(\*\*\*(.*?)\*\*\*)|(\*\*(.*?)\*\*)|(\*(.*?)\*)|([^\[\*]+)""".toRegex()
                    val matches = regex.findAll(
                        model.content.substring(
                            model.node.startOffset,
                            model.node.endOffset
                        )
                    )
                    val text = buildAnnotatedString {
                        matches.forEach { match ->
                            val value = match.value
                            val groups = match.groupValues
                            when {
                                groups[1].isNotEmpty() -> {
                                    // link reference
                                    val label = groups[2]
                                    val citationIndex = groups[3].toIntOrNull()
                                    val linkListener = LinkInteractionListener {
                                        if (citationIndex != null) onCitationClick(citationIndex)
                                    }
                                    withLink(
                                        link = LinkAnnotation.Url(
                                            url = label,
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
                                        append(label)
                                    }
                                }

                                groups[4].isNotEmpty() -> {
                                    // document reference
                                    var label = groups[4]
                                    var labelContent = label.drop(1).dropLast(1)
                                    if(labelContent.uppercase().startsWith("CITATION: ")) {
                                        labelContent = labelContent.drop(10)
                                        label = "[$labelContent]"
                                    }
                                    val citationIndex = labelContent.toIntOrNull()
                                    val linkListener = LinkInteractionListener {
                                        if (citationIndex != null) onCitationClick(citationIndex)
                                    }
                                    withLink(
                                        link = LinkAnnotation.Url(
                                            url = label,
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
                                        append(label)
                                    }
                                }

                                groups[5].isNotEmpty() -> {
                                    // bold and italic
                                    val content = groups[6]
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic
                                        )
                                    ) {
                                        append(content)
                                    }
                                }

                                groups[7].isNotEmpty() -> {
                                    // bold
                                    val content = groups[8]
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(content)
                                    }
                                }

                                groups[9].isNotEmpty() -> {
                                    // italic
                                    val content = groups[10]
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
                                    append(value)
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