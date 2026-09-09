package com.learn.story.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.network.ApiResult
import com.learn.story.data.repository.AuthRepository
import com.learn.story.util.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthSubmitState {
    data object Idle : AuthSubmitState
    data object Loading : AuthSubmitState
    data object Success : AuthSubmitState
    data class Error(val message: String) : AuthSubmitState
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val submitState: AuthSubmitState = AuthSubmitState.Idle
) {
    val isLoading: Boolean get() = submitState is AuthSubmitState.Loading
    val isSuccess: Boolean get() = submitState is AuthSubmitState.Success
    val errorMessage: String? get() = (submitState as? AuthSubmitState.Error)?.message
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                submitState = if (it.submitState is AuthSubmitState.Error) AuthSubmitState.Idle else it.submitState
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                submitState = if (it.submitState is AuthSubmitState.Error) AuthSubmitState.Idle else it.submitState
            )
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        val emailErr = AuthValidator.validateEmail(state.email)
        val passErr = AuthValidator.validatePassword(state.password)

        _uiState.update {
            it.copy(
                emailError = emailErr,
                passwordError = passErr
            )
        }

        return emailErr == null && passErr == null
    }

    fun login() {
        if (!validate()) return

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(submitState = AuthSubmitState.Loading) }
            when (val result = authRepository.login(email, password)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(submitState = AuthSubmitState.Success) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(submitState = AuthSubmitState.Error(result.message))
                    }
                }
                is ApiResult.Loading -> {
                    _uiState.update { it.copy(submitState = AuthSubmitState.Loading) }
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(submitState = AuthSubmitState.Idle) }
    }

    fun clearError() {
        _uiState.update { it.copy(submitState = AuthSubmitState.Idle) }
    }
}
