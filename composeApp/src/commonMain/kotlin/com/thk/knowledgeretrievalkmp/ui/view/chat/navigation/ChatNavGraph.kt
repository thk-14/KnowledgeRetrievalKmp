package com.thk.knowledgeretrievalkmp.ui.view.chat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thk.knowledgeretrievalkmp.ui.view.chat.ChatViewModel
import kotlinx.serialization.Serializable

sealed class ChatNavDestination {
    @Serializable
    object Sources : ChatNavDestination()

    @Serializable
    object Chat : ChatNavDestination()
}

@Composable
fun ChatNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    chatViewModel: ChatViewModel
) {
    NavHost(
        navController = navController,
        startDestination = ChatNavDestination.Sources,
        modifier = modifier
    ) {
        composable<ChatNavDestination.Sources> {
            NavSourceScreen(
                chatViewModel = chatViewModel
            )
        }
        composable<ChatNavDestination.Chat> {
            NavChatScreen(
                chatViewModel = chatViewModel
            )
        }
    }
}