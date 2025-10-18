package com.thk.knowledgeretrievalkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() {
    FileKit.init(appId = "com.thk.knowledgeretrievalkmp")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "knowledgeretrievalkmp",
        ) {
            App()
        }
    }
}