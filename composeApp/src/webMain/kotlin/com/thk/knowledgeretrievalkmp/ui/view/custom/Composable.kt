package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
actual fun getScreenWidthDp(): Dp {
    var screenWidth by remember {
        mutableStateOf(window.innerWidth.dp)
    }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            screenWidth = window.innerWidth.dp
        }
        window.addEventListener("resize", listener)

        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    return screenWidth
}

@Composable
actual fun getScreenHeightDp(): Dp {
    var screenHeight by remember {
        mutableStateOf(window.innerHeight.dp)
    }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            screenHeight = window.innerHeight.dp
        }
        window.addEventListener("resize", listener)

        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    return screenHeight
}