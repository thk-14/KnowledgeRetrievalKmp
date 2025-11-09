package com.thk.knowledgeretrievalkmp.ui

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.thk.knowledgeretrievalkmp.ui.view.chat.ChatScreen
import com.thk.knowledgeretrievalkmp.ui.view.detail.DetailScreen
import com.thk.knowledgeretrievalkmp.ui.view.kb.KbScreen
import com.thk.knowledgeretrievalkmp.ui.view.login.LoginScreen
import com.thk.knowledgeretrievalkmp.ui.view.signup.SignupScreen
import kotlinx.serialization.Serializable

@Composable
fun KnowledgeRetrievalNavGraph(
    navController: NavHostController = rememberNavController()
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = KbDestination.Authentication
        ) {
            navigation<KbDestination.Authentication>(
                startDestination = KbDestination.Login,
            ) {
                composable<KbDestination.Login> {
                    LoginScreen(
                        onNavigateToSignup = {
                            navController.navigate(
                                route = KbDestination.Signup,
                                builder = {
                                    popUpTo(KbDestination.Signup) {
                                        inclusive = true
                                    }
                                }
                            )
                        },
                        onNavigateToKnowledgeBase = {
                            navController.navigate(
                                route = KbDestination.KnowledgeBase,
                                builder = {
                                    popUpTo(KbDestination.Authentication) {
                                        inclusive = true
                                    }
                                }
                            )
                        }
                    )
                }
                composable<KbDestination.Signup> {
                    SignupScreen(
                        onBackPressed = {
                            navController.popBackStack()
                        },
                        onNavigateToLogin = {
                            navController.navigate(
                                route = KbDestination.Login,
                                builder = {
                                    popUpTo(KbDestination.Login) {
                                        inclusive = true
                                    }
                                }
                            )
                        },
                        onNavigateToKnowledgeBase = {
                            navController.navigate(
                                route = KbDestination.KnowledgeBase,
                                builder = {
                                    popUpTo(KbDestination.Authentication) {
                                        inclusive = true
                                    }
                                }
                            )
                        }
                    )
                }
            }
            composable<KbDestination.KnowledgeBase> {
                KbScreen(
                    onNavigateToDetail = { kbId ->
                        navController.navigate(
                            route = KbDestination.Detail(kbId),
                            builder = {
                                launchSingleTop = true
                            }
                        )
                    },
                    onNavigateToAuthentication = {
                        navController.navigate(
                            route = KbDestination.Authentication,
                            builder = {
                                popUpTo(KbDestination.Authentication) {
                                    inclusive = true
                                }
                            }
                        )
                    },
                    onNavigateToChat = { kbId ->
                        navController.navigate(
                            route = KbDestination.Chat(kbId),
                            builder = {
                                launchSingleTop = true
                            }
                        )
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
            composable<KbDestination.Detail> {
                DetailScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onNavigateToChat = { kbId ->
                        navController.navigate(
                            route = KbDestination.Chat(kbId),
                            builder = {
                                launchSingleTop = true
                            }
                        )
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
            composable<KbDestination.Chat> {
                ChatScreen(
                    onNavigateToKnowledgeBase = {
                        navController.navigate(
                            route = KbDestination.KnowledgeBase,
                            builder = {
                                popUpTo(KbDestination.KnowledgeBase) {
                                    inclusive = true
//                                    saveState = true
                                }
                                launchSingleTop = true
//                                restoreState = true
                            }
                        )
                    }
                )
            }
        }
    }
}

@Serializable
sealed class KbDestination {
    @Serializable
    object Authentication : KbDestination()

    @Serializable
    object Login : KbDestination()

    @Serializable
    object Signup : KbDestination()

    @Serializable
    object KnowledgeBase : KbDestination()

    @Serializable
    data class Detail(
        val knowledgeBaseId: String
    ) : KbDestination()

    @Serializable
    data class Chat(
        val initialKnowledgeBaseId: String
    ) : KbDestination()
}