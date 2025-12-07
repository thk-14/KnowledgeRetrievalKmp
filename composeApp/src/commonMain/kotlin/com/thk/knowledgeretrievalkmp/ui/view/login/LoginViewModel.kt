package com.thk.knowledgeretrievalkmp.ui.view.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thk.knowledgeretrievalkmp.data.AppContainer
import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.KnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.local.db.createDatabase
import com.thk.knowledgeretrievalkmp.data.local.db.createDriver
import com.thk.knowledgeretrievalkmp.ui.view.custom.LottieAnimation
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.launch

data class LoginUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val emailInputState: TextFieldState = TextFieldState(),
    val passwordInputState: TextFieldState = TextFieldState(),
    val isLoggedIn: MutableState<Boolean> = mutableStateOf(false),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null)
)

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val exchangeCode = savedStateHandle.get<String>("exchangeCode")
    val loginUiState = LoginUiState()
    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    init {
        viewModelScope.launch {
            AppContainer.db = createDatabase(createDriver())
            log("database created")
//            refreshToken()

            log("exchangeCode: $exchangeCode")
            repository.exchangeGoogleAuthCode(exchangeCode)
            log("exchangeGoogleAuthCode finish")

            val userId = repository.getUserId()
            if (userId != null) {
                loginUiState.isLoggedIn.value = true
            }
        }
    }

    fun loginWithGoogle(
        userId: String = "",
        displayName: String = "",
        profileUri: String = "",
        idToken: String = "",
        onLoginFinish: (Boolean) -> Unit
    ) {
//        val succeed = repository.loginGoogleWithServer(onLoginFinish)

        viewModelScope.launch {
            val succeed = repository.loginWithGoogle(
                userId = userId,
                displayName = displayName,
                profileUri = profileUri,
                idToken = idToken
            )
            log("loginWithGoogle succeed: $succeed")
            onLoginFinish(succeed)
        }
    }

    fun authenticateUser(
        onLoginFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val succeed = repository.loginUser(
            email = loginUiState.emailInputState.text.toString(),
            password = loginUiState.passwordInputState.text.toString()
        )
        log("authenticateUser succeed: $succeed")
        onLoginFinish(succeed)
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            loginUiState.snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    private suspend fun refreshToken() {
        loginUiState.showLoadingAction.value = ShowLoadingAction(
            loadingText = "Checking",
            loadingAnimation = LottieAnimation.LOADING
        )
        val succeed = repository.refreshToken()
        loginUiState.showLoadingAction.value = null
        if (succeed) {
            loginUiState.isLoggedIn.value = true
        }
        log("Refresh token succeed: $succeed")
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                LoginViewModel(savedStateHandle)
            }
        }
    }
}