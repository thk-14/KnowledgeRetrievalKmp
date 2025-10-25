package com.thk.knowledgeretrievalkmp.ui.view.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thk.knowledgeretrievalkmp.ui.theme.Blue
import com.thk.knowledgeretrievalkmp.ui.theme.DeepBlue
import com.thk.knowledgeretrievalkmp.ui.theme.LightBlue
import com.thk.knowledgeretrievalkmp.ui.theme.White
import com.thk.knowledgeretrievalkmp.ui.view.custom.*
import com.thk.knowledgeretrievalkmp.util.checkValidPasswordError
import com.thk.knowledgeretrievalkmp.util.isValidEmail
import com.thk.knowledgeretrievalkmp.util.log
import knowledgeretrievalkmp.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    signupViewModel: SignupViewModel = viewModel(factory = SignupViewModel.Factory),
    onBackPressed: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit
) {
    LaunchedEffect(
        signupViewModel.signupUiState.isLoggedIn.value,
        signupViewModel.signupUiState.signupCompleted.value
    ) {
        when {
            signupViewModel.signupUiState.isLoggedIn.value -> onNavigateToKnowledgeBase()
            signupViewModel.signupUiState.signupCompleted.value -> onNavigateToLogin()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = signupViewModel.signupUiState.snackBarHostState)
        }
    ) {
        SignupMainScreen(
            modifier = modifier
                .background(White)
                .fillMaxSize()
                .padding(
                    horizontal = Dimens.padding_horizontal
                ),
            signupViewModel = signupViewModel,
            onBackPressed = onBackPressed
        )
    }

    FullScreenLoader(
        visible = signupViewModel.signupUiState.showLoadingAction.value != null,
        text = signupViewModel.signupUiState.showLoadingAction.value?.loadingText ?: ""
    )
}

@Composable
fun SignupMainScreen(
    modifier: Modifier = Modifier,
    signupViewModel: SignupViewModel,
    onBackPressed: () -> Unit
) {
    val screenWidth = LocalWindowSize.current.width
    val screenHeight = LocalWindowSize.current.height
    val coroutineScope = rememberCoroutineScope()

    val loginFailedWarning = stringResource(Res.string.login_failed_warning)
    val emailInvalidWarning = stringResource(Res.string.email_invalid_warning)
    val signupFailedWarning = stringResource(Res.string.signup_failed_warning)
    val loginLoadingText = stringResource(Res.string.LS_login)
    val signupLoadingText = stringResource(Res.string.LS_signup)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.05 * screenHeight),
        modifier = modifier
    ) {
        // Back btn
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp)
                .height(0.05 * screenHeight)
                .padding(start = Dimens.padding_horizontal)
        ) {
            IconButton(
                onClick = onBackPressed
            ) {
                Image(
                    imageVector = vectorResource(Res.drawable.back),
                    contentDescription = null,
                )
            }
        }
        // Title
        Text(
            text = stringResource(Res.string.signup_title),
            color = DeepBlue,
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold
        )
        // Social btn
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                0.1 * screenWidth
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleButton(
                onGoogleSignInResult = SignInResult@{ googleUser ->
                    log("googleUser: $googleUser")
                    if (googleUser == null) {
                        return@SignInResult
                    }
                    signupViewModel.signupUiState.showLoadingAction.value = ShowLoadingAction(loginLoadingText)
                    signupViewModel.loginWithGoogle(
                        userId = googleUser.email ?: "",
                        displayName = googleUser.displayName,
                        profileUri = googleUser.profilePicUrl ?: "",
                        idToken = googleUser.idToken,
                        onLoginFinish = { succeed ->
                            if (succeed) {
                                signupViewModel.signupUiState.isLoggedIn.value = true
                            } else {
                                coroutineScope.launch {
                                    signupViewModel.signupUiState.snackBarHostState.showSnackbar(
                                        message = loginFailedWarning,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                    signupViewModel.signupUiState.showLoadingAction.value = null
                },
                iconSize = 20.dp
            )
        }
        // Or
        Text(stringResource(Res.string.login_or))
        // Login info
        Column {
            TextField(
                state = signupViewModel.signupUiState.emailInputState,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text(stringResource(Res.string.email)) },
                placeholder = { Text(stringResource(Res.string.email_placeholder)) },
                modifier = Modifier
                    .sizeIn(minHeight = 50.dp)
                    .size(
                        width = 0.8 * screenWidth,
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
            SecureTextField(
                state = signupViewModel.signupUiState.passwordInputState,
                label = { Text(stringResource(Res.string.password)) },
                modifier = Modifier
                    .sizeIn(minHeight = 50.dp)
                    .size(
                        width = 0.8 * screenWidth,
                        height = 0.05 * screenHeight
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DeepBlue,
                    unfocusedIndicatorColor = LightBlue
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        // Signup btn
        Button(
            onClick = onSignupClick@{
                val email = signupViewModel.signupUiState.emailInputState.text.toString().trim()
                val password =
                    signupViewModel.signupUiState.passwordInputState.text.toString().trim()
                if (!email.isValidEmail()) {
                    coroutineScope.launch {
                        signupViewModel.signupUiState.snackBarHostState.showSnackbar(
                            message = emailInvalidWarning,
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@onSignupClick
                }
                val passwordError = checkValidPasswordError(password)
                if (passwordError != null) {
                    coroutineScope.launch {
                        signupViewModel.signupUiState.snackBarHostState.showSnackbar(
                            message = passwordError,
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@onSignupClick
                }
                signupViewModel.signupUiState.showLoadingAction.value = ShowLoadingAction(signupLoadingText)
                signupViewModel.signupUser(
                    onSignupFinish = { succeed ->
                        signupViewModel.signupUiState.showLoadingAction.value = null
                        if (succeed) {
                            signupViewModel.signupUiState.signupCompleted.value = true
                        } else {
                            coroutineScope.launch {
                                signupViewModel.signupUiState.snackBarHostState.showSnackbar(
                                    message = signupFailedWarning,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue,
                contentColor = White
            ),
            modifier = Modifier
                .sizeIn(minHeight = 40.dp)
                .size(
                    width = 0.8 * screenWidth,
                    height = 0.05 * screenHeight
                )
        ) {
            Text(
                text = stringResource(Res.string.signup_btn),
                fontSize = 20.sp
            )
        }
    }
}