package com.thk.knowledgeretrievalkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.thk.knowledgeretrievalkmp.data.AppContainer
import io.github.vinceglb.filekit.FileKit

fun main() {
    FileKit.init(appId = "com.thk.knowledgeretrievalkmp")
    AppContainer.googleAuthProvider
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "knowledgeretrievalkmp",
        ) {
            App()
        }
    }
}