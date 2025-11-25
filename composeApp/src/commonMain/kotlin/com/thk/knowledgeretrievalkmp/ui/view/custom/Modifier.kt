package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

fun Modifier.sizeIn(
    minWidth: Float = 0f,
    minHeight: Float = 0f,
    maxWidth: Float = 1f,
    maxHeight: Float = 1f
) = this.layout { measurable, constraints ->
    val targetMinWidth =
        if (constraints.hasBoundedWidth) (constraints.maxWidth * minWidth).roundToInt() else constraints.minWidth
    val targetMaxWidth =
        if (constraints.hasBoundedWidth) (constraints.maxWidth * maxWidth).roundToInt() else constraints.maxWidth
    val targetMinHeight =
        if (constraints.hasBoundedHeight) (constraints.maxHeight * minHeight).roundToInt() else constraints.minHeight
    val targetMaxHeight =
        if (constraints.hasBoundedHeight) (constraints.maxHeight * maxHeight).roundToInt() else constraints.maxHeight

    val newConstraints = constraints.copy(
        minWidth = targetMinWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
        maxWidth = targetMaxWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
        minHeight = targetMinHeight.coerceIn(constraints.minHeight, constraints.maxHeight),
        maxHeight = targetMaxHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
    )

    val placeable = measurable.measure(newConstraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, 0)
    }
}