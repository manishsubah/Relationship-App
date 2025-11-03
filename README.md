# Amora - Relationship App 💕

> **Amora** - Derived from "amor" (love) in Latin, representing the essence of meaningful connections and relationships.

## 📱 What is Amora?

Amora is a modern Android relationship app that helps people build meaningful connections. Built with the latest Android technologies and a focus on security and great user experience.

## ✨ Key Features

### 🔐 Secure Authentication (Recently Implemented!)

I just completed building a dual OTP authentication system:

- **Email OTP**: Get verification codes via email
- **Mobile OTP**: Get verification codes via SMS

**Security Features:**
- ✅ OTP codes hashed with SHA-256
- ✅ 5-minute expiration time
- ✅ Maximum 3 verification attempts
- ✅ 60-second resend cooldown
- ✅ Secure session management

**Why it's Cool:**
- Built completely free using Firebase
- No paid services required
- Industry-standard security practices
- Clean, maintainable code architecture

### 🎨 Beautiful Design

- Modern Material Design 3
- Clean, intuitive interface
- Smooth animations
- Pink/rose color theme for a warm, friendly feel

## 🛠️ Built With

- **Language**: Kotlin
- **UI**: Jetpack Compose (latest Android UI framework)
- **Architecture**: Clean Architecture with MVVM pattern
- **Authentication**: Firebase Auth + Firestore
- **Dependency Injection**: Dagger Hilt
- **Async**: Kotlin Coroutines & Flow

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Android device or emulator (Android 9+)

### Quick Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RelationShipApp
   ```

2. **Open in Android Studio**
   - Click "Open" and select the project folder
   - Let Gradle sync

3. **Configure Firebase**
   - Add your `google-services.json` to the `app/` folder
   - (Already included in the project)

4. **Run the app**
   - Click the green play button
   - Or run: `./gradlew assembleDebug`

## 🎯 Project Highlights

### OTP Authentication Implementation

This was a challenging and rewarding feature to build! Here's what I learned:

**Challenges Solved:**
1. ❌ Firebase Cloud Functions required paid plan → ✅ Used Firestore directly
2. ❌ Phone number format errors → ✅ Auto-format with country code (+91)
3. ❌ Complex error messages → ✅ User-friendly translations
4. ❌ Activity context issues → ✅ Proper context passing from Compose

**Technical Approach:**
- Store OTP in Firestore with encryption
- Hash codes before storage (never store plain text)
- Limit verification attempts to prevent brute force
- Clean up expired OTPs automatically
- E.164 phone number format for SMS

### Clean Code Architecture

```
📱 Presentation (UI)
   ├── Compose screens
   └── ViewModels

💼 Domain (Business Logic)
   ├── Use Cases
   └── Repository Interfaces

📊 Data (Data Management)
   ├── Firebase Auth
   ├── Firestore
   └── Local Storage
```

## 📋 Current Progress

- ✅ Project setup with Clean Architecture
- ✅ Email OTP authentication
- ✅ Mobile OTP authentication
- ✅ Beautiful UI design system
- ✅ Secure session management
- 🔄 User profile management (in progress)
- 📝 Matching system (planned)
- 📝 Chat and messaging (planned)

## 📖 Documentation

Want to know more about how I built the OTP system? Check out:
- `OTP_IMPLEMENTATION_JOURNEY.md` - My complete journey and learnings
- `FIREBASE_EMAIL_OTP_SETUP.md` - Email OTP setup guide
- `HOW_TO_FIND_OTP.md` - Finding OTPs during testing

## 🎓 What I Learned

Building this app taught me:
- **Security First**: Never compromise on user data protection
- **Clean Code Matters**: Proper architecture makes everything easier
- **Free Tools Are Powerful**: Firebase free tier is surprisingly capable
- **User Experience**: Make errors clear and helpful
- **Problem Solving**: Every challenge has a creative solution

## 🤝 Contributing

This is a personal learning project, but I'm open to:
- Suggestions for improvements
- Bug reports
- Feature ideas
- Code reviews

## 📄 License

MIT License - Feel free to learn from this code!

## 👨‍💻 Developer

**Manish Subah**

Building Amora to learn modern Android development and create something meaningful.

---

**Amora** - Building meaningful connections with clean code and secure authentication 💕

*Built with ❤️ using Kotlin and Jetpack Compose*

---

### 🌟 Recent Achievement

Just completed implementing a secure, dual-mode OTP authentication system using Firebase - completely free and following industry best practices! 🎉
