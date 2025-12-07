package com.thk.knowledgeretrievalkmp.ui.view.kb

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil3.compose.AsyncImage
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.ColumnWithScrollbar
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import knowledgeretrievalkmp.composeapp.generated.resources.LS_logout
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.add
import knowledgeretrievalkmp.composeapp.generated.resources.book_open
import knowledgeretrievalkmp.composeapp.generated.resources.chat
import knowledgeretrievalkmp.composeapp.generated.resources.default_avatar
import knowledgeretrievalkmp.composeapp.generated.resources.expandable
import knowledgeretrievalkmp.composeapp.generated.resources.knowledge_base_title
import knowledgeretrievalkmp.composeapp.generated.resources.logout
import knowledgeretrievalkmp.composeapp.generated.resources.logout_btn
import knowledgeretrievalkmp.composeapp.generated.resources.logout_failed_warning
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun KbMainScreen(
    modifier: Modifier = Modifier,
    kbViewModel: KbViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToAuthentication: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val screenHeight = LocalWindowSize.current.height

    val logoutLoadingText = stringResource(Res.string.LS_logout)
    val logoutFailedWarning = stringResource(Res.string.logout_failed_warning)
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KbTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.top_bar_height),
                profileUrl = kbViewModel.profileUri.value,
                menuExpanded = kbViewModel.kbUiState.menuExpanded.value,
                onMenuDismissRequest = {
                    kbViewModel.kbUiState.menuExpanded.value = false
                },
                onProfileClicked = {
                    kbViewModel.kbUiState.apply {
                        menuExpanded.value = !menuExpanded.value
                    }
                },
                onLogout = {
                    kbViewModel.kbUiState.menuExpanded.value = false
                    kbViewModel.kbUiState.showLoadingAction.value = ShowLoadingAction(
                        loadingText = logoutLoadingText,
                        loadingAnimation = LottieAnimation.LOADING
                    )
                    kbViewModel.logout(
                        onLogoutFinish = { succeed ->
                            kbViewModel.kbUiState.showLoadingAction.value = null
                            if (succeed) {
                                onNavigateToAuthentication()
                            } else {
                                kbViewModel.showSnackbar(logoutFailedWarning)
                            }
                        }
                    )
                },
                onFetchData = {
                    kbViewModel.fetchData()
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = fabExpanded) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = !fabExpanded
                                kbViewModel.showKbCreateDialog()
                            },
                            containerColor = White,
                            contentColor = Black
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.add),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(Dimens.top_bar_icon_size)
                            )
                        }

                        SmallFloatingActionButton(
                            onClick = navigateToChat@{
                                fabExpanded = !fabExpanded
                                if (kbViewModel.kbUiState.knowledgeBases.isEmpty()) {
                                    kbViewModel.showSnackbar("Knowledge Bases is empty!")
                                    return@navigateToChat
                                }
                                onNavigateToChat(
                                    kbViewModel.kbUiState.knowledgeBases.first().KbId
                                )
                            },
                            containerColor = White,
                            contentColor = Black
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.chat),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(Dimens.top_bar_icon_size)
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = White,
                    contentColor = Black
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.expandable),
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimens.fab_icon_size)
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        ColumnWithScrollbar(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.01 * screenHeight),
            boxModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            kbViewModel.kbUiState.knowledgeBases.forEach { knowledgeBase ->
                KbItem(
                    knowledgeBase = knowledgeBase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 0.1 * screenHeight)
                        .wrapContentHeight()
                        .padding(horizontal = Dimens.padding_horizontal),
                    onKnowledgeBaseClicked = {
                        onNavigateToDetail(knowledgeBase.KbId)
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
        }
    }
}

@Composable
fun KbTopBar(
    modifier: Modifier = Modifier,
    profileUrl: String,
    menuExpanded: Boolean,
    onMenuDismissRequest: () -> Unit,
    onProfileClicked: () -> Unit,
    onLogout: () -> Unit,
    onFetchData: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    onFetchData()
                }
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.book_open),
                contentDescription = null,
                modifier = Modifier
                    .size(Dimens.logo_icon_size)
            )
            // Title
            Text(
                text = stringResource(Res.string.knowledge_base_title),
                color = Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.offset(x = (-10).dp)) {
            if (profileUrl.isNotEmpty()) {
                AsyncImage(
                    model = profileUrl,
                    contentDescription = null,
                    placeholder = painterResource(Res.drawable.default_avatar),
                    error = painterResource(Res.drawable.default_avatar),
                    modifier = Modifier
                        .size(Dimens.logo_icon_size)
                        .clip(CircleShape)
                        .clickable {
                            onProfileClicked()
                        },
                    onLoading = {
                        log("AsyncImage onLoading $profileUrl")
                    },
                    onError = { e ->
                        log("AsyncImage onError $e")
                    },
                    onSuccess = { imageData ->
                        log("AsyncImage onSuccess")
                    }
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.default_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(Dimens.top_bar_icon_size)
                        .clip(CircleShape)
                        .clickable {
                            onProfileClicked()
                        },
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onMenuDismissRequest
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.logout_btn))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = vectorResource(Res.drawable.logout),
                            contentDescription = null,
                        )
                    },
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

@Composable
fun KbItem(
    knowledgeBase: KnowledgeBase,
    modifier: Modifier = Modifier,
    onKnowledgeBaseClicked: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        border = BorderStroke(1.dp, Black),
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {
        val screenHeight = LocalWindowSize.current.height

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(
                    onClick = onKnowledgeBaseClicked
                )
        ) {
            with(sharedTransitionScope) {
                Text(
                    text = knowledgeBase.Name,
                    color = Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = knowledgeBase.KbId),
                            animatedVisibilityScope = animatedContentScope
                        )
                        .padding(10.dp)
                )
            }
            ColumnWithScrollbar(
                boxModifier = Modifier
                    .sizeIn(maxHeight = 0.3 * screenHeight)
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(10.dp),
                columnModifier = Modifier
                    .sizeIn(maxHeight = 0.3 * screenHeight)
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Text(
                    text = knowledgeBase.Description,
                    color = Black,
                    fontSize = 15.sp,
                )
            }
        }
    }
}