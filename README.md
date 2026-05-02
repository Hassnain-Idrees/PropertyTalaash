# 🏠 Property Talash

Property Talash is an Android application that helps users search, browse, and list properties. Built with modern Android technologies and Firebase backend, it provides a seamless experience for property buyers, sellers, and renters.

**"Talash"** means "Search" in Urdu/Hindi - reflecting the core purpose of the application.

## ✨ Features

- 🔐 **User Authentication** - Secure login and signup using Firebase Authentication
- 🗺️ **Interactive Maps** - Google Maps integration for property location visualization
- 📍 **Location Services** - GPS-based location tracking and property discovery
- 🏘️ **Property Listings** - Browse and manage property listings in real-time
- ➕ **Add Properties** - Create and list new properties with details and images
- 📸 **Image Gallery** - Property images loaded efficiently with Glide
- 🔄 **Real-time Database** - Firebase Realtime Database for live data synchronization
- ℹ️ **User Information** - About Us, Contact Us, and Terms & Conditions sections

## 🛠️ Technology Stack

- **Language**: Java (Android)
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 36)
- **Compile SDK**: Android 15 (API 36)
- **Build Tool**: Gradle

### Key Dependencies

- **Firebase**
  - Firebase Authentication
  - Firebase Realtime Database
  
- **Google Services**
  - Google Maps
  - Location Services
  - Google ID Sign-In
  
- **UI Libraries**
  - Material Design 3
  - ConstraintLayout
  - AndroidX AppCompat
  
- **Image Loading**
  - Glide

## 📋 Project Structure

```
Property Talash/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/
│   │   │   │   └── com/example/myapplication/
│   │   │   │       ├── SplashActivity.java
│   │   │   │       ├── LoginActivity.java
│   │   │   │       ├── SignUpActivity.java
│   │   │   │       ├── MainActivity.java
│   │   │   │       ├── AddPropertyActivity.java
│   │   │   │       ├── MapPickerActivity.java
│   │   │   │       ├── AboutUsActivity.java
│   │   │   │       ├── ContactUsActivity.java
│   │   │   │       └── TermsActivity.java
│   │   │   └── res/
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── local.properties
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK with API level 24+ installed
- Google Play Services configured
- Firebase project setup

### Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/property-talash.git
   cd property-talash
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory and click "Open"

3. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download the `google-services.json` file
   - Place it in the `app/` directory (already included in project structure)
   - Enable Firebase Authentication and Realtime Database in your Firebase console

4. **Configure Google Maps API**
   - Obtain a Google Maps API Key from [Google Cloud Console](https://console.cloud.google.com/)
   - Update the API key in `AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_API_KEY_HERE" />
     ```

5. **Build and Run**
   - Connect an Android device (API 24+) or use an emulator
   - Click the "Run" button in Android Studio
   - Select your target device/emulator
   - The app will build and launch automatically

## 💻 Activities Overview

| Activity | Purpose |
|----------|---------|
| **SplashActivity** | Launch screen and app initialization |
| **LoginActivity** | User authentication |
| **SignUpActivity** | New user registration |
| **MainActivity** | Home screen and property listings |
| **AddPropertyActivity** | Create new property listings |
| **MapPickerActivity** | Select property location on map |
| **AboutUsActivity** | App information |
| **ContactUsActivity** | Contact information |
| **TermsActivity** | Terms and conditions |

## 📝 Permissions Required

The app requests the following permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## 🔒 Security & Best Practices

- Firebase rules should be properly configured to protect user data
- API keys and sensitive information should be secured
- Always use HTTPS for data transmission
- Implement proper authentication checks

## 📦 Build

To build a debug APK:
```bash
./gradlew assembleDebug
```

To build a release APK:
```bash
./gradlew assembleRelease
```

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## 🔄 Testing Requirements

- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 36)
- **Supported Architectures**: ARM64, ARMv7

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🐛 Bug Reports & Feature Requests

If you find a bug or have a feature request, please open an issue on GitHub with:
- Clear description of the issue
- Steps to reproduce (for bugs)
- Expected behavior
- Screenshots (if applicable)

## 📧 Contact

For questions or support, please reach out through:
- **Email**: support@propertytalash.com
- **GitHub Issues**: [Open an Issue](https://github.com/yourusername/property-talash/issues)

## 🙏 Acknowledgments

- Firebase for backend services
- Google for Maps and Location APIs
- The Android development community

---

**Happy Coding!** 🚀 If you find this project helpful, please consider giving it a ⭐ on GitHub!

