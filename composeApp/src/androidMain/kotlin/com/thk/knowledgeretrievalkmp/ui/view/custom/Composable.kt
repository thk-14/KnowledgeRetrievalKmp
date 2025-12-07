@file:JvmName("ComposableAndroid")

package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun getScreenWidthDp(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

@Composable
actual fun getScreenHeightDp(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}

@Composable
actual fun ActualVerticalScrollbar(
    scrollState: ScrollState,
    reverseLayout: Boolean,
    modifier: Modifier
) {
}