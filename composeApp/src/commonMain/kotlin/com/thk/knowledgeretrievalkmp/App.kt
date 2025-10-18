package com.thk.knowledgeretrievalkmp

import androidx.compose.runtime.Composable
import com.thk.knowledgeretrievalkmp.ui.KnowledgeRetrievalNavGraph
import com.thk.knowledgeretrievalkmp.ui.theme.KnowledgeRetrievalTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    KnowledgeRetrievalTheme {
        KnowledgeRetrievalNavGraph()
    }
}