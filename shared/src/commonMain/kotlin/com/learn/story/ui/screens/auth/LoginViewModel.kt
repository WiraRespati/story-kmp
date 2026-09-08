package com.learn.story.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.network.ApiResult
import com.learn.story.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

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
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        val state = _uiState.value
        val email = state.email.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(emailError = "Email tidak boleh kosong") }
            isValid = false
        } else if (!isValidEmail(email)) {
            _uiState.update { it.copy(emailError = "Format email tidak valid") }
            isValid = false
        }

        if (state.password.trim().isEmpty()) {
            _uiState.update { it.copy(passwordError = "Password tidak boleh kosong") }
            isValid = false
        } else if (state.password.length < 8) {
            _uiState.update { it.copy(passwordError = "Password minimal 8 karakter") }
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val atIndex = email.indexOf('@')
        val dotIndex = email.lastIndexOf('.')
        return atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < email.length - 1
    }

    fun login(onSuccess: () -> Unit) {
        if (!validate()) return

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(email, password)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is ApiResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
