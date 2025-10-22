package com.thk.knowledgeretrievalkmp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.thk.knowledgeretrievalkmp.data.AppContainer

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AppContainer.googleAuthProvider
    ComposeViewport {
        App()
    }
}