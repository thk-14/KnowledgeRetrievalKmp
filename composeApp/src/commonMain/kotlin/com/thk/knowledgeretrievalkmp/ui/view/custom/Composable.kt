package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mmk.kmpauth.google.GoogleUser
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.theme.White80
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.google
import knowledgeretrievalkmp.composeapp.generated.resources.google_btn
import kotlinx.coroutines.launch
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
            onClick = {

//                onGoogleSignInResult(null)

                this.onClick()
            },
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
fun FullScreenLoader(
    modifier: Modifier = Modifier,
    visible: Boolean,
    bgColor: Color = White80,
    indicatorColor: Color = White,
    indicatorSize: Dp = 140.dp,
    indicatorStrokeWidth: Dp = 20.dp,
    loadingText: String?,
    loadingAnimation: LottieAnimation?,
    textColor: Color = Black,
    textFontSize: TextUnit = 20.sp
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
//                InfiniteLoadingCircle(
//                    size = indicatorSize,
//                    color = indicatorColor,
//                    strokeWidth = indicatorStrokeWidth,
//                )
                if (loadingAnimation != null) {
                    LottieAnimation(
                        lottieFilePath = loadingAnimation.lottieFilePath,
                        modifier = Modifier.height(200.dp).width(300.dp)
                    )
                }
                if (loadingText != null) {
                    Text(
                        text = loadingText,
                        color = textColor,
                        fontSize = textFontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class ShowLoadingAction(
    val loadingText: String,
    val loadingAnimation: LottieAnimation
)

@Composable
fun CustomResizeNavigationDrawer(
    drawerState: DrawerState,
    drawerWidth: Dp,
    gestureEnabled: Boolean = false,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedWidth by animateDpAsState(
        targetValue = if (drawerState.isOpen) drawerWidth else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (drawerState.isOpen) 0.32f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .width(animatedWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .clip(RectangleShape)
        ) {
            Box(
                modifier = Modifier.requiredWidth(drawerWidth)
            ) {
                drawerContent()
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
            if (gestureEnabled) {
                if (drawerState.isOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Black.copy(alpha = scrimAlpha))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                    )
                }
            }
        }
    }
}