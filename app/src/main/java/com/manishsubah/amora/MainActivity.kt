package com.manishsubah.amora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.manishsubah.amora.data.local.SessionManager
import com.manishsubah.amora.data.local.SessionManagerEntryPoint
import com.manishsubah.amora.ui.screens.HomeScreen
import com.manishsubah.amora.ui.screens.LoginScreen
import com.manishsubah.amora.ui.screens.SignupScreen
import com.manishsubah.amora.ui.screens.WelcomeScreen
import com.manishsubah.amora.ui.theme.AmoraTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AmoraTheme {
                var currentScreen by remember { mutableStateOf("welcome") }
                
                // Access SessionManager through Hilt EntryPoint
                val sessionManager = remember {
                    EntryPointAccessors.fromApplication(
                        applicationContext,
                        SessionManagerEntryPoint::class.java
                    ).sessionManager()
                }
                
                // Check login status on composition
                LaunchedEffect(Unit) {
                    if (sessionManager.isLoggedIn()) {
                        currentScreen = "home"
                    }
                }
                
                when (currentScreen) {
                    "welcome" -> WelcomeScreen(
                        onGetStartedClick = {
                            currentScreen = "signup"
                        }
                    )
                    "signup" -> SignupScreen(
                        onSignupClick = { _, _, _ ->
                            currentScreen = "login"
                        },
                        onLoginClick = {
                            currentScreen = "login"
                        }
                    )
                    "login" -> LoginScreen(
                        onLoginSuccess = { userIdentifier ->
                            // Navigate to home screen on successful login
                            currentScreen = "home"
                        },
                        onSignupClick = {
                            currentScreen = "signup"
                        }
                    )
                    "home" -> HomeScreen(
                        userIdentifier = sessionManager.getUserEmail() ?: "User",
                        onLogout = {
                            sessionManager.clearSession()
                            currentScreen = "welcome"
                        }
                    )
                }
            }
        }
    }
}
