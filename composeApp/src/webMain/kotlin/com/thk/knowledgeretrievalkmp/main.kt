package com.thk.knowledgeretrievalkmp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.thk.knowledgeretrievalkmp.data.AppContainer
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AppContainer.googleAuthProvider
    val params = URLSearchParams(window.location.search)
    val exchangeCode = params.get("code")
    ComposeViewport {
        App(
            exchangeCode = exchangeCode
        )
    }
}