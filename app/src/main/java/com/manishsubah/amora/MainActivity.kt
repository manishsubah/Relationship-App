package com.manishsubah.amora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.manishsubah.amora.ui.screens.WelcomeScreen
import com.manishsubah.amora.ui.theme.AmoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmoraTheme {
                WelcomeScreen(
                    onGetStartedClick = {
                        // TODO: Navigate to authentication screens
                        // For now, this is a placeholder
                    }
                )
            }
        }
    }
}
