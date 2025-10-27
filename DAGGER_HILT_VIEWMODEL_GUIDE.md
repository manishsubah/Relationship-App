# Dagger Hilt & ViewModel Architecture Guide

## 📚 Table of Contents
1. [What is Dagger Hilt?](#what-is-dagger-hilt)
2. [What is ViewModel?](#what-is-viewmodel)
3. [Why Use Dagger Hilt?](#why-use-dagger-hilt)
4. [Why Use ViewModel?](#why-use-viewmodel)
5. [How They Work Together](#how-they-work-together)
6. [Implementation in Amora App](#implementation-in-amora-app)
7. [Benefits & Best Practices](#benefits--best-practices)

---

## 🎯 What is Dagger Hilt?

**Dagger Hilt** is a dependency injection framework for Android that simplifies the process of providing dependencies to your classes. 
It's built on top of Dagger 2 and provides a more Android-friendly API.

### Key Concepts:

#### 1. **Dependency Injection (DI)**
- **Definition**: A design pattern where objects receive their dependencies from external sources rather than creating them internally
- **Purpose**: Reduces coupling between classes and makes code more testable and maintainable

#### 2. **Annotations**
- `@HiltAndroidApp`: Marks the Application class
- `@AndroidEntryPoint`: Marks Activities/Fragments for injection
- `@Inject`: Marks constructor or field for injection
- `@Singleton`: Ensures only one instance exists
- `@HiltViewModel`: Marks ViewModels for injection

#### 3. **Scopes**
- **Application Scope**: Lives for the entire app lifecycle
- **Activity Scope**: Lives for the activity lifecycle
- **ViewModel Scope**: Lives for the ViewModel lifecycle

---

## 🧠 What is ViewModel?

**ViewModel** is an Android Architecture Component that manages UI-related data in a lifecycle-conscious way. 
It survives configuration changes and provides data to the UI.

### Key Concepts:

#### 1. **Lifecycle Awareness**
- Survives screen rotations and configuration changes
- Automatically cleared when the associated Activity/Fragment is destroyed
- Provides data to UI components

#### 2. **State Management**
- Holds UI state using `StateFlow` or `LiveData`
- Provides reactive data streams to the UI
- Manages business logic separate from UI

#### 3. **Separation of Concerns**
- UI logic stays in Composables/Activities
- Business logic stays in ViewModels
- Data operations stay in Repositories

---

## 🔍 Why Use Dagger Hilt?

### 1. **Dependency Management**
```kotlin
// Without Hilt (Manual Dependency Management)
class SignupViewModel {
    private val sessionManager: SessionManager
    private val apiService: ApiService
    
    constructor() {
        sessionManager = SessionManager(context) // Manual creation
        apiService = ApiService() // Manual creation
    }
}

// With Hilt (Automatic Dependency Injection)
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) {
    // Dependencies automatically provided
}
```

### 2. **Testability**
```kotlin
// Easy to mock dependencies for testing
@Test
fun testSignup() {
    val mockSessionManager = mockk<SessionManager>()
    val mockApiService = mockk<ApiService>()
    
    val viewModel = SignupViewModel(mockSessionManager, mockApiService)
    // Test with mocked dependencies
}
```

### 3. **Singleton Management**
```kotlin
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Only one instance across the entire app
    // Automatically managed by Hilt
}
```

### 4. **Configuration Changes**
- Dependencies survive configuration changes
- No need to recreate expensive objects
- Automatic lifecycle management

---

## 🎯 Why Use ViewModel?

### 1. **Configuration Change Survival**
```kotlin
// Without ViewModel - Data lost on rotation
class MainActivity : ComponentActivity() {
    private var userEmail: String = "" // Lost on rotation!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Data needs to be restored from savedInstanceState
    }
}

// With ViewModel - Data survives rotation
@HiltViewModel
class SignupViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()
    
    // Data automatically survives configuration changes
}
```

### 2. **Reactive UI Updates**
```kotlin
// UI automatically updates when ViewModel state changes
@Composable
fun SignupScreen(viewModel: SignupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // UI automatically recomposes when uiState changes
    if (uiState.isLoading) {
        CircularProgressIndicator()
    }
}
```

### 3. **Business Logic Separation**
```kotlin
// UI focuses only on display
@Composable
fun SignupScreen() {
    Button(onClick = { viewModel.signup(email, password, otp) }) {
        Text("Sign Up")
    }
}

// ViewModel handles business logic
class SignupViewModel {
    fun signup(email: String, password: String, otp: String) {
        // Validation logic
        // API calls
        // State management
        // Error handling
    }
}
```

---

## 🤝 How They Work Together

### 1. **Dependency Injection Flow**
```
Application (@HiltAndroidApp)
    ↓
SessionManager (@Singleton) ← Created once
    ↓
SignupViewModel (@HiltViewModel) ← Gets SessionManager injected
    ↓
SignupScreen (@Composable) ← Gets ViewModel injected
```

### 2. **Data Flow**
```
User Input → SignupScreen → SignupViewModel → SessionManager → SharedPreferences
                ↑                                              ↓
            UI Updates ← StateFlow ← Business Logic ← Data Storage
```

### 3. **Lifecycle Management**
```
App Start → Hilt creates singletons → ViewModel created → UI observes state
    ↓
Configuration Change → ViewModel survives → UI recreates → State preserved
    ↓
App Destroy → ViewModel cleared → Singletons remain → Clean shutdown
```

---

## 🏗️ Implementation in Amora App

### 1. **Application Setup**
```kotlin
@HiltAndroidApp
class AmoraApplication : Application()
```
- Triggers Hilt code generation
- Sets up dependency injection graph

### 2. **Session Management**
```kotlin
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Manages user session data
    // Survives app restarts
    // Shared across all screens
}
```

### 3. **Signup Logic**
```kotlin
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    // Handles signup business logic
    // Manages UI state
    // Survives configuration changes
}
```

### 4. **UI Integration**
```kotlin
@Composable
fun SignupScreen(viewModel: SignupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Reactive UI that updates automatically
    // Clean separation of concerns
}
```

---

## ✅ Benefits & Best Practices

### **Benefits:**

1. **Maintainability**
   - Clear separation of concerns
   - Easy to modify and extend
   - Reduced code duplication

2. **Testability**
   - Easy to mock dependencies
   - Unit tests for business logic
   - Integration tests for UI

3. **Performance**
   - Singleton objects created once
   - ViewModels survive configuration changes
   - Efficient memory management

4. **Scalability**
   - Easy to add new features
   - Consistent architecture patterns
   - Team collaboration friendly

### **Best Practices:**

1. **Use Scopes Appropriately**
   ```kotlin
   @Singleton // For app-wide services
   @ViewModelScoped // For ViewModel-specific data
   @ActivityScoped // For activity-specific data
   ```

2. **Keep ViewModels Light**
   ```kotlin
   // Good: ViewModel focuses on state management
   class SignupViewModel {
       fun signup() { /* business logic */ }
   }
   
   // Bad: ViewModel doing too much
   class SignupViewModel {
       fun signup() { /* business logic */ }
       fun sendEmail() { /* should be in service */ }
       fun uploadImage() { /* should be in repository */ }
   }
   ```

3. **Use StateFlow for Reactive UI**
   ```kotlin
   // Reactive state management
   private val _uiState = MutableStateFlow(SignupUiState())
   val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()
   ```

4. **Handle Errors Gracefully**
   ```kotlin
   // Proper error handling
   try {
       val result = apiCall()
       _uiState.value = _uiState.value.copy(isSuccess = true)
   } catch (e: Exception) {
       _uiState.value = _uiState.value.copy(error = e.message)
   }
   ```

---

## 🎯 Summary

**Dagger Hilt** and **ViewModel** work together to create a robust, maintainable, and testable Android architecture:

- **Hilt** manages dependencies automatically, reducing boilerplate and improving testability
- **ViewModel** manages UI state and business logic, surviving configuration changes
- Together, they provide a clean separation of concerns and make the codebase scalable

In the Amora app, this architecture ensures:
- User sessions persist across app restarts
- Signup forms survive screen rotations
- Business logic is testable and maintainable
- Dependencies are managed automatically
- Code follows SOLID principles

This foundation makes it easy to add new features, write tests, and maintain the codebase as the app grows! 💕
