package com.manishsubah.amora.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences utility for managing user session and app preferences.
 * 
 * This class follows the Single Responsibility Principle by handling only
 * local data persistence operations. It provides a clean interface for
 * storing and retrieving user authentication state and app preferences.
 * 
 * @param context Application context for SharedPreferences access
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "amora_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    /**
     * Saves user login session information.
     * 
     * @param email User's email address
     * @param userId Unique user identifier
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     */
    fun saveUserSession(
        email: String,
        userId: String,
        accessToken: String,
        refreshToken: String
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ID, userId)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    /**
     * Checks if user is currently logged in.
     * 
     * @return true if user has valid session, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Retrieves stored user email.
     * 
     * @return user email or null if not available
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Retrieves stored user ID.
     * 
     * @return user ID or null if not available
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Retrieves stored access token.
     * 
     * @return access token or null if not available
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Retrieves stored refresh token.
     * 
     * @return refresh token or null if not available
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Clears all user session data.
     * Call this method when user logs out.
     */
    fun clearSession() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    /**
     * Updates only the access token while keeping other session data.
     * Useful for token refresh scenarios.
     * 
     * @param newAccessToken Updated access token
     */
    fun updateAccessToken(newAccessToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, newAccessToken)
            apply()
        }
    }
}
