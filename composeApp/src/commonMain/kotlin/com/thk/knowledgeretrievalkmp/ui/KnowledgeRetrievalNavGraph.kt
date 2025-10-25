package com.thk.knowledgeretrievalkmp.ui

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.thk.knowledgeretrievalkmp.ui.view.chat.ChatScreen
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
                            navController.navigate(KbDestination.Signup)
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
                            navController.navigate(KbDestination.Login)
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
                    onNavigateToChat = { kbId ->
                        navController.navigate(KbDestination.Chat(kbId))
                    },
                    onNavigateToAuthentication = {
                        navController.navigate(KbDestination.Authentication)
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
            composable<KbDestination.Chat> {
                ChatScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
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
    data class Chat(
        val knowledgeBaseId: String
    ) : KbDestination()
}