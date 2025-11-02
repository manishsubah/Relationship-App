const {setGlobalOptions} = require("firebase-functions");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {initializeApp} = require("firebase-admin/app");
const {getAuth, getFirestore} = require("firebase-admin");
const nodemailer = require("nodemailer");
const crypto = require("crypto");

// Initialize Firebase Admin
initializeApp();

// Configure email transporter
// TODO: Replace with your email credentials
// For Gmail: Use App Password (not regular password)
// Generate at: https://myaccount.google.com/apppasswords
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "manishranjan210@gmail.com",
    pass: "faugdtxptafglwnb",
  },
});

// Set global options
setGlobalOptions({maxInstances: 10});

/**
 * Send OTP to email address
 * 
 * Generates a 6-digit OTP, stores it securely in Firestore with expiration,
 * and sends it to the user's email.
 */
exports.sendEmailOTP = onCall(async (request) => {
  const {email} = request.data;

  // Validate email
  if (!email || !email.includes("@")) {
    throw new HttpsError("invalid-argument", "Invalid email address");
  }

  // Generate 6-digit OTP
  const otp = Math.floor(100000 + Math.random() * 900000).toString();

  // Hash OTP for secure storage
  const otpHash = crypto.createHash("sha256").update(otp).digest("hex");

  // Store in Firestore with 5-minute expiration
  const db = getFirestore();
  const expiresAt = new Date(Date.now() + 5 * 60 * 1000); // 5 minutes

  await db.collection("otpCodes").doc(email).set({
    otpHash: otpHash,
    createdAt: new Date(),
    expiresAt: expiresAt,
    attempts: 0,
  });

  // Send email with OTP
  const mailOptions = {
    from: "Amora <manishranjan210@gmail.com>",
    to: email,
    subject: "Your Amora Verification Code",
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
        <h2 style="color: #E91E63; text-align: center;">Welcome to Amora! 💕</h2>
        <p style="font-size: 16px;">Your verification code is:</p>
        <div style="background-color: #000; color: #fff; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; border-radius: 8px; margin: 20px 0;">
          ${otp}
        </div>
        <p style="font-size: 14px; color: #666;">This code will expire in 5 minutes.</p>
        <p style="font-size: 14px; color: #666;">If you didn't request this code, please ignore this email.</p>
        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
        <p style="color: #999; font-size: 12px; text-align: center;">© 2025 Amora - Building meaningful connections</p>
      </div>
    `,
  };

  try {
    await transporter.sendMail(mailOptions);
    return {success: true, message: "OTP sent successfully"};
  } catch (error) {
    console.error("Error sending email:", error);
    throw new HttpsError("internal", "Failed to send email. Please try again.");
  }
});

/**
 * Verify OTP code
 * 
 * Validates the OTP code, checks expiration and attempt limits,
 * and returns a custom token for Firebase Authentication.
 */
exports.verifyEmailOTP = onCall(async (request) => {
  const {email, otp} = request.data;

  // Validate inputs
  if (!email || !otp) {
    throw new HttpsError("invalid-argument", "Email and OTP are required");
  }

  if (otp.length !== 6 || !/^\d+$/.test(otp)) {
    throw new HttpsError("invalid-argument", "OTP must be 6 digits");
  }

  // Get stored OTP from Firestore
  const db = getFirestore();
  const otpDoc = await db.collection("otpCodes").doc(email).get();

  if (!otpDoc.exists) {
    throw new HttpsError("not-found", "OTP not found or expired");
  }

  const otpData = otpDoc.data();

  // Check expiration
  const expiresAt = otpData.expiresAt.toDate();
  if (expiresAt < new Date()) {
    await otpDoc.ref.delete(); // Clean up expired OTP
    throw new HttpsError("deadline-exceeded", "OTP expired. Please request a new one.");
  }

  // Check attempts (max 5 attempts)
  if (otpData.attempts >= 5) {
    await otpDoc.ref.delete();
    throw new HttpsError("resource-exhausted", "Too many attempts. Please request a new OTP.");
  }

  // Verify OTP
  const otpHash = crypto.createHash("sha256").update(otp).digest("hex");

  if (otpHash !== otpData.otpHash) {
    // Increment attempts
    await otpDoc.ref.update({
      attempts: otpData.attempts + 1,
    });
    throw new HttpsError("invalid-argument", "Invalid OTP code");
  }

  // OTP verified successfully
  const auth = getAuth();
  let userRecord;

  try {
    // Check if user exists
    userRecord = await auth.getUserByEmail(email);
  } catch (error) {
    // User doesn't exist, create new user
    userRecord = await auth.createUser({
      email: email,
      emailVerified: true,
    });
  }

  // Create custom token for Firebase Authentication
  const customToken = await auth.createCustomToken(userRecord.uid);

  // Clean up used OTP
  await otpDoc.ref.delete();

  return {
    success: true,
    customToken: customToken,
    userId: userRecord.uid,
  };
});
