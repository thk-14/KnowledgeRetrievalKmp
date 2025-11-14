package com.thk.knowledgeretrievalkmp.ui.view.kb

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import knowledgeretrievalkmp.composeapp.generated.resources.*
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
                    kbViewModel.kbUiState.showLoadingAction.value =
                        ShowLoadingAction(logoutLoadingText)
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
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.01 * screenHeight),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(
                items = kbViewModel.kbUiState.knowledgeBases,
                key = { it.KbId }
            ) { knowledgeBase ->
                KbItem(
                    knowledgeBase = knowledgeBase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 0.1 * screenHeight)
                        .wrapContentHeight()
                        .padding(horizontal = Dimens.padding_horizontal)
                        .animateItem(),
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
            modifier = Modifier.clickable {
                onFetchData()
            }
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.book_open),
                contentDescription = null,
                modifier = Modifier
                    .size(Dimens.top_bar_icon_size)
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
                        .size(Dimens.top_bar_icon_size)
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
                    onClick = onLogout
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
                        .align(Alignment.Start)
                        .padding(10.dp)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(10.dp)
                    .fillMaxWidth()
                    .sizeIn(maxHeight = 0.3 * screenHeight)
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
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