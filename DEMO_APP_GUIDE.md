# Demo App Integration Guide
## How to Use This Sample Android App

This Android application demonstrates how to integrate with the **Push Notification for Post and BuddyPress** WordPress plugin.

---

## 📱 App Overview

This is a **sample/demo application** that shows:
- ✅ How to get FCM push notification tokens
- ✅ How to encrypt tokens using AES-256-GCM
- ✅ How to send encrypted tokens to WordPress API
- ✅ How to handle group subscriptions
- ✅ How to use WebView JavaScript bridge for frontend integration

**Target Users**: Plugin users wanting to integrate with Android mobile apps

---

## 🎯 Key Features

### 1. Firebase Cloud Messaging (FCM)
- Automatically retrieves FCM token
- Stores token securely in EncryptedSharedPreferences
- Handles notification permissions (Android 13+)

### 2. Token Encryption
- Encrypts token with AES-256-GCM algorithm
- Generates random IV for each encryption
- Calculates HMAC-SHA256 for integrity verification
- Matches server-side decryption expectations

### 3. API Integration
- Type-safe API calls using Retrofit
- Automatic JSON serialization/deserialization
- Proper error handling and logging
- Connection pooling and retry logic

### 4. WebView Integration
- JavaScript bridge for web-to-native communication
- Seamless subscription management from web pages
- Support for group subscriptions
- User-specific subscriptions

---

## 🔧 Configuration Steps

### Step 1: Update WordPress Domain

**File**: `app/src/main/java/com/sample/pnfpbandroid/di/NetworkModule.kt`

Find this line:
```kotlin
@Provides
@Singleton
fun provideBaseUrl(): String {
    return "https://www.muraliwebworld.com/"  // ← Update this
}
```

Replace with your WordPress domain:
```kotlin
@Provides
@Singleton
fun provideBaseUrl(): String {
    return "https://yoursite.com/"
}
```

### Step 2: Get API Secret from WordPress

1. Login to your WordPress admin
2. Navigate to: **Settings → Push Notification for Post and BuddyPress**
3. Find the field: **"Mobile App Secret Code"**
4. Copy the value (e.g., `abc123def456ghi789`)

### Step 3: Store API Secret

The app stores the secret in encrypted storage. You have two options:

**Option A: Set at Runtime** (Recommended for demos)
```kotlin
val encryptedDataHolder = EncryptedDataHolder(context)
encryptedDataHolder.setApiKey("your-api-secret-from-wordpress")
```

**Option B: Add to BuildConfig** (For production)
```gradle
buildTypes {
    debug {
        buildConfigField "String", "API_SECRET", '"your-debug-secret"'
    }
    release {
        buildConfigField "String", "API_SECRET", '"your-production-secret"'
    }
}
```

### Step 4: Build and Run

```bash
cd android-project
./gradlew build
./gradlew installDebug
```

---

## 📋 App Architecture

### Modern Structure (Implemented)

```
App Layer
  ├── MainActivity (or MainActivityModern)
  │   └── Manages UI and lifecycle
  │
  ├── WebView Integration
  │   └── JavaScriptInterfaceModern
  │       └── Handles JS ↔ Android communication
  │
  └── Firebase Integration
      └── Receives FCM tokens

Domain Layer
  └── SubscriptionRepository (Interface)
      └── Defines business logic contracts

Data Layer
  ├── SubscriptionRepositoryImpl
  │   └── Implements business logic
  ├── PushNotificationApiService (Retrofit)
  │   └── API communication
  └── Models
      ├── SubscriptionTokenRequest
      └── SubscriptionTokenResponse

Utilities
  ├── TokenEncryptionUtil
  │   └── AES-256-GCM encryption
  └── EncryptedDataHolder
      └── Secure storage (EncryptedSharedPreferences)

Infrastructure
  └── Hilt DI Container
      └── Manages dependencies
```

---

## 🚀 Usage Scenarios

### Scenario 1: Generic Push Notification Subscription

**Use Case**: User wants to receive all push notifications

**Code**:
```kotlin
lifecycleScope.launch {
    val encryptedToken = subscriptionRepository.encryptToken(fcmToken, apiSecret)
    val request = SubscriptionTokenRequest(
        encryptedToken = encryptedToken,
        userId = 0,
        groupId = "",
        subscriptionType = "",
        subscriptionOptions = ""
    )
    
    subscriptionRepository.sendSubscriptionToken(request).collect { result ->
        result.onSuccess { message ->
            Toast.makeText(this@MainActivity, "Subscribed!", Toast.LENGTH_SHORT).show()
        }
    }
}
```

**Expected Response**:
```json
{
  "status": 200,
  "message": "Subscription successful"
}
```

### Scenario 2: Subscribe to Specific Group/Category

**Use Case**: User wants to receive notifications only for a specific category

**Code**:
```kotlin
lifecycleScope.launch {
    val encryptedToken = subscriptionRepository.encryptToken(fcmToken, apiSecret)
    val request = SubscriptionTokenRequest(
        encryptedToken = encryptedToken,
        userId = 0,
        groupId = "5",  // Category ID
        subscriptionType = "subscribe-group"
    )
    
    subscriptionRepository.sendSubscriptionToken(request).collect { result ->
        result.onSuccess { message ->
            Toast.makeText(this@MainActivity, "Subscribed to category!", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### Scenario 3: User-Specific Subscription

**Use Case**: Logged-in user subscription

**Code**:
```kotlin
lifecycleScope.launch {
    val encryptedToken = subscriptionRepository.encryptToken(fcmToken, apiSecret)
    val request = SubscriptionTokenRequest(
        encryptedToken = encryptedToken,
        userId = 42,  // WordPress user ID
        groupId = "",
        subscriptionType = ""
    )
    
    subscriptionRepository.sendSubscriptionToken(request).collect { result ->
        result.onSuccess { message ->
            Toast.makeText(this@MainActivity, "User subscription updated!", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### Scenario 4: From WebView JavaScript

**HTML/JavaScript**:
```html
<button onclick="subscribeToNotifications()">Subscribe</button>

<script>
function subscribeToNotifications() {
    // Get token from Android
    const token = Android.getFromAndroid();
    console.log("FCM Token:", token);
    
    // Subscribe to group
    Android.postMessage("5", "subscribe-group");
    
    // Or just generic subscription
    Android.postMessage("", "");
}

function unsubscribeFromNotifications() {
    const token = Android.getFromAndroid();
    Android.postMessage("5", "unsubscribe-group");
}
</script>
```

---

## 🔐 Token Encryption Details

### The Encryption Process

1. **Generate Random IV** (16 bytes)
   - Each encryption gets a fresh random IV
   - Ensures same token encrypts differently each time

2. **Encrypt FCM Token** (AES-256-GCM)
   - Uses API secret as encryption key
   - Produces ciphertext with authentication tag

3. **Encode Results**
   - Base64 encode ciphertext
   - Base64 encode IV
   - Hex encode HMAC

4. **Combine Parts**
   - Format: `encryptedToken:base64(iv):hex(hmac):hex(hmac)`
   - Send to API in the "token" parameter

### Example

**Input**:
- FCM Token: `dHlXa4...` (actual Firebase token)
- API Secret: `my-secret-key-123`

**Output**:
```
abc123def456ghi789jkl012mno345:
pqr678stu901vwx234yz:
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6:
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**Full Request**:
```json
{
  "token": "abc123def456ghi789jkl012mno345:pqr678stu901vwx234yz:a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6:a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "userid": 0,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

---

## 🧪 Testing Checklist

### Unit Tests
- [ ] Token encryption/decryption works
- [ ] HMAC calculation is correct
- [ ] API request format is valid

### Integration Tests
- [ ] FCM token is retrieved
- [ ] Token is stored securely
- [ ] API call succeeds
- [ ] Error responses handled correctly

### Manual Testing
- [ ] App launches without crashes
- [ ] FCM token is displayed in logs
- [ ] API returns 200 status
- [ ] WebView JavaScript bridge works
- [ ] Group subscription works
- [ ] User subscription works
- [ ] Push notifications are received

---

## 🐛 Common Issues & Solutions

### Issue: "API Secret not found"
**Solution**: 
1. Copy API secret from WordPress settings
2. Call `encryptedDataHolder.setApiKey(secret)` early in app lifecycle
3. Check EncryptedSharedPreferences has write permission

### Issue: "Encryption failed"
**Solution**:
1. Verify API secret is correct
2. Check FCM token is not empty
3. Ensure token is UTF-8 encoded

### Issue: "API returns 401"
**Solution**:
1. Verify HMAC calculation matches server
2. Check encryption algorithm is AES-256-GCM
3. Verify IV is 16 bytes
4. Check all parameters are sent

### Issue: "WebView bridge not working"
**Solution**:
1. Enable JavaScript: `webView.settings.javaScriptEnabled = true`
2. Verify `@JavascriptInterface` annotation present
3. Check method signatures match JavaScript calls
4. Verify Activity is LifecycleOwner

### Issue: "Notifications not received"
**Solution**:
1. Verify FCM token is stored correctly
2. Check WordPress API secret matches mobile app
3. Verify user has permission to receive notifications
4. Check Firebase Cloud Messaging service is enabled
5. Review WordPress plugin logs

---

## 📚 Implementation Steps for Users

### For Plugin Users Wanting Android Integration

1. **Read Documentation**
   - Review MODERNIZATION_GUIDE.md
   - Review TOKEN_ENCRYPTION_GUIDE.md

2. **Get WordPress API Secret**
   - Login to WordPress Admin
   - Settings → Push Notification Plugin
   - Copy "Mobile App Secret Code"

3. **Modify App Configuration**
   - Update domain in NetworkModule.kt
   - Set API secret in MainActivity

4. **Test Locally**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   adb logcat | grep "SubscriptionRepo\|OkHttp"
   ```

5. **Deploy to Device**
   ```bash
   ./gradlew installDebug
   # Open app on device
   # Check FCM token in Logcat
   # Verify API calls
   ```

6. **Monitor Push Notifications**
   - Send test notification from WordPress
   - Verify notification received on device
   - Check user can interact with notification

---

## 📖 Code Walkthrough

### Main Activity Setup
```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var subscriptionRepository: SubscriptionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize encrypted storage
        encryptedDataHolder = EncryptedDataHolder(this)
        
        // Setup WebView
        setupWebView()
        
        // Get FCM token
        subscribeFCM()
    }
    
    private fun setupWebView() {
        val jsInterface = JavaScriptInterfaceModern(
            this,
            this,
            encryptedDataHolder,
            subscriptionRepository
        )
        webView.addJavascriptInterface(jsInterface, "Android")
        webView.loadUrl("https://yoursite.com")
    }
    
    private fun subscribeFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                encryptedDataHolder.setApiKey(token)
                // Now ready to send to API
            }
        }
    }
}
```

### Sending Subscription Token
```kotlin
lifecycleScope.launch {
    try {
        // Step 1: Get stored secret
        val apiSecret = encryptedDataHolder.getApiKey()
        
        // Step 2: Encrypt token
        val encryptedToken = subscriptionRepository.encryptToken(fcmToken, apiSecret)
        
        // Step 3: Build request
        val request = SubscriptionTokenRequest(
            encryptedToken = encryptedToken,
            userId = 0,
            groupId = "",
            subscriptionType = ""
        )
        
        // Step 4: Send to API
        subscriptionRepository.sendSubscriptionToken(request).collect { result ->
            result.onSuccess { message ->
                Log.i("Success", "Token sent: $message")
            }
            result.onFailure { error ->
                Log.e("Error", "Failed: ${error.message}", error)
            }
        }
    } catch (e: Exception) {
        Log.e("Exception", "Error: ${e.message}", e)
    }
}
```

---

## 🎓 Learning Resources

### For Understanding Encryption
- [AES-GCM Encryption](https://en.wikipedia.org/wiki/Galois/Counter_Mode)
- [HMAC-SHA256](https://en.wikipedia.org/wiki/HMAC)
- [Base64 Encoding](https://en.wikipedia.org/wiki/Base64)

### For Android Development
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)

### For WordPress Plugin
- See: push-notification-for-post-and-buddypress/README.md
- Plugin File: pnfpb_mobile_app_notification_api_routine.php

---

## 📞 Support & Contribution

For issues or questions:
1. Check troubleshooting section above
2. Review Logcat output
3. Verify API secret is correct
4. Check network connectivity
5. Review WordPress plugin logs

For improvements or contributions:
1. Create an issue with details
2. Submit a pull request with changes
3. Include test cases
4. Update documentation

---

## 🎉 Conclusion

This demo app showcases modern Android best practices for integrating with the Push Notification for Post and BuddyPress WordPress plugin. Users can use this as a reference or starting point for their own Android push notification applications.

**Key Takeaways**:
- ✅ Use AES-256-GCM for encryption
- ✅ Store API secret securely
- ✅ Handle permissions properly
- ✅ Use modern async patterns (Coroutines)
- ✅ Implement proper error handling
- ✅ Log for debugging

---

**Version**: 2.0 (Modernized)  
**Last Updated**: 2026-09  
**Status**: Production Ready
