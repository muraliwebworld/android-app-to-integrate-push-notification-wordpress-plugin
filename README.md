# Push Notification for Post and BuddyPress - Android Demo App

**Version**: 2.0 (Modernized)  
**Status**: ✅ Production Ready  
**Last Updated**: 2026-09

---

## 📱 What is This App?

This is a **sample/demo Android application** that demonstrates how to integrate push notifications with the **Push Notification for Post and BuddyPress** WordPress plugin.

It's designed to show plugin users how to build their own Android apps that receive push notifications from their WordPress site.

Download Push notification plugin from WordPress.org repository.

https://wordpress.org/plugins/push-notification-for-post-and-buddypress/<br/><br/>
It sends notification whenever new WordPress post, custom post types,new BuddyPress activities,comments published. It has facility to generate PWA - Progressive Web App. This plugin is able to send push notification to more than 200,000 subscribers unlimited push notifications using background action scheduler.

---

## ✨ Key Features

✅ **Firebase Cloud Messaging (FCM)**
- Automatically retrieves device FCM token
- Securely stores token with encryption

✅ **Token Encryption** 
- AES-256-GCM encryption for security
- HMAC-SHA256 verification for integrity
- Meets server-side requirements

✅ **API Integration**
- Type-safe REST API calls with Retrofit
- Automatic JSON serialization
- Proper error handling and logging

✅ **WebView Integration**
- JavaScript bridge for web-to-native communication
- Group subscription management
- User-specific subscriptions

✅ **Modern Architecture**
- Repository pattern for clean code
- Hilt dependency injection
- Kotlin coroutines for async operations
- Professional logging with Timber

---

## 🎯 Who Should Use This?

| User Type | Use Case |
|-----------|----------|
| **WordPress Plugin Users** | Learn how to build Android apps that integrate with the plugin |
| **Android Developers** | Reference implementation of modern Android patterns |
| **Teams** | Base implementation for custom mobile apps |
| **Learners** | Example of Retrofit, Coroutines, Hilt, and encryption in Android |

---

## 🚀 Quick Start (2 minutes)

### Prerequisites
- Android Studio 2023.3+
- Android SDK 36+
- Java 17 JDK

### Setup
```bash
# 1. Clone and open in Android Studio
cd android-project

# 2. Update your WordPress domain
# Edit: app/src/main/java/com/sample/pnfpbandroid/di/NetworkModule.kt
# Change: return "https://yoursite.com/"

# 3. Get API Secret from WordPress
# WordPress Admin > Settings > Push Notification for Post and BuddyPress
# Copy: "Mobile App Secret Code"

# 4. Build and run
./gradlew build
./gradlew installDebug
```

**Done!** Check logcat for FCM token: `adb logcat | grep "MainActivity"`

---

## 📚 Documentation

| Document | Purpose | Read Time |
|----------|---------|-----------|
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | 📖 Start here! | 5 min |
| [QUICK_START.md](QUICK_START.md) | 🚀 Setup guide | 15 min |
| [DEMO_APP_GUIDE.md](DEMO_APP_GUIDE.md) | 📱 App features & usage | 20 min |
| [TOKEN_ENCRYPTION_GUIDE.md](TOKEN_ENCRYPTION_GUIDE.md) | 🔐 Encryption details | 25 min |
| [VISUAL_REFERENCE_GUIDE.md](VISUAL_REFERENCE_GUIDE.md) | 📊 Flow diagrams | 20 min |
| [MODERNIZATION_GUIDE.md](MODERNIZATION_GUIDE.md) | 🏗️ Architecture | 30 min |
| [BEFORE_AND_AFTER.md](BEFORE_AND_AFTER.md) | 📝 Code comparisons | 20 min |
| [IMPLEMENTATION_CHECKLIST.md](IMPLEMENTATION_CHECKLIST.md) | ✅ Implementation guide | 30 min |
| [CHANGES_SUMMARY.md](CHANGES_SUMMARY.md) | 📊 Modernization summary | 15 min |

**→ Start with [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) to find the right guide for you!**

---

## 🔐 How It Works (30 seconds)

```
1. App gets FCM token from Firebase
   ↓
2. App encrypts token with AES-256-GCM
   ↓
3. App sends encrypted token to WordPress API
   ↓
4. WordPress server verifies and stores token
   ↓
5. User receives push notifications on their Android device
```

**That's it!** The process is fully automated after initial setup.

---

## 📁 Project Structure

```
📦 app/
├── 🎯 src/main/java/com/sample/pnfpbandroid/
│   ├── 🔌 data/          (Data layer - API & models)
│   ├── 🎮 domain/        (Business logic interface)
│   ├── 🛠️  util/         (Encryption & helpers)
│   ├── 💉 di/            (Dependency injection)
│   ├── 🎨 *.kt           (UI & Application classes)
│
├── 📄 build.gradle       (Dependencies & SDK version)
├── 📄 AndroidManifest.xml (App configuration)
└── 📦 res/               (Resources, layouts, strings)

📚 Documentation/
├── DOCUMENTATION_INDEX.md  ← Start here!
├── QUICK_START.md
├── TOKEN_ENCRYPTION_GUIDE.md
├── DEMO_APP_GUIDE.md
├── VISUAL_REFERENCE_GUIDE.md
├── MODERNIZATION_GUIDE.md
├── BEFORE_AND_AFTER.md
├── IMPLEMENTATION_CHECKLIST.md
└── CHANGES_SUMMARY.md
```

---

## 🧪 Testing

### Build
```bash
./gradlew build
```

### Unit Tests
```bash
./gradlew test
```

### Install on Device
```bash
./gradlew installDebug
```

### View Logs
```bash
adb logcat | grep "SubscriptionRepo\|OkHttp\|MainActivity"
```

---

## 🔑 Configuration

### API Secret
Get from WordPress:
1. Login to WordPress Admin
2. Go: Settings → Push Notification for Post and BuddyPress
3. Find: "Mobile App Secret Code"
4. Copy value

### Base URL
Update in `app/src/main/java/com/sample/pnfpbandroid/di/NetworkModule.kt`:
```kotlin
return "https://yoursite.com/"
```

### Firebase
Already configured in `google-services.json`. No additional setup needed.

---

## 🏗️ Architecture Highlights

### Modern Stack
- 🎯 **Retrofit** - Type-safe API calls
- 🔄 **Coroutines** - Async/await operations
- 💉 **Hilt** - Dependency injection
- 📝 **Timber** - Professional logging
- 🗃️ **EncryptedSharedPreferences** - Secure storage

### Design Patterns
- 📦 Repository Pattern (data layer separation)
- 🔌 Dependency Injection (loose coupling)
- 🎮 Domain Layer Interface (abstraction)
- 📊 Data Models (type safety)

### Security
- 🔐 AES-256-GCM encryption
- 🔒 EncryptedSharedPreferences for secrets
- ✅ HMAC-SHA256 verification
- 🚫 No hardcoded credentials

---

## 📊 API Endpoint

**URL**: `/wp-json/PNFPBpush/v1/subscriptiontoken`  
**Method**: `POST`  
**Authentication**: Encryption via AES-256-GCM

**Request Example**:
```json
{
  "token": "encrypted_token:base64_iv:hex_hmac:hex_hmac",
  "userid": 0,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**Response**:
```json
{
  "status": 200,
  "message": "Subscription successful"
}
```

---

## 🎓 Code Examples

### Subscribe to Notifications (Kotlin)
```kotlin
lifecycleScope.launch {
    val encryptedToken = subscriptionRepository.encryptToken(fcmToken, apiSecret)
    val request = SubscriptionTokenRequest(
        encryptedToken = encryptedToken,
        userId = 0,
        groupId = "",
        subscriptionType = ""
    )
    
    subscriptionRepository.sendSubscriptionToken(request).collect { result ->
        result.onSuccess { message ->
            Log.i("Success", "Subscription updated: $message")
        }
        result.onFailure { error ->
            Log.e("Error", "Subscription failed: ${error.message}")
        }
    }
}
```

### From WebView JavaScript
```javascript
// Get token from Android
const token = Android.getFromAndroid();

// Subscribe to group
Android.postMessage("5", "subscribe-group");

// Unsubscribe from group
Android.postMessage("5", "unsubscribe-group");
```

---

## 📈 Benefits of This Implementation

| Aspect | Benefit |
|--------|---------|
| **Performance** | Connection pooling, lightweight coroutines |
| **Security** | AES-256-GCM encryption, secure storage |
| **Maintainability** | Clean architecture, testable code |
| **Reliability** | Type-safe API, proper error handling |
| **Scalability** | Repository pattern allows easy changes |
| **Developer Experience** | Coroutines, Kotlin, professional logging |

---

## 🐛 Troubleshooting

### "API returns 401"
→ Check API secret matches WordPress setting

### "Encryption failed"
→ Verify API secret is correct and token is not empty

### "WebView bridge not working"
→ Enable JavaScript: `webView.settings.javaScriptEnabled = true`

### "Notifications not received"
→ Verify user has notification permission, check server logs

**More troubleshooting**: See [QUICK_START.md](QUICK_START.md) (Troubleshooting section)

---

## 📊 Modernization Changes

✅ Updated SDK: Android 33 → 36 
✅ Updated Java: 11 → 17  
✅ Added: Retrofit, Coroutines, Hilt, Timber  
✅ Pattern: Repository Pattern, Clean Architecture  
✅ Language: Java → Kotlin  
✅ Async: Callbacks → Coroutines  
✅ Logging: Log.d → Timber  

**See [CHANGES_SUMMARY.md](CHANGES_SUMMARY.md) for complete details**

---

## 🎯 Next Steps

### For Users
1. ✅ Read [QUICK_START.md](QUICK_START.md)
2. ✅ Configure API secret and domain
3. ✅ Test on your device
4. ✅ Reference [TOKEN_ENCRYPTION_GUIDE.md](TOKEN_ENCRYPTION_GUIDE.md) for details

### For Developers
1. ✅ Read [MODERNIZATION_GUIDE.md](MODERNIZATION_GUIDE.md)
2. ✅ Review [BEFORE_AND_AFTER.md](BEFORE_AND_AFTER.md)
3. ✅ Study code in `src/main/java`
4. ✅ Write tests in `src/test` and `src/androidTest`

### For Teams
1. ✅ Review [IMPLEMENTATION_CHECKLIST.md](IMPLEMENTATION_CHECKLIST.md)
2. ✅ Assign tasks based on checklist
3. ✅ Use [VISUAL_REFERENCE_GUIDE.md](VISUAL_REFERENCE_GUIDE.md) for team training
4. ✅ Deploy following [QUICK_START.md](QUICK_START.md) (Deployment section)

---

## 📞 Support & Resources

### Documentation
- 📖 [Complete Documentation Index](DOCUMENTATION_INDEX.md)
- 🚀 [Quick Start Guide](QUICK_START.md)
- 🔐 [Token Encryption Details](TOKEN_ENCRYPTION_GUIDE.md)
- 📊 [Visual Reference Diagrams](VISUAL_REFERENCE_GUIDE.md)

### External Resources
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)

### WordPress Plugin
- [Push Notification for Post and BuddyPress](https://wordpress.org/plugins/push-notification-for-post-and-buddypress/)

---

## 📋 Requirements

### System Requirements
- Android Studio 2023.3 or newer
- Android SDK 34+
- Java 17 JDK
- Kotlin 1.9+

### Dependencies
- Firebase Cloud Messaging 33.1.0
- Retrofit 2.11.0
- OkHttp 4.12.0
- Hilt 2.51.1
- Coroutines 1.8.0
- Timber 5.1.0

---

## 📈 Versions

| Version | Date | Changes |
|---------|------|---------|
| 2.0 | 2026-09 | Modernized: Retrofit, Coroutines, Hilt, Timber |
| 1.2 | 2024 | Original OkHttp implementation |
| 1.0 | 2022 | Initial release |

---

## 📝 License

This demo app is provided as-is for educational purposes. 

---

## 🎉 Ready to Start?

### 1. First-Time Users
→ Read: [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) (5 min)

### 2. Setup
→ Read: [QUICK_START.md](QUICK_START.md) (15 min)

### 3. Deep Dive
→ Read: [TOKEN_ENCRYPTION_GUIDE.md](TOKEN_ENCRYPTION_GUIDE.md) (25 min)

### 4. Reference
→ Use: [VISUAL_REFERENCE_GUIDE.md](VISUAL_REFERENCE_GUIDE.md)

---

## 🙋 Common Questions

**Q: Can I use this code in production?**  
A: Yes! This is production-ready with modern best practices.

**Q: Do I need to modify the code?**  
A: Yes, update the domain URL and API secret for your site.

**Q: How do I get the API secret?**  
A: WordPress Admin → Settings → Push Notification Plugin

**Q: Does it work offline?**  
A: No, it requires internet connection to send notifications.

**Q: Can I modify the encryption?**  
A: No, it must match the server-side algorithm (AES-256-GCM).

**More Q&A**: See [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)

---

## 📚 Documentation Map

```
START HERE
    ↓
DOCUMENTATION_INDEX.md (this is the complete guide map)
    ↓
├─→ QUICK_START.md (getting started)
│   └─→ IMPLEMENTATION_CHECKLIST.md (step-by-step)
│
├─→ DEMO_APP_GUIDE.md (how this app works)
│   └─→ TOKEN_ENCRYPTION_GUIDE.md (encryption details)
│       └─→ VISUAL_REFERENCE_GUIDE.md (diagrams)
│
└─→ MODERNIZATION_GUIDE.md (architecture)
    └─→ BEFORE_AND_AFTER.md (code improvements)
        └─→ CHANGES_SUMMARY.md (summary)
```

---

**🚀 Let's get started!**

**Next Step**: Open [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) or [QUICK_START.md](QUICK_START.md)

---

Version 2.0 | Modernized | Production Ready ✅
