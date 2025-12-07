package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@Composable
actual fun ActualVerticalScrollbar(
    scrollState: ScrollState,
    reverseLayout: Boolean,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        reverseLayout = reverseLayout,
        modifier = modifier
    )
}