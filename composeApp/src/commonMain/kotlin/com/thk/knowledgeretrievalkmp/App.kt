package com.thk.knowledgeretrievalkmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thk.knowledgeretrievalkmp.ui.KnowledgeRetrievalNavGraph
import com.thk.knowledgeretrievalkmp.ui.theme.KnowledgeRetrievalTheme
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.WindowSize
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    exchangeCode: String? = null
) {
    KnowledgeRetrievalTheme {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(White).safeDrawingPadding()
        ) {
            val windowSize = WindowSize(
                width = this.maxWidth,
                height = this.maxHeight
            )
            CompositionLocalProvider(LocalWindowSize provides windowSize) {
                KnowledgeRetrievalNavGraph(
                    exchangeCode = exchangeCode
                )
            }
        }
    }
}