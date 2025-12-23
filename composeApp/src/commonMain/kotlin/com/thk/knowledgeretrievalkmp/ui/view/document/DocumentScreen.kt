package com.thk.knowledgeretrievalkmp.ui.view.document

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.view.custom.ColumnWithScrollbar
import com.thk.knowledgeretrievalkmp.ui.view.custom.DefaultMarkdown
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.back
import org.jetbrains.compose.resources.vectorResource

@Composable
fun DocumentScreen(
    modifier: Modifier = Modifier,
    documentViewModel: DocumentViewModel = viewModel(factory = DocumentViewModel.Factory),
    onBackPressed: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = documentViewModel.snackBarHostState)
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
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
                    Text(
                        text = documentViewModel.title.value,
                        color = Black,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Gray50
                )
            }
        }
    ) { paddingValues ->
        ColumnWithScrollbar(
            modifier = Modifier.padding(paddingValues)
        ) {
            if (documentViewModel.content.value.isEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LottieAnimation(
                        lottieFilePath = LottieAnimation.LOADING.lottieFilePath,
                        speed = 2f
                    )
                }
            } else {
                SelectionContainer {
                    DefaultMarkdown(
                        modifier = Modifier.padding(8.dp).sizeIn(minHeight = 50.dp),
                        content = documentViewModel.content.value
                    )
                }
            }
        }
    }
}