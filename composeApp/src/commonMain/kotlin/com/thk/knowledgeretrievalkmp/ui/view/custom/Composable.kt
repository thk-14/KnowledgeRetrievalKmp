package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mmk.kmpauth.google.GoogleUser
import com.thk.knowledgeretrievalkmp.ui.theme.*
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.google
import knowledgeretrievalkmp.composeapp.generated.resources.google_btn
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
expect fun getScreenWidthDp(): Dp

@Composable
expect fun getScreenHeightDp(): Dp

data class WindowSize(
    val width: Dp = Dp.Unspecified,
    val height: Dp = Dp.Unspecified
)

val LocalWindowSize = staticCompositionLocalOf { WindowSize() }

@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    onGoogleSignInResult: (GoogleUser?) -> Unit,
    iconSize: Dp = 50.dp,
    textFontSize: TextUnit = 20.sp
) {
    GoogleButtonUiContainer(onGoogleSignInResult = onGoogleSignInResult) {
        Button(
            onClick = { this.onClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = White, contentColor = Black
            ),
            modifier = modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = vectorResource(Res.drawable.google),
                    contentDescription = stringResource(Res.string.google_btn),
                    modifier = Modifier.size(iconSize, iconSize)
                )
                Text(
                    text = stringResource(Res.string.google_btn),
                    fontSize = textFontSize
                )
            }
        }
    }
}

@Composable
fun InfiniteLoadingCircle(
    modifier: Modifier = Modifier,
    size: Dp,
    strokeWidth: Dp = 4.dp,
    color: Color = Gray50,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000
            )
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(size)
                .rotate(rotation),
            strokeWidth = strokeWidth,
            color = color
        )
    }
}

@Composable
fun FullScreenLoader(
    modifier: Modifier = Modifier,
    visible: Boolean,
    bgColor: Color = Gray80,
    indicatorColor: Color = White,
    indicatorSize: Dp = 140.dp,
    indicatorStrokeWidth: Dp = 20.dp,
    text: String = "",
    textColor: Color = Black,
    textFontSize: TextUnit = 30.sp
) {

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = null,
                    indication = null
                ) {}
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InfiniteLoadingCircle(
                    size = indicatorSize,
                    color = indicatorColor,
                    strokeWidth = indicatorStrokeWidth,
                )
                Text(
                    text = text,
                    color = textColor,
                    fontSize = textFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class ShowLoadingAction(
    val loadingText: String = ""
)

@Composable
fun TypingDots(
    delayUnit: Int = 200,
    maxOffset: Float = 10f,
    dotSize: Dp = 12.dp
) {

    @Composable
    fun Dot(
        offset: Float
    ) = Spacer(
        Modifier
            .size(dotSize)
            .offset(y = -offset.dp)
            .background(
                color = LightGreen,
                shape = CircleShape
            )
    )

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateOffsetWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = delayUnit * 4
                0f at delay using LinearEasing
                maxOffset at delay + delayUnit using LinearEasing
                0f at delay + delayUnit * 2
            }
        )
    )

    val offset1 by animateOffsetWithDelay(0)
    val offset2 by animateOffsetWithDelay(delayUnit)
    val offset3 by animateOffsetWithDelay(delayUnit * 2)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = maxOffset.dp)
    ) {
        val spaceSize = 2.dp

        Dot(offset1)
        Spacer(Modifier.width(spaceSize))
        Dot(offset2)
        Spacer(Modifier.width(spaceSize))
        Dot(offset3)
    }
}