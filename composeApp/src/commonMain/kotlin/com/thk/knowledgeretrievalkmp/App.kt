package com.thk.knowledgeretrievalkmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.thk.knowledgeretrievalkmp.data.AppContainer
import com.thk.knowledgeretrievalkmp.data.local.db.createDatabase
import com.thk.knowledgeretrievalkmp.data.local.db.createDriver
import com.thk.knowledgeretrievalkmp.ui.KnowledgeRetrievalNavGraph
import com.thk.knowledgeretrievalkmp.ui.theme.KnowledgeRetrievalTheme
import com.thk.knowledgeretrievalkmp.util.log
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) {
        AppContainer.db = createDatabase(createDriver())
        log("database created")
    }
    KnowledgeRetrievalTheme {
        KnowledgeRetrievalNavGraph()
    }
}