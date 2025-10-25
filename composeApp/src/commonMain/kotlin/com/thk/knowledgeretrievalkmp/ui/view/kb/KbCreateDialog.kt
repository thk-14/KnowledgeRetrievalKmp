package com.thk.knowledgeretrievalkmp.ui.view.kb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import com.thk.knowledgeretrievalkmp.ui.theme.*
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import knowledgeretrievalkmp.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun KbCreateDialog(
    modifier: Modifier = Modifier,
    kbViewModel: KbViewModel
) {
    val screenWidth = LocalWindowSize.current.width
    val screenHeight = LocalWindowSize.current.height
    val coroutineScope = rememberCoroutineScope()

    val createKbLoadingText = stringResource(Res.string.LS_create_kb)
    val kbCreateSuccess = stringResource(Res.string.kb_create_success)
    val kbCreateFailed = stringResource(Res.string.kb_create_failed)

    Dialog(
        onDismissRequest = {
            kbViewModel.dismissKbCreateDialog()
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = White
            )
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(0.01 * screenHeight),
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = stringResource(Res.string.knowledge_base_create_dialog_title),
                    color = Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    state = kbViewModel.kbUiState.createKBNameState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text(stringResource(Res.string.knowledge_base_create_dialog_name)) },
                    placeholder = { Text(stringResource(Res.string.knowledge_base_create_dialog_name_placeholder)) },
                    modifier = Modifier
                        .sizeIn(minHeight = 60.dp)
                        .size(
                            width = 0.6 * screenWidth,
                            height = 0.05 * screenHeight
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = DeepBlue,
                        unfocusedIndicatorColor = LightBlue
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    state = kbViewModel.kbUiState.createKBDescriptionState,
                    lineLimits = TextFieldLineLimits.MultiLine(),
                    label = { Text(stringResource(Res.string.knowledge_base_create_dialog_description)) },
                    placeholder = { Text(stringResource(Res.string.knowledge_base_create_dialog_description_placeholder)) },
                    modifier = Modifier
                        .sizeIn(minHeight = 60.dp)
                        .size(
                            width = 0.6 * screenWidth,
                            height = 0.3 * screenHeight
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = DeepBlue,
                        unfocusedIndicatorColor = LightBlue
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.05 * screenWidth)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            kbViewModel.dismissKbCreateDialog()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .sizeIn(minHeight = 60.dp)
                            .height(0.05 * screenHeight),
                    ) {
                        Text(stringResource(Res.string.cancel_btn))
                    }
                    val nameEmptyWarning =
                        stringResource(Res.string.kb_name_empty_warning)
                    val descriptionEmptyWarning =
                        stringResource(Res.string.kb_description_empty_warning)
                    Button(
                        onClick = onCreateButtonClick@{
                            val name =
                                kbViewModel.kbUiState.createKBNameState.text.toString()
                            val description =
                                kbViewModel.kbUiState.createKBDescriptionState.text.toString()
                            if (name.isEmpty()) {
                                coroutineScope.launch {
                                    kbViewModel.kbUiState.snackBarHostState.showSnackbar(
                                        message = nameEmptyWarning,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@onCreateButtonClick
                            }
                            if (description.isEmpty()) {
                                coroutineScope.launch {
                                    kbViewModel.kbUiState.snackBarHostState.showSnackbar(
                                        message = descriptionEmptyWarning,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@onCreateButtonClick
                            }
                            kbViewModel.dismissKbCreateDialog()
                            kbViewModel.kbUiState.showLoadingAction.value =
                                ShowLoadingAction(createKbLoadingText)
                            kbViewModel.createKnowledgeBase(
                                name = name,
                                description = description,
                                onCreateKbFinish = { succeed ->
                                    kbViewModel.kbUiState.showLoadingAction.value = null
                                    if (succeed) {
                                        coroutineScope.launch {
                                            kbViewModel.kbUiState.snackBarHostState.showSnackbar(
                                                message = kbCreateSuccess,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            kbViewModel.kbUiState.snackBarHostState.showSnackbar(
                                                message = kbCreateFailed,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Black,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .sizeIn(minHeight = 60.dp)
                            .height(0.05 * screenHeight)
                    ) {
                        Text(stringResource(Res.string.create_btn))
                    }
                }
            }
        }
    }
}