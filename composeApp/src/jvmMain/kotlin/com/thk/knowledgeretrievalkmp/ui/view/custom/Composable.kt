@file:JvmName("ComposableDesktop")

package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

@Composable
actual fun getScreenWidthDp(): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        windowInfo.containerSize.width.toDp()
    }
}

@Composable
actual fun getScreenHeightDp(): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        windowInfo.containerSize.height.toDp()
    }
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