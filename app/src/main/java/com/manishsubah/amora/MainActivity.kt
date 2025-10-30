package com.manishsubah.amora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.manishsubah.amora.ui.screens.LoginScreen
import com.manishsubah.amora.ui.screens.SignupScreen
import com.manishsubah.amora.ui.screens.WelcomeScreen
import com.manishsubah.amora.ui.theme.AmoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmoraTheme {
                var currentScreen by remember { mutableStateOf("welcome") }
                
                when (currentScreen) {
                    "welcome" -> WelcomeScreen(
                        onGetStartedClick = {
                            currentScreen = "signup"
                        }
                    )
                    "signup" -> SignupScreen(
                        onSignupClick = { _, _, _ ->
                            // After successful signup, navigate to login
                            currentScreen = "login"
                        },
                        onLoginClick = {
                            currentScreen = "login"
                        }
                    )
                    "login" -> LoginScreen(
                        onLoginSuccess = {
                            // After login, go to welcome (or home when available)
                            currentScreen = "welcome"
                        },
                        onSignupClick = {
                            currentScreen = "signup"
                        }
                    )
                }
            }
        }
    }
}
