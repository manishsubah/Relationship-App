# Amora - Relationship App 💕

> **Amora** - Derived from "amor" (love) in Latin, representing the essence of meaningful connections and relationships.

## 📱 Project Overview

Amora is a modern Android relationship application built with Jetpack Compose, designed to help users build and maintain meaningful connections. The app focuses on creating a beautiful, intuitive user experience that facilitates genuine relationships through thoughtful design and user-centric features.

## 🎨 UI Design Concept

Based on the provided design mockups, Amora features a clean, modern interface with:

### Design System
- **Color Palette**: 
  - Primary: Deep pink/rose (`#E91E63` / `#C2185B`)
  - Secondary: Soft pink (`#FCE4EC`)
  - Accent: Light pink (`#F8BBD9`)
  - Tertiary: Warm purple (`#9C27B0`)
  - Background: White and light gray tones (`#FAFAFA` / `#F5F5F5`)
  - Text: Dark gray (`#424242`) and white for contrast

- **Typography**: Clean, modern fonts with clear hierarchy
- **Components**: Rounded corners, subtle shadows, and smooth transitions
- **Layout**: Card-based design with curved elements and overlapping shapes

### Screen Flow
1. **Welcome/Onboarding Screen**: Introduction to Amora with engaging visuals
2. **Authentication Screens**: 
   - Login with email/password and social authentication
   - Account creation with secure registration
3. **Main App Experience**: Core relationship features and interactions

## 🏗️ Architecture & Development Principles

### Clean Architecture Implementation

We follow **Clean Architecture** principles with clear separation of concerns:

```
📁 Presentation Layer (UI)
├── 🎨 Compose UI Components
├── 🎯 ViewModels (MVVM)
└── 🧭 Navigation

📁 Domain Layer (Business Logic)
├── 📋 Use Cases
├── 🏪 Repository Interfaces
└── 📊 Domain Models

📁 Data Layer (Data Management)
├── 🗄️ Local Database (Room)
├── 🌐 Remote API (Retrofit)
└── 📱 Data Sources
```

### SOLID Principles Implementation

1. **Single Responsibility Principle (SRP)**
   - Each class has one reason to change
   - Clear separation between UI, business logic, and data

2. **Open/Closed Principle (OCP)**
   - Extensible architecture without modifying existing code
   - Plugin-based feature system

3. **Liskov Substitution Principle (LSP)**
   - Repository implementations are interchangeable
   - Consistent interfaces across layers

4. **Interface Segregation Principle (ISP)**
   - Focused interfaces for specific functionalities
   - No unnecessary dependencies

5. **Dependency Inversion Principle (DIP)**
   - Dependency injection with Hilt
   - Abstractions over concrete implementations

### Modular Architecture

```
📦 app/
├── 📁 core/
│   ├── 📁 di/          # Dependency Injection
│   ├── 📁 network/     # Network configuration
│   ├── 📁 database/    # Database setup
│   └── 📁 utils/       # Common utilities
├── 📁 features/
│   ├── 📁 auth/        # Authentication module
│   ├── 📁 profile/     # User profile module
│   ├── 📁 matches/     # Matching system
│   └── 📁 chat/        # Messaging module
├── 📁 ui/
│   ├── 📁 theme/       # Design system
│   ├── 📁 components/  # Reusable components
│   └── 📁 navigation/  # App navigation
└── 📁 data/
    ├── 📁 local/       # Local data sources
    ├── 📁 remote/      # Remote data sources
    └── 📁 repository/  # Repository implementations
```

## 🛠️ Technology Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Async Operations**: Kotlin Coroutines + Flow

### Key Libraries
- **Compose BOM**: `2024.09.00` - Latest Compose components
- **Material 3**: Modern Material Design components
- **Navigation Compose**: Type-safe navigation
- **Room**: Local database for offline support
- **Retrofit**: Network API communication
- **Coil**: Image loading and caching
- **Timber**: Logging framework

### Development Tools
- **Kotlin**: `2.0.21` - Latest Kotlin features
- **Android Gradle Plugin**: `8.13.0`
- **Target SDK**: 36 (Android 14)
- **Min SDK**: 28 (Android 9)

## 📋 Development Guidelines

### Code Quality Standards

1. **Naming Conventions**
   ```kotlin
   // Classes: PascalCase
   class UserProfileViewModel
   
   // Functions: camelCase
   fun getUserProfile()
   
   // Constants: UPPER_SNAKE_CASE
   const val MAX_PROFILE_IMAGES = 5
   
   // Variables: camelCase
   val userProfile: UserProfile
   ```

2. **Class Structure**
   ```kotlin
   class FeatureViewModel @Inject constructor(
       private val useCase: FeatureUseCase
   ) : ViewModel() {
       
       // Private properties first
       private val _uiState = MutableStateFlow(FeatureUiState())
       val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
       
       // Public methods
       fun performAction() {
           // Implementation
       }
       
       // Private helper methods
       private fun updateState() {
           // Implementation
       }
   }
   ```

3. **Documentation Standards**
   ```kotlin
   /**
    * Handles user authentication and session management.
    * 
    * This repository provides a clean interface for authentication operations,
    * abstracting away the complexity of local and remote data sources.
    * 
    * @param localDataSource Handles local authentication data
    * @param remoteDataSource Manages remote authentication API calls
    */
   class AuthRepository @Inject constructor(
       private val localDataSource: AuthLocalDataSource,
       private val remoteDataSource: AuthRemoteDataSource
   ) : AuthRepositoryInterface {
       
       /**
        * Authenticates user with email and password.
        * 
        * @param email User's email address
        * @param password User's password
        * @return Result containing authentication token or error
        */
       suspend fun login(email: String, password: String): Result<AuthToken> {
           // Implementation
       }
   }
   ```

### Testing Strategy

1. **Unit Tests**: Business logic and use cases
2. **Integration Tests**: Repository implementations
3. **UI Tests**: Critical user flows
4. **Test Coverage**: Minimum 80% for business logic

### Performance Considerations

- **Lazy Loading**: Images and data loaded on demand
- **Caching**: Intelligent caching strategies for network and database
- **Memory Management**: Proper lifecycle management
- **Background Processing**: Efficient use of coroutines and WorkManager

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK 28+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RelationShipApp
   ```

2. **Open in Android Studio**
   - Import the project
   - Sync Gradle files
   - Install required SDK components

3. **Configure Environment**
   - Set up API keys in `local.properties`
   - Configure signing keys for release builds

4. **Run the app**
   ```bash
   ./gradlew assembleDebug
   ```

## 📱 Features Roadmap

### Phase 1: Foundation (Current)
- [x] Project setup with Clean Architecture
- [x] Authentication system design
- [x] UI theme and design system
- [ ] User registration and login
- [ ] Profile creation and management

### Phase 2: Core Features
- [ ] Matching algorithm implementation
- [ ] Chat and messaging system
- [ ] Photo and media sharing
- [ ] Push notifications

### Phase 3: Advanced Features
- [ ] Video calling integration
- [ ] Advanced matching preferences
- [ ] Social features and events
- [ ] Premium subscription model

## 🤝 Contributing

### Development Workflow

1. **Feature Branch**: Create feature branches from `main`
2. **Code Review**: All changes require peer review
3. **Testing**: Ensure all tests pass before merging
4. **Documentation**: Update documentation for new features

### Commit Convention
```
feat: add user profile photo upload
fix: resolve authentication token refresh issue
docs: update API documentation
style: format code according to style guide
refactor: extract common UI components
test: add unit tests for matching algorithm
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Lead Developer**: Manish Subah
- **UI/UX Design**: Based on modern Material Design principles
- **Architecture**: Clean Architecture with SOLID principles

---

**Amora** - Building meaningful connections, one relationship at a time. 💕

*Built with ❤️ using Kotlin and Jetpack Compose*
