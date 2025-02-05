# Secure Weather App

A secure Android weather application built with modern Android development practices and strong security measures.

## Features

- User authentication (Login/Registration)
- Current weather information
- Weather history
- Location-based weather updates
- Encrypted data storage


## Tech Stack

### Core Technologies
- Kotlin
- Jetpack Compose
- MVVM Architecture
- Coroutines & Flow
- Hilt (Dependency Injection)

### Storage & Database
- Room Database
- EncryptedSharedPreferences

### Networking & API
- Retrofit
- OkHttp
- OpenWeatherMap API

### Security
- AndroidX Security Crypto
- Certificate Pinning
- PBKDF2 Password Hashing

1. Clone the repository
```bash
git clone https://github.com/yourusername/SecureWeatherApp.git
```

2. Create/modify `local.properties` in project root:
```properties
sdk.dir=/Users/YourUsername/Library/Android/sdk

# API Keys
DEBUG_API_KEY=your_debug_openweathermap_api_key
RELEASE_API_KEY=your_release_openweathermap_api_key

# Signing Config
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

## Generate Release Keys

1. Create keystore:
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias your_key_alias
```

2. Move keystore to app folder:
```bash
mv release-keystore.jks app/
```

## OpenWeatherMap API Setup

1. Register at [OpenWeatherMap](https://openweathermap.org/api)
2. Create API keys for debug and release builds
3. Add keys to `local.properties`

## Build Variants

- Debug: Development version with logging
- Release: Production version with ProGuard enabled

## Running the App

Debug:
```bash
./gradlew assembleDebug
```

Release:
```bash
./gradlew assembleRelease
```

### Certificate Pinning
Certificate pins are configured in AppModule.kt:
```kotlin
.add(BASE_URL,
    "sha256/primary-cert-hash",
    "sha256/backup-cert-hash")
```

## Security Features

### Authentication
- Secure password storage using PBKDF2 with SHA256
- Session management with token expiration
- Device security checks (root detection, developer options (disabled for testing))

### Data Protection
- Encrypted SharedPreferences for sensitive data
- Room database security

### Device Security
- Root detection
- Developer options detection (disabled for testing)

### Network Security
- Certificate pinning
- HTTPS-only communication
- API key protection
- Request/Response encryption

### Build Security
- API keys are not committed to version control
- Release signing configuration is secured via uncommitted local.properties
- Different API keys for debug/release builds

## Architecture

### MVVM Components
- **View**: Jetpack Compose UI
- **ViewModel**: AuthViewModel, WeatherViewModel
- **Model**: Repository pattern with Room and API integration

### Data Flow
1. UI Layer (Compose)
2. ViewModels
3. Repositories
4. Data Sources (Room/API)

### Security Layer
```
┌─────────────────┐
│   UI Layer      │
├─────────────────┤
│   Auth Manager  │
├─────────────────┤
│ Security Utils  │
└─────────────────┘
```

## Building and Testing

### Requirements
- Android Studio Electric Eel or newer
- Android SDK 33+
- Gradle 8.0+

### Build Types
- Debug: Development build with logging
- Release: Proguard enabled, logging disabled

## Best Practices

### Security
- No hardcoded credentials
- Secure key storage
- Strong password policies
- Protected API communication

### Performance
- Coroutines for async operations
- Efficient data caching
- Memory leak prevention

### Experimental Security
- Google Credential Manager (removed implementation because it's not testable on emulators due to no playstore services)
