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

sealed interface RegisterSubmitState {
    data object Idle : RegisterSubmitState
    data object Loading : RegisterSubmitState
    data class Success(val message: String) : RegisterSubmitState
    data class Error(val message: String) : RegisterSubmitState
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val submitState: RegisterSubmitState = RegisterSubmitState.Idle
) {
    val isLoading: Boolean get() = submitState is RegisterSubmitState.Loading
    val isSuccess: Boolean get() = submitState is RegisterSubmitState.Success
    val errorMessage: String? get() = (submitState as? RegisterSubmitState.Error)?.message
    val successMessage: String? get() = (submitState as? RegisterSubmitState.Success)?.message
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = null,
                submitState = if (it.submitState is RegisterSubmitState.Error) RegisterSubmitState.Idle else it.submitState
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                submitState = if (it.submitState is RegisterSubmitState.Error) RegisterSubmitState.Idle else it.submitState
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                submitState = if (it.submitState is RegisterSubmitState.Error) RegisterSubmitState.Idle else it.submitState
            )
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        val nameErr = AuthValidator.validateName(state.name)
        val emailErr = AuthValidator.validateEmail(state.email)
        val passErr = AuthValidator.validatePassword(state.password)

        _uiState.update {
            it.copy(
                nameError = nameErr,
                emailError = emailErr,
                passwordError = passErr
            )
        }

        return nameErr == null && emailErr == null && passErr == null
    }

    fun register() {
        if (!validate()) return

        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(submitState = RegisterSubmitState.Loading) }
            when (val result = authRepository.register(name, email, password)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(submitState = RegisterSubmitState.Success(result.data))
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(submitState = RegisterSubmitState.Error(result.message))
                    }
                }
                is ApiResult.Loading -> {
                    _uiState.update { it.copy(submitState = RegisterSubmitState.Loading) }
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(submitState = RegisterSubmitState.Idle) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(submitState = RegisterSubmitState.Idle) }
    }
}
