package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.rememberMarkdownState
import com.thk.knowledgeretrievalkmp.ui.theme.Blue
import com.thk.knowledgeretrievalkmp.ui.theme.White
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.delete
import knowledgeretrievalkmp.composeapp.generated.resources.edit
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ConversationBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        },
        containerColor = White,
        sheetMaxWidth = 200.dp,
        sheetGesturesEnabled = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .clickable {
                        onRename()
                    }
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.edit),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.top_bar_icon_size),
                )
                Text("Rename")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .clickable {
                        onDelete()
                    }
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.delete),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.top_bar_icon_size),
                )
                Text("Delete")
            }
        }
    }
}

@Composable
fun MessageBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    header: String,
    body: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        },
        containerColor = White,
        sheetGesturesEnabled = true
    ) {
        SelectionContainer {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxHeight(0.5f)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 10.dp)
            ) {
                val headerText = buildAnnotatedString {
                    if (header.startsWith("http")) {
                        withLink(
                            link = LinkAnnotation.Url(
                                url = header,
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
                                )
                            )
                        ) {
                            append(header)
                        }
                    } else {
                        append(header)
                    }
                }
                Text(
                    text = headerText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    val markdownState = rememberMarkdownState(
                        content = body,
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
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }
        }
    }
}