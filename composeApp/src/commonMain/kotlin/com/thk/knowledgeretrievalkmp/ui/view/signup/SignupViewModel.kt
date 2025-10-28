package com.thk.knowledgeretrievalkmp.ui.view.signup

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
import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.data.KnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.ui.view.custom.ShowLoadingAction
import com.thk.knowledgeretrievalkmp.util.log
import kotlinx.coroutines.launch

data class SignupUiState(
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val emailInputState: TextFieldState = TextFieldState(),
    val passwordInputState: TextFieldState = TextFieldState(),
    val signupCompleted: MutableState<Boolean> = mutableStateOf(false),
    val isLoggedIn: MutableState<Boolean> = mutableStateOf(false),
    val showLoadingAction: MutableState<ShowLoadingAction?> = mutableStateOf(null)
)

class SignupViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val signupUiState = SignupUiState()

    private val repository: KnowledgeRetrievalRepository = DefaultKnowledgeRetrievalRepository

    fun loginWithGoogle(
        userId: String,
        displayName: String,
        profileUri: String,
        idToken: String,
        onLoginFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val succeed = repository.loginWithGoogle(
            userId = userId,
            displayName = displayName,
            profileUri = profileUri,
            idToken = idToken
        )
        log("loginWithGoogle succeed: $succeed")
        onLoginFinish(succeed)
    }

    fun signupUser(
        onSignupFinish: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val succeed = repository.registerUser(
            email = signupUiState.emailInputState.text.toString(),
            password = signupUiState.passwordInputState.text.toString(),
            userName = signupUiState.emailInputState.text.toString(),
            fullName = signupUiState.emailInputState.text.toString()
        )
        log("signupUser succeed: $succeed")
        onSignupFinish(succeed)
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            signupUiState.snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                SignupViewModel(savedStateHandle)
            }
        }
    }
}