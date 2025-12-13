package com.thk.knowledgeretrievalkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.thk.knowledgeretrievalkmp.data.AppContainer
import io.github.vinceglb.filekit.FileKit
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.book_icon
import org.jetbrains.compose.resources.painterResource

fun main() {
    FileKit.init(appId = "com.thk.knowledgeretrievalkmp")
    AppContainer.googleAuthProvider
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMS",
            icon = painterResource(Res.drawable.book_icon)
        ) {
            App()
        }
    }
}