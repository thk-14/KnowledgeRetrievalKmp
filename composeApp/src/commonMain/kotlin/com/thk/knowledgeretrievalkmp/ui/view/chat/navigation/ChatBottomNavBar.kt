package com.thk.knowledgeretrievalkmp.ui.view.chat.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.thk.knowledgeretrievalkmp.ui.theme.Black
import com.thk.knowledgeretrievalkmp.ui.theme.Gray50
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.Dimens
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.chat
import knowledgeretrievalkmp.composeapp.generated.resources.source
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

enum class ChatBottomNavItem(
    val label: String,
    val icon: DrawableResource,
    val destination: ChatNavDestination
) {
    SOURCES("Sources", Res.drawable.source, ChatNavDestination.Sources),
    CHAT("Chat", Res.drawable.chat, ChatNavDestination.Chat)
}

@Composable
fun ChatBottomNavBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startItem: ChatBottomNavItem = ChatBottomNavItem.SOURCES
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(startItem.ordinal) }

    NavigationBar(
        modifier = modifier,
        containerColor = White
    ) {
        ChatBottomNavItem.entries.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = (index == selectedItem),
                onClick = {
                    navController.navigate(item.destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    selectedItem = index
                },
                icon = {
                    Icon(
                        imageVector = vectorResource(item.icon),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.top_bar_icon_size)
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Black,
                    indicatorColor = Gray50
                )
            )
        }
    }
}