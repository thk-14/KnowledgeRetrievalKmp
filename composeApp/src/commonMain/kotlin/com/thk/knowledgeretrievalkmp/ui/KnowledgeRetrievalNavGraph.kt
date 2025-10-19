package com.thk.knowledgeretrievalkmp.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.thk.knowledgeretrievalkmp.data.network.NetworkApiService
import com.thk.knowledgeretrievalkmp.util.log
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
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
                    val coroutineScope = rememberCoroutineScope()
                    val textFieldState = rememberTextFieldState()
                    Column {
                        Text("Login")
                        BasicTextField(
                            state = textFieldState,
                        )
                        Button(onClick = {
                            coroutineScope.launch {

                            }
                        }) {
                            Text("test")
                        }
                    }

                }
                composable<KbDestination.Signup> {
                    // Signup screen
                }
            }
            composable<KbDestination.KnowledgeBase> {
                // Kb screen
            }
            composable<KbDestination.Chat> {
                // Chat screen
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

// FOR TESTING
suspend fun selectFile() {
    val stateFlow = FileKit.openFilePicker(
        mode = FileKitMode.SingleWithState,
        type = FileKitType.File(listOf("pdf", "docx", "txt"))
    )
    stateFlow.collect { state ->
        when (state) {
            is FileKitPickerState.Started -> log("Selection started with ${state.total} files")
            is FileKitPickerState.Progress -> log("Processing: ${state.processed.size()} / ${state.total}")
            is FileKitPickerState.Completed -> {
                log("Completed: ${state.result.size()} files selected")
                val file = state.result
                log("File selected: $file")
                NetworkApiService.uploadDocument(
                    knowledgeBaseId = "068f3aa1-de61-72c6-8000-d36e326c329c",
                    fileName = file.name,
                    mimeType = file.mimeType().toString(),
                    file = file.readBytes()
                )
            }

            is FileKitPickerState.Cancelled -> log("Selection cancelled")
        }
    }
}