package com.manishsubah.amora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Amora app.
 * 
 * This class is required for Hilt dependency injection to work properly.
 * The @HiltAndroidApp annotation triggers Hilt's code generation.
 */
@HiltAndroidApp
class AmoraApplication : Application()
