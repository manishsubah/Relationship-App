package com.manishsubah.amora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manishsubah.amora.ui.theme.AmoraTheme
import com.manishsubah.amora.ui.viewmodels.SignupViewModel

@Composable
fun SignupScreen(
    onSignupClick: (email: String, password: String, otp: String) -> Unit = { _, _, _ -> },
    onLoginClick: () -> Unit = {},
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var otpVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSignupSuccessful) {
        if (uiState.isSignupSuccessful) {
            onSignupClick(email, password, otp)
            viewModel.resetSignupSuccess()
        }
    }
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // Display error - you can add Snackbar here if needed
            // For now, we'll show it in the emailError field if it's OTP related
            val errorMsg = uiState.error
            if (errorMsg?.contains("OTP", ignoreCase = true) == true ||
                errorMsg?.contains("email", ignoreCase = true) == true
            ) {
                emailError = errorMsg
            }
            viewModel.clearError()
        }
    }
    
    // Show success message when OTP is sent
    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            // OTP sent successfully - message is displayed in UI
            // In test mode, OTP is also logged to Logcat
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                ),
                                radius = 800f
                            ),
                            shape = RoundedCornerShape(
                                bottomStart = 60.dp,
                                bottomEnd = 60.dp
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(60.dp)
                        )
                )

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    fontSize = 28.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp, top = 40.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "email@test.com",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        isError = emailError.isNotEmpty(),
                        singleLine = true
                    )
                    
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Send OTP Button
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (!uiState.isOtpSent) {
                        // Send OTP button (first time)
                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    emailError = "Email is required"
                                } else if (!isValidEmail(email)) {
                                    emailError = "Please enter a valid email"
                                } else {
                                    viewModel.sendOtp(email)
                                }
                            },
                            enabled = !uiState.isSendingOtp && email.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSendingOtp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            } else {
                                Text(
                                    text = "Send OTP",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Resend OTP button with countdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OTP sent to your email",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                fontSize = 12.sp
                            )
                            
                            TextButton(
                                onClick = {
                                    if (email.isNotBlank() && isValidEmail(email)) {
                                        viewModel.sendOtp(email)
                                    }
                                },
                                enabled = uiState.otpResendSeconds == 0 && !uiState.isSendingOtp
                            ) {
                                Text(
                                    text = if (uiState.otpResendSeconds > 0) {
                                        "Resend OTP (${uiState.otpResendSeconds}s)"
                                    } else {
                                        "Resend OTP"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter your password",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        isError = passwordError.isNotEmpty(),
                        singleLine = true
                    )
                    
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "OTP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { newValue ->
                            // Only allow numeric input and limit to 6 digits
                            val filtered = newValue.filter { it.isDigit() }.take(6)
                            otp = filtered
                            otpError = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter 6-digit OTP",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (otpVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        trailingIcon = {
                            IconButton(onClick = { otpVisible = !otpVisible }) {
                                Text(
                                    text = if (otpVisible) "Hide" else "Show",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        isError = otpError.isNotEmpty(),
                        singleLine = true
                    )
                    
                    if (otpError.isNotEmpty()) {
                        Text(
                            text = otpError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        var hasError = false
                        
                        if (email.isBlank()) {
                            emailError = "Email is required"
                            hasError = true
                        } else if (!isValidEmail(email)) {
                            emailError = "Please enter a valid email"
                            hasError = true
                        }
                        
                        if (password.isBlank()) {
                            passwordError = "Password is required"
                            hasError = true
                        } else if (password.length < 6) {
                            passwordError = "Password must be at least 6 characters"
                            hasError = true
                        }
                        
                        if (!uiState.isOtpSent) {
                            emailError = "Please send OTP first"
                            hasError = true
                        }
                        
                        if (otp.isBlank()) {
                            otpError = "OTP is required"
                            hasError = true
                        } else if (otp.length != 6) {
                            otpError = "OTP must be 6 digits"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            viewModel.verifyOtpAndSignup(email, password, otp)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            fontSize = 16.sp
                        )
                    }
                }

                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? Login",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
    return emailRegex.matches(email)
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    AmoraTheme {
        SignupScreen()
    }
}
