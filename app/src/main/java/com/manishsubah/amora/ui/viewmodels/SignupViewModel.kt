package com.manishsubah.amora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manishsubah.amora.data.local.SessionManager
import com.manishsubah.amora.data.remote.OtpService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val otpService: OtpService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun signup(email: String, password: String, otp: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                val validationResult = validateSignupInputs(email, password, otp)
                if (!validationResult.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validationResult.errorMessage
                    )
                    return@launch
                }

                val signupResult = performSignupApiCall(email, password, otp)
                
                if (signupResult.isSuccess) {
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

    private suspend fun performSignupApiCall(
        email: String, 
        password: String, 
        otp: String
    ): SignupResult {
        kotlinx.coroutines.delay(2000)

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

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSignupSuccess() {
        _uiState.value = _uiState.value.copy(isSignupSuccessful = false)
    }
    
    fun sendOtp(email: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("SignupViewModel", "sendOtp called for email: $email")
                
                if (email.isBlank()) {
                    android.util.Log.w("SignupViewModel", "Email is blank")
                    _uiState.value = _uiState.value.copy(
                        error = "Email is required",
                        isSendingOtp = false
                    )
                    return@launch
                }
                
                if (!isValidEmail(email)) {
                    android.util.Log.w("SignupViewModel", "Invalid email format: $email")
                    _uiState.value = _uiState.value.copy(
                        error = "Please enter a valid email address",
                        isSendingOtp = false
                    )
                    return@launch
                }
                
                android.util.Log.d("SignupViewModel", "Starting OTP send process...")
                _uiState.value = _uiState.value.copy(
                    isSendingOtp = true,
                    error = null
                )
                
                android.util.Log.d("SignupViewModel", "Calling otpService.sendOtp()...")
                val result = otpService.sendOtp(email)
                
                result.fold(
                    onSuccess = {
                        android.util.Log.i("SignupViewModel", "OTP sent successfully!")
                        _uiState.value = _uiState.value.copy(
                            isSendingOtp = false,
                            isOtpSent = true,
                            otpResendSeconds = 60 // Start 60-second cooldown
                        )
                        startOtpResendTimer()
                    },
                    onFailure = { exception ->
                        android.util.Log.e("SignupViewModel", "OTP send failed: ${exception.message}", exception)
                        _uiState.value = _uiState.value.copy(
                            isSendingOtp = false,
                            error = exception.message ?: "Failed to send OTP. Please try again."
                        )
                    }
                )
                
            } catch (e: Exception) {
                android.util.Log.e("SignupViewModel", "Unexpected error in sendOtp: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSendingOtp = false,
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    fun verifyOtpAndSignup(email: String, password: String, otp: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                val validationResult = validateSignupInputs(email, password, otp)
                if (!validationResult.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validationResult.errorMessage
                    )
                    return@launch
                }

                val verifyResult = otpService.verifyOtp(email, otp)
                
                verifyResult.fold(
                    onSuccess = { customToken ->
                        sessionManager.saveUserSession(
                            email = email,
                            userId = "user_${System.currentTimeMillis()}",
                            accessToken = customToken,
                            refreshToken = "refresh_token_${System.currentTimeMillis()}"
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignupSuccessful = true
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Invalid OTP. Please try again."
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    private fun startOtpResendTimer() {
        viewModelScope.launch {
            android.util.Log.d("SignupViewModel", "Starting OTP resend timer (60 seconds)")
            var seconds = 60
            while (seconds > 0) {
                kotlinx.coroutines.delay(1000)
                seconds--
                _uiState.value = _uiState.value.copy(otpResendSeconds = seconds)
                if (seconds % 10 == 0) { // Log every 10 seconds
                    android.util.Log.d("SignupViewModel", "Resend timer: $seconds seconds remaining")
                }
            }
            android.util.Log.d("SignupViewModel", "Resend timer completed - resend now available")
            _uiState.value = _uiState.value.copy(otpResendSeconds = 0)
        }
    }
}

data class SignupUiState(
    val isLoading: Boolean = false,
    val isSignupSuccessful: Boolean = false,
    val error: String? = null,
    val isSendingOtp: Boolean = false,
    val isOtpSent: Boolean = false,
    val otpResendSeconds: Int = 0
)

private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)

private data class SignupResult(
    val isSuccess: Boolean,
    val userId: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val errorMessage: String?
)
