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

/**
 * ViewModel for handling signup screen business logic.
 * 
 * This ViewModel follows the MVVM pattern and Single Responsibility Principle
 * by managing only signup-related state and operations. It handles form validation,
 * API calls, and session management through dependency injection.
 * 
 * @param sessionManager Handles local session storage
 */
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI State management
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    /**
     * Handles user signup process.
     * 
     * This method validates input, makes API call, and manages session state.
     * It follows the Open/Closed Principle by being extensible for different
     * signup strategies without modifying existing code.
     * 
     * @param email User's email address
     * @param password User's password
     * @param otp One-time password for verification
     */
    fun signup(email: String, password: String, otp: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                // Validate inputs
                val validationResult = validateSignupInputs(email, password, otp)
                if (!validationResult.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validationResult.errorMessage
                    )
                    return@launch
                }

                // Simulate API call (replace with actual API call)
                val signupResult = performSignupApiCall(email, password, otp)
                
                if (signupResult.isSuccess) {
                    // Save session data - ensure non-null values
                    val userId = signupResult.userId ?: "unknown_user"
                    val accessToken = signupResult.accessToken ?: "temp_access_token"
                    val refreshToken = signupResult.refreshToken ?: "temp_refresh_token"
                    
                    sessionManager.saveUserSession(
                        email = email,
                        userId = userId,
                        accessToken = accessToken,
                        refreshToken = refreshToken
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignupSuccessful = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = signupResult.errorMessage ?: "Signup failed"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }

    /**
     * Validates signup form inputs.
     * 
     * @param email Email to validate
     * @param password Password to validate
     * @param otp OTP to validate
     * @return ValidationResult with validation status and error message
     */
    private fun validateSignupInputs(email: String, password: String, otp: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !isValidEmail(email) -> ValidationResult(false, "Please enter a valid email address")
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
            otp.isBlank() -> ValidationResult(false, "OTP is required")
            otp.length != 6 -> ValidationResult(false, "OTP must be 6 digits")
            !otp.all { it.isDigit() } -> ValidationResult(false, "OTP must contain only numbers")
            else -> ValidationResult(true, null)
        }
    }

    /**
     * Performs signup API call.
     * 
     * This method simulates an API call. In a real implementation,
     * this would make an actual network request to your backend.
     * 
     * @param email User's email
     * @param password User's password
     * @param otp User's OTP
     * @return SignupResult with success status and user data
     */
    private suspend fun performSignupApiCall(
        email: String, 
        password: String, 
        otp: String
    ): SignupResult {
        // Simulate network delay
        kotlinx.coroutines.delay(2000)
        
        // Simulate API response (replace with actual API call)
        return if (email.contains("@") && password.length >= 6 && otp.length == 6) {
            SignupResult(
                isSuccess = true,
                userId = "user_${System.currentTimeMillis()}",
                accessToken = "access_token_${System.currentTimeMillis()}",
                refreshToken = "refresh_token_${System.currentTimeMillis()}",
                errorMessage = null
            )
        } else {
            SignupResult(
                isSuccess = false,
                userId = null,
                accessToken = null,
                refreshToken = null,
                errorMessage = "Invalid credentials. Please check your information."
            )
        }
    }

    /**
     * Validates email format using regex.
     * 
     * @param email Email string to validate
     * @return true if email format is valid
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    /**
     * Clears any error messages from the UI state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Resets the signup success state.
     */
    fun resetSignupSuccess() {
        _uiState.value = _uiState.value.copy(isSignupSuccessful = false)
    }
}

/**
 * UI State data class for signup screen.
 * 
 * This follows the Data Transfer Object pattern and encapsulates
 * all UI-related state in a single, immutable data class.
 */
data class SignupUiState(
    val isLoading: Boolean = false,
    val isSignupSuccessful: Boolean = false,
    val error: String? = null
)

/**
 * Validation result data class.
 * 
 * Encapsulates validation status and error message.
 */
private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)

/**
 * Signup API result data class.
 * 
 * Encapsulates the result of signup API call including
 * success status and user authentication data.
 */
private data class SignupResult(
    val isSuccess: Boolean,
    val userId: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val errorMessage: String?
)
