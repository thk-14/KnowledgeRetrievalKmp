package com.thk.knowledgeretrievalkmp.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSharedTransitionApi::class)
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
                    // Login screen
                    log("login")
                    Column {
                        Text("Login")
                        Button(onClick = { navController.navigate(KbDestination.Signup) }) {
                            Text("Signup")
                        }
                    }

                }
                composable<KbDestination.Signup> {
                    // Signup screen
                    log("Signup")
                    Column {
                        Text("Signup")
                        Button(onClick = { navController.navigate(KbDestination.KnowledgeBase) }) {
                            Text("KnowledgeBase")
                        }
                    }

                }
            }
            composable<KbDestination.KnowledgeBase> {
                // Kb screen
                log("Kb")
                Column {
                    Text("Kb")
                    Button(onClick = { navController.navigate(KbDestination.Chat("")) }) {
                        Text("Chat")
                    }
                }

            }
            composable<KbDestination.Chat> {
                // Chat screen
                log("Chat")
                Column {
                    Text("Chat")
                    Button(onClick = { navController.navigate(KbDestination.Login) }) {
                        Text("Login")
                    }
                }

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