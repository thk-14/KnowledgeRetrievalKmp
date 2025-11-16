package com.thk.knowledgeretrievalkmp.ui.view.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.thk.knowledgeretrievalkmp.util.isValidEmail
import com.thk.knowledgeretrievalkmp.util.log
import knowledgeretrievalkmp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    onNavigateToSignup: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit,
) {
    LaunchedEffect(loginViewModel.loginUiState.isLoggedIn.value) {
        if (loginViewModel.loginUiState.isLoggedIn.value) {
            onNavigateToKnowledgeBase()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = loginViewModel.loginUiState.snackBarHostState)
        }
    ) {
        LoginMainScreen(
            modifier = modifier
                .background(White)
                .fillMaxSize()
                .padding(
                    horizontal = Dimens.padding_horizontal
                ),
            loginViewModel = loginViewModel,
            onNavigateToSignup = onNavigateToSignup
        )
    }

    FullScreenLoader(
        visible = loginViewModel.loginUiState.showLoadingAction.value != null,
        loadingText = loginViewModel.loginUiState.showLoadingAction.value?.loadingText,
        loadingAnimation = loginViewModel.loginUiState.showLoadingAction.value?.loadingAnimation
    )
}

@Composable
fun LoginMainScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel,
    onNavigateToSignup: () -> Unit
) {
    val screenWidth = LocalWindowSize.current.width
    val screenHeight = LocalWindowSize.current.height
    val coroutineScope = rememberCoroutineScope()

    val emailInvalidWarning = stringResource(Res.string.email_invalid_warning)
    val passwordEmptyWarning = stringResource(Res.string.password_empty_warning)
    val loginFailedWarning = stringResource(Res.string.login_failed_warning)
    val logInLoadingText = stringResource(Res.string.LS_login)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.05 * screenHeight),
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier.size(0.05 * screenHeight)
        )
        // Title
        Text(
            text = stringResource(Res.string.login_title),
            color = DeepBlue,
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold
        )
        // Social btn
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleButton(
                onGoogleSignInResult = SignInResult@{ googleUser ->
                    log("googleUser: $googleUser")
                    if (googleUser == null) {
                        return@SignInResult
                    }
                    loginViewModel.loginUiState.showLoadingAction.value = ShowLoadingAction(
                        loadingText = logInLoadingText,
                        loadingAnimation = LoadingAnimation.LOADING
                    )
                    loginViewModel.loginWithGoogle(
                        userId = googleUser.email ?: "",
                        displayName = googleUser.displayName,
                        profileUri = googleUser.profilePicUrl ?: "",
                        idToken = googleUser.idToken,
                        onLoginFinish = { succeed ->
                            loginViewModel.loginUiState.showLoadingAction.value = null
                            if (succeed) {
                                loginViewModel.loginUiState.isLoggedIn.value = true
                            } else {
                                loginViewModel.showSnackbar(loginFailedWarning)
                            }
                        }
                    )
                },
                iconSize = 20.dp
            )
        }
        // Or
        Text(stringResource(Res.string.login_or))
        // Login info
        Column {
            TextField(
                state = loginViewModel.loginUiState.emailInputState,
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
                state = loginViewModel.loginUiState.passwordInputState,
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
        // Login btn
        Column(
            verticalArrangement = Arrangement.spacedBy(0.01 * screenHeight)
        ) {
            Button(
                onClick = OnLoginClick@{
                    val email = loginViewModel.loginUiState.emailInputState.text.toString().trim()
                    val password =
                        loginViewModel.loginUiState.passwordInputState.text.toString().trim()
                    if (!email.isValidEmail()) {
                        loginViewModel.showSnackbar(emailInvalidWarning)
                        return@OnLoginClick
                    }
                    if (password.isEmpty()) {
                        loginViewModel.showSnackbar(passwordEmptyWarning)
                        return@OnLoginClick
                    }

                    loginViewModel.loginUiState.showLoadingAction.value = ShowLoadingAction(
                        loadingText = logInLoadingText,
                        loadingAnimation = LoadingAnimation.LOADING
                    )
                    loginViewModel.authenticateUser(
                        onLoginFinish = { succeed ->
                            loginViewModel.loginUiState.showLoadingAction.value = null
                            if (succeed) {
                                loginViewModel.loginUiState.isLoggedIn.value = true
                            } else {
                                loginViewModel.showSnackbar(loginFailedWarning)
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
                    text = stringResource(Res.string.login_btn),
                    fontSize = 20.sp
                )
            }
            // Don't have an account? Sign up
            Row(
                modifier = Modifier.offset(x = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(stringResource(Res.string.signup_prompt))
                Text(
                    text = stringResource(Res.string.signup_btn),
                    color = LightBlue,
                    modifier = Modifier.clickable {
                        onNavigateToSignup()
                    }
                )
            }
        }
    }
}