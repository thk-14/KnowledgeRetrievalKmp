package com.thk.knowledgeretrievalkmp.ui.view.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import com.thk.knowledgeretrievalkmp.ui.theme.*
import com.thk.knowledgeretrievalkmp.ui.view.custom.LocalWindowSize
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.cancel_btn
import knowledgeretrievalkmp.composeapp.generated.resources.delete_btn
import knowledgeretrievalkmp.composeapp.generated.resources.rename_btn
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val screenWidth = LocalWindowSize.current.width
    val screenHeight = LocalWindowSize.current.height

    Dialog(
        onDismissRequest = onDismiss
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
                    text = title,
                    color = Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = content,
                    color = Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.05 * screenWidth)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .sizeIn(minHeight = 50.dp)
                            .height(0.05 * screenHeight),
                    ) {
                        Text(stringResource(Res.string.cancel_btn))
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .sizeIn(minHeight = 50.dp)
                            .height(0.05 * screenHeight)
                    ) {
                        Text(stringResource(Res.string.delete_btn))
                    }
                }
            }
        }
    }
}

@Composable
fun RenameDialog(
    modifier: Modifier = Modifier,
    title: String,
    textFieldLabel: String,
    textFieldPlaceholder: String,
    renameTextState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val screenWidth = LocalWindowSize.current.width
    val screenHeight = LocalWindowSize.current.height

    Dialog(
        onDismissRequest = onDismiss
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
                    text = title,
                    color = Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    state = renameTextState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text(textFieldLabel) },
                    placeholder = { Text(textFieldPlaceholder) },
                    modifier = Modifier
                        .sizeIn(minHeight = 60.dp)
                        .width(width = 0.6 * screenWidth),
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
                        onClick = onDismiss,
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

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Black,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .sizeIn(minHeight = 60.dp)
                            .height(0.05 * screenHeight)
                    ) {
                        Text(stringResource(Res.string.rename_btn))
                    }
                }
            }
        }
    }
}