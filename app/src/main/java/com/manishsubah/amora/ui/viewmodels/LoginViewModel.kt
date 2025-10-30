package com.manishsubah.amora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manishsubah.amora.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val validation = validate(email, password)
                if (!validation.isValid) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = validation.error)
                    return@launch
                }

                // Simulate API call
                kotlinx.coroutines.delay(1200)
                val success = email.contains("@") && password.length >= 6
                if (success) {
                    sessionManager.saveUserSession(
                        email = email,
                        userId = "user_${System.currentTimeMillis()}",
                        accessToken = "access_token_${System.currentTimeMillis()}",
                        refreshToken = "refresh_token_${System.currentTimeMillis()}"
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Invalid credentials")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Something went wrong")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }

    private fun validate(email: String, password: String): LoginValidationResult {
        return when {
            email.isBlank() -> LoginValidationResult(false, "Email is required")
            !isValidEmail(email) -> LoginValidationResult(false, "Please enter a valid email")
            password.isBlank() -> LoginValidationResult(false, "Password is required")
            password.length < 6 -> LoginValidationResult(false, "Password must be at least 6 characters")
            else -> LoginValidationResult(true, null)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val error: String? = null
)

private data class LoginValidationResult(
    val isValid: Boolean,
    val error: String?
)
