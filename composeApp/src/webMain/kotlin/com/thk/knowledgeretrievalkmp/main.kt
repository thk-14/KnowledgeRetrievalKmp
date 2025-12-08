package com.thk.knowledgeretrievalkmp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        var showApp by remember { mutableStateOf(exchangeCode != null) }

        if (showApp) {
            App(
                exchangeCode = exchangeCode
            )
        } else {
            LandingPage(
                onLoginClick = {
                    showApp = true
                }
            )
        }
    }
}