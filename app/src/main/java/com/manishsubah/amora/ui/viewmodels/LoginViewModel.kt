package com.manishsubah.amora.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.manishsubah.amora.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var forceResendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verification completed (instant verification)
            signInWithPhoneCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            android.util.Log.e("LoginViewModel", "Phone verification failed: ${e.message}", e)
            // Provide user-friendly error message
            val errorMessage = when {
                e.message?.contains("TOO_SHORT") == true -> {
                    "Phone number is too short. Please include country code (e.g., +91 for India)"
                }
                e.message?.contains("INVALID_PHONE_NUMBER") == true -> {
                    "Invalid phone number format. Please use E.164 format: +{country code}{number}"
                }
                e.message?.contains("format") == true -> {
                    "Invalid phone number format. Use format: +{country code}{number} (e.g., +91 9876543210)"
                }
                else -> {
                    "Failed to send OTP: ${e.message}"
                }
            }
            _uiState.value = _uiState.value.copy(
                isSendingOtp = false,
                error = errorMessage
            )
        }

        override fun onCodeSent(
            vid: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            android.util.Log.d("LoginViewModel", "OTP code sent successfully")
            verificationId = vid
            forceResendToken = token
            _uiState.value = _uiState.value.copy(
                isSendingOtp = false,
                isOtpSent = true,
                otpResendSeconds = 60
            )
            startOtpResendTimer()
        }
    }

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
    
    /**
     * Sends phone OTP via Firebase Phone Authentication.
     * 
     * @param phoneNumber Phone number in E.164 format (e.g., +1234567890)
     * @param activity Current activity for reCAPTCHA verification
     */
    fun sendPhoneOtp(phoneNumber: String, activity: Activity? = null) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "Sending OTP to: $phoneNumber")
                
                _uiState.value = _uiState.value.copy(
                    isSendingOtp = true,
                    error = null
                )
                
                // Build phone auth options
                val builder = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setCallbacks(phoneAuthCallbacks)
                
                // Add activity for reCAPTCHA if available
                activity?.let {
                    builder.setActivity(it)
                }
                
                // Send OTP
                PhoneAuthProvider.verifyPhoneNumber(builder.build())
                
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error sending OTP: ${e.message}", e)
                // Provide user-friendly error message
                val errorMessage = when {
                    e.message?.contains("TOO_SHORT") == true -> {
                        "Phone number is too short. Please include country code (e.g., +91 for India)"
                    }
                    e.message?.contains("INVALID_PHONE_NUMBER") == true -> {
                        "Invalid phone number format. Please use E.164 format: +{country code}{number}"
                    }
                    e.message?.contains("format") == true -> {
                        "Invalid phone number format. Use format: +{country code}{number} (e.g., +91 9876543210)"
                    }
                    else -> {
                        "Failed to send OTP: ${e.message}"
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isSendingOtp = false,
                    error = errorMessage
                )
            }
        }
    }
    
    /**
     * Resends phone OTP using the force resend token.
     * 
     * @param phoneNumber Phone number in E.164 format
     * @param activity Current activity for reCAPTCHA verification
     */
    fun resendPhoneOtp(phoneNumber: String, activity: Activity? = null) {
        val token = forceResendToken ?: run {
            android.util.Log.w("LoginViewModel", "No resend token available, sending new OTP")
            sendPhoneOtp(phoneNumber, activity)
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSendingOtp = true,
                    error = null
                )
                
                val builder = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setCallbacks(phoneAuthCallbacks)
                    .setForceResendingToken(token)
                
                // Add activity for reCAPTCHA if available
                activity?.let {
                    builder.setActivity(it)
                }
                
                PhoneAuthProvider.verifyPhoneNumber(builder.build())
                
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error resending OTP: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSendingOtp = false,
                    error = "Failed to resend OTP: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Verifies the OTP code entered by user.
     * 
     * @param phoneNumber Phone number in E.164 format
     * @param code 6-digit OTP code
     */
    fun verifyPhoneOtp(phoneNumber: String, code: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                val vid = verificationId ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Please send OTP first"
                    )
                    return@launch
                }
                
                // Create credential from verification ID and code
                val credential = PhoneAuthProvider.getCredential(vid, code)
                
                // Sign in with credential
                signInWithPhoneCredential(credential)
                
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "OTP verification failed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Invalid OTP code. Please try again."
                )
            }
        }
    }
    
    /**
     * Signs in user with phone credential and saves session.
     * 
     * @param credential PhoneAuthCredential from Firebase
     */
    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                
                val user = result.user
                val phoneNumber = user?.phoneNumber ?: ""
                
                // Save session
                sessionManager.saveUserSession(
                    email = phoneNumber, // Using phone number as identifier
                    userId = user?.uid ?: "user_${System.currentTimeMillis()}",
                    accessToken = (user?.getIdToken(false)?.await() ?: "phone_token_${System.currentTimeMillis()}") as String,
                    refreshToken = "refresh_token_${System.currentTimeMillis()}"
                )
                
                android.util.Log.d("LoginViewModel", "Phone login successful for: $phoneNumber")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = true
                )
                
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Sign in failed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Login failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Starts countdown timer for OTP resend functionality.
     */
    private fun startOtpResendTimer() {
        viewModelScope.launch {
            var seconds = 60
            while (seconds > 0) {
                kotlinx.coroutines.delay(1000)
                seconds--
                _uiState.value = _uiState.value.copy(otpResendSeconds = seconds)
            }
            _uiState.value = _uiState.value.copy(otpResendSeconds = 0)
        }
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
    val error: String? = null,
    val isSendingOtp: Boolean = false,
    val isOtpSent: Boolean = false,
    val otpResendSeconds: Int = 0
)

private data class LoginValidationResult(
    val isValid: Boolean,
    val error: String?
)
