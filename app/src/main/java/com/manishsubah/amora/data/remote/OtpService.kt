package com.manishsubah.amora.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling email OTP functionality using SendGrid API and Firestore.
 * 
 * This implementation uses:
 * - SendGrid free API (100 emails/day) - Direct from Android, no Cloud Functions needed
 * - Firestore free tier for OTP storage and verification
 * 
 * Follows the Single Responsibility Principle by handling only OTP operations.
 * 
 * @property firestore Firestore instance for OTP storage
 */
@Singleton
class OtpService @Inject constructor() {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    
    companion object {
        private const val OTP_COLLECTION = "otpCodes"
        private const val OTP_EXPIRY_MINUTES = 5L
        private const val MAX_ATTEMPTS = 5
        
        // Set to true for testing (logs OTP to console instead of sending email)
        private const val TEST_MODE = true
        
        // TODO: Replace with your SendGrid API Key when ready
        // Get free API key at: https://app.sendgrid.com/settings/api_keys
        private const val SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY"
        private const val SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send"
        private const val FROM_EMAIL = "noreply@amora.com"
    }
    
    /**
     * Sends OTP code to the specified email address using SendGrid API.
     * 
     * Generates a 6-digit OTP, stores it securely in Firestore, and sends email via SendGrid.
     * 
     * @param email User's email address
     * @return Result containing success status or error message
     */
    suspend fun sendOtp(email: String): Result<Unit> {
        return try {
            // Validate email
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email address"))
            }
            
            // Generate 6-digit OTP
            val otp = generateOtp()
            
            // Hash OTP for secure storage
            val otpHash = hashOtp(otp)
            
            // Store in Firestore with expiration
            val expiresAt = Timestamp(Date(System.currentTimeMillis() + OTP_EXPIRY_MINUTES * 60 * 1000))
            
            firestore.collection(OTP_COLLECTION)
                .document(email)
                .set(mapOf(
                    "otpHash" to otpHash,
                    "createdAt" to Timestamp.now(),
                    "expiresAt" to expiresAt,
                    "attempts" to 0
                ))
                .await()
            
            // Send email via SendGrid (or log in test mode)
            if (TEST_MODE) {
                android.util.Log.d("OTP_TEST", "🎯 TEST MODE - OTP for $email: $otp")
                android.util.Log.i("OTP_TEST", "OTP Code: $otp")
                println("🎯 TEST MODE - OTP for $email: $otp")
            } else {
                sendEmailViaSendGrid(email, otp)
            }
            
            android.util.Log.d("OTP_SERVICE", "OTP sent successfully for $email")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("OTP_ERROR", "Failed to send OTP: ${e.message}", e)
            android.util.Log.e("OTP_ERROR", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Verifies the OTP code entered by the user.
     * 
     * Validates OTP, checks expiration and attempt limits from Firestore.
     * 
     * @param email User's email address
     * @param otp OTP code entered by user
     * @return Result containing success status or error message
     */
    suspend fun verifyOtp(email: String, otp: String): Result<String> {
        return try {
            // Validate inputs
            if (otp.length != 6 || !otp.all { it.isDigit() }) {
                return Result.failure(Exception("OTP must be 6 digits"))
            }
            
            // Get stored OTP from Firestore
            val otpDoc = firestore.collection(OTP_COLLECTION)
                .document(email)
                .get()
                .await()
            
            if (!otpDoc.exists()) {
                return Result.failure(Exception("OTP not found or expired"))
            }
            
            val otpData = otpDoc.data ?: return Result.failure(Exception("Invalid OTP data"))
            
            // Check expiration
            val expiresAtTimestamp = otpData["expiresAt"] as? Timestamp
                ?: return Result.failure(Exception("Invalid expiration date"))
            
            if (expiresAtTimestamp.toDate().before(Date())) {
                otpDoc.reference.delete().await() // Clean up expired OTP
                return Result.failure(Exception("OTP expired. Please request a new one."))
            }
            
            // Check attempts
            val attempts = (otpData["attempts"] as? Long)?.toInt() ?: 0
            if (attempts >= MAX_ATTEMPTS) {
                otpDoc.reference.delete().await()
                return Result.failure(Exception("Too many attempts. Please request a new OTP."))
            }
            
            // Verify OTP
            val storedHash = otpData["otpHash"] as? String ?: return Result.failure(Exception("Invalid OTP data"))
            val inputHash = hashOtp(otp)
            
            if (inputHash != storedHash) {
                // Increment attempts
                otpDoc.reference.update("attempts", attempts + 1).await()
                return Result.failure(Exception("Invalid OTP code"))
            }
            
            // OTP verified successfully
            // Clean up used OTP
            otpDoc.reference.delete().await()
            
            // Return success token (you can customize this)
            Result.success("verified_${System.currentTimeMillis()}")
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generates a random 6-digit OTP.
     */
    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }
    
    /**
     * Hashes OTP using SHA-256 for secure storage.
     */
    private fun hashOtp(otp: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(otp.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Sends email via SendGrid REST API.
     * 
     * Uses HTTP POST to SendGrid API to send OTP email.
     */
    private suspend fun sendEmailViaSendGrid(email: String, otp: String) {
        try {
            val url = java.net.URL(SENDGRID_API_URL)
            val connection = url.openConnection() as java.net.HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $SENDGRID_API_KEY")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // Create email payload
            val emailBody = """
                {
                    "personalizations": [{
                        "to": [{"email": "$email"}]
                    }],
                    "from": {"email": "$FROM_EMAIL"},
                    "subject": "Your Amora Verification Code",
                    "content": [{
                        "type": "text/html",
                        "value": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'><h2 style='color: #E91E63; text-align: center;'>Welcome to Amora! 💕</h2><p style='font-size: 16px;'>Your verification code is:</p><div style='background-color: #000; color: #fff; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; border-radius: 8px; margin: 20px 0;'>$otp</div><p style='font-size: 14px; color: #666;'>This code will expire in 5 minutes.</p><p style='font-size: 14px; color: #666;'>If you didn't request this code, please ignore this email.</p><hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'><p style='color: #999; font-size: 12px; text-align: center;'>© 2025 Amora - Building meaningful connections</p></div>"
                    }]
                }
            """.trimIndent()
            
            connection.outputStream.use { os ->
                os.write(emailBody.toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorMessage = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("SendGrid API error: $responseCode - $errorMessage")
            }
            
        } catch (e: Exception) {
            throw Exception("Failed to send email: ${e.message}")
        }
    }
    
    /**
     * Validates email format.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
}