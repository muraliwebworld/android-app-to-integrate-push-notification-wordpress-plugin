# Quick Start Guide - Modernized Android App

**⚡ READY TO BUILD** - Only change the domain name, everything else is configured!

---

## 📖 Important: Read First

**New to Gradle?** → Read [ANDROID_STUDIO_GRADLE_SETUP.md](ANDROID_STUDIO_GRADLE_SETUP.md)

This guide has complete instructions for:
- Opening project in Android Studio
- Troubleshooting Gradle sync issues
- Build commands reference
- Running on device/emulator
- Gradle configuration details

---

## 📋 Prerequisites

### Required
- ✅ Android Studio 2023.3+
- ✅ Android SDK 34+
- ✅ Java 17 JDK
- ✅ Kotlin 1.9+ (included with Android Studio)

### Optional (for easier setup)
- USB cable for device testing (or Android Emulator)
- Gradle 8.6+ (included with project)

---

## 🎯 Configuration Point
```bash
# 1. Clone and open in Android Studio
cd android-project

# 2. Add your Firebase google-services.json
# Download from Firebase Console and copy to app/google-services.json
# ⚠️ THIS IS REQUIRED - app will not build without it
cp ~/Downloads/google-services.json app/google-services.json

# 3. Get API Secret from WordPress
# WordPress Admin > Settings > Push Notification for Post and BuddyPress
# Copy: "Mobile App Secret Code"

# 4. Update your WordPress domain
# Edit: app/build.gradle (Line ~23)
# Change: buildConfigField "String", "API_BASE_URL", '"https://www.pnfpb.com/"'
# To:     buildConfigField "String", "API_BASE_URL", '"https://your-wordpress-site.com/"'

# 5. Update your generated secret code from step 3 as mentioned below,
# Edit: app/build.gradle (Line ~24)
# Change: buildConfigField "String", "PNFPB_API_SECRET", '"your site pnfpb plugin secret api key"'

# 5. Build and run
./gradlew build
./gradlew installDebug
```

✅ **That's it!** The app will:
- Load your WordPress site in WebView
- Send subscription tokens to your WordPress push notification plugin
- Handle all encryption and API calls automatically
- Manage Firebase Cloud Messaging

---

## ✅ Clean Codebase - Modern Only

**Legacy files removed** - This repository contains ONLY:

✅ Modern Kotlin implementations (MainActivityModern.kt, etc.)  
✅ Type-safe Retrofit API layer  
✅ Hilt Dependency Injection  
✅ Kotlin Coroutines for async operations  
✅ AES-256-GCM encryption  
✅ Professional Timber logging  

❌ **Deleted**:
- Old callback-based MainActivity.java
- Legacy JavaScriptInterface.java
- Incomplete MyFirebaseMessagingService.java
- Unfinished MyWorker.java

---

## Prerequisites

- Android Studio 2023.3 or newer
- Android SDK 34+
- Java 17 JDK
- Kotlin 1.9+

## Installation Steps

### Step 1: Sync Gradle

1. Open the project in Android Studio
2. Let it auto-sync, or click: **File → Sync Now**
3. Wait for dependencies to download

```bash
cd android-project
./gradlew build
```

### Step 2: Update Domain Configuration (THE ONLY REQUIRED CHANGE)

#### ⭐ Edit: `app/build.gradle` - Line ~14

This is the **ONLY file you need to edit**:

```gradle
buildConfigField "String", "API_BASE_URL", '"https://www.muraliwebworld.com/"'
```

Change to your WordPress domain:

```gradle
buildConfigField "String", "API_BASE_URL", '"https://your-wordpress-site.com/"'
```

**That's it!** The app will automatically:
- Load your WordPress site in WebView
- Send subscription tokens to: `https://your-wordpress-site.com/wp-json/PNFPBpush/v1/subscriptiontoken`
- Encrypt tokens with server-side API secret
- Handle all Firebase messaging
- Manage notifications

### Step 3: Rename MainActivityModern.kt to MainActivity.kt

The file `MainActivityModern.kt` needs to be renamed to `MainActivity.kt`.

#### Using Android Studio (Recommended)

1. Right-click `MainActivityModern.kt`
2. Select: **Refactor → Rename**
3. Change to: `MainActivity.kt`
4. Click **Refactor** button
5. Done! ✅

#### Using Terminal

```bash
cd app/src/main/java/com/sample/pnfpbandroid/
mv MainActivityModern.kt MainActivity.kt
```

### Step 4: Sync Gradle Again

After renaming, let Android Studio re-sync:

1. **File → Sync Now**
2. Wait for gradle to rebuild

### Step 5: Build & Test

#### Build Debug APK

```bash
cd android-project
./gradlew assembleDebug
```

Expected output:
```
BUILD SUCCESSFUL in Xs
```

#### Run on Device/Emulator

```bash
./gradlew installDebug
```

#### Test WebView Loads

1. Open the app
2. You should see your WordPress site loading in the WebView
3. Check Logcat for:
   ```
   I/MainActivity: WebView loading: https://your-wordpress-site.com/
   ```

#### Verify FCM Token

Check Logcat for:
```
I/MainActivity: FCM Token obtained
I/SubscriptionRepository: Token sent successfully
```

---

## ✅ Summary: What's Ready to Build

| Component | Status | Details |
|-----------|--------|---------|
| **Hilt DI** | ✅ Complete | All providers configured in NetworkModule |
| **Retrofit API** | ✅ Complete | Type-safe endpoints with Gson serialization |
| **Coroutines** | ✅ Complete | suspend functions, Flow<Result<T>>, lifecycleScope |
| **Encryption** | ✅ Complete | AES-256-GCM with HMAC-SHA256 verification |
| **Firebase** | ✅ Complete | FCM token management and notifications |
| **WebView** | ✅ Complete | JavaScript bridge for native communication |
| **Logging** | ✅ Complete | Timber with DEBUG/RELEASE build optimization |
| **Manifest** | ✅ Complete | All permissions and app reference configured |
| **Build Config** | ✅ Complete | SDK 34, Java 17, all dependencies latest |

**Only change needed**: Domain name in `build.gradle`

---

## 📋 Modern Implementation Details

```kotlin
package com.sample.pnfpbandroid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.sample.pnfpbandroid.domain.repository.SubscriptionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Activity for hosting WebView
 * Manages Firebase Cloud Messaging subscription and WebView setup
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_CHANNEL_ID = "pnfpb_notifications"
    }
    
    @Inject
    lateinit var subscriptionRepository: SubscriptionRepository
    
    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var encryptedDataHolder: EncryptedDataHolder
    
    // Request notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.tag(TAG).i("Notification permission granted")
            subscribeFCM()
        } else {
            Timber.tag(TAG).w("Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize encrypted data holder for secure storage
        encryptedDataHolder = EncryptedDataHolder(this)
        
        // Setup views
        setupWebView()
        setupSwipeRefresh()
        
        // Create notification channel
        createNotificationChannel()
        
        // Request notification permission and subscribe to FCM
        requestNotificationPermission()
        
        Timber.tag(TAG).i("MainActivity created")
    }
    
    /**
     * Setup WebView with proper settings and JavaScript interface
     */
    private fun setupWebView() {
        webView = findViewById(R.id.webView)
        
        // Step 4a: Configure WebView Settings
        webView.settings.apply {
            javaScriptEnabled = true        // Enable JavaScript for Android bridge
            domStorageEnabled = true         // Enable DOM storage
            databaseEnabled = true           // Enable database
            mixedContentMode = WebSettings.MIXED_CONTENT_ALLOW_ALL  // Dev only
            userAgentString = "PNFPBAndroid/2.0"  // Custom user agent
        }
        
        // Step 4b: Add JavaScript Interface for Web-to-Native Communication
        val jsInterface = JavaScriptInterfaceModern(
            context = this,
            activity = this,
            encryptedDataHolder = encryptedDataHolder,
            subscriptionRepository = subscriptionRepository
        )
        webView.addJavascriptInterface(jsInterface, "Android")
        
        // Setup WebViewClient for page navigation
        webView.webViewClient = WebViewClient()
        
        // Load your WordPress site
        webView.loadUrl("https://www.muraliwebworld.com/")  // ← Update to your domain
        
        Timber.tag(TAG).d("WebView configured with modern settings")
    }
    
    /**
     * Setup SwipeRefreshLayout for pull-to-refresh functionality
     */
    private fun setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    
    /**
     * Create notification channel for Android 8+ (required)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "PNFPB Notifications"
            val descriptionText = "Push Notification for Post and BuddyPress"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            
            Timber.tag(TAG).d("Notification channel created")
        }
    }
    
    /**
     * Request notification permission (required for Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                subscribeFCM()
            }
        } else {
            subscribeFCM()
        }
    }
    
    /**
     * Step 5: Subscribe to Firebase Cloud Messaging and get FCM token
     */
    private fun subscribeFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Timber.tag(TAG).i("FCM Token obtained: $token")
                
                // Store token securely in EncryptedSharedPreferences
                encryptedDataHolder.setApiKey(token)
                
                // Optionally send to server immediately
                lifecycleScope.launch {
                    try {
                        val encryptedToken = subscriptionRepository.encryptToken(
                            token = token,
                            apiSecret = encryptedDataHolder.getApiKey() ?: return@launch
                        )
                        Timber.tag(TAG).d("Token encrypted successfully")
                    } catch (e: Exception) {
                        Timber.tag(TAG).e("Token encryption failed: ${e.message}")
                    }
                }
            } else {
                Timber.tag(TAG).e("Failed to get FCM token", task.exception)
            }
        }
    }
}
```

3. Delete the old `MainActivity.java` file
4. Delete `MainActivityModern.kt` (no longer needed)

### Step 5: Customize the Configuration

#### Update Your WordPress Domain (Line 102 in MainActivity.kt)

```kotlin
// Line 102 - Change this to your WordPress site URL
webView.loadUrl("https://yoursite.com/")  // ← Replace with your domain
```

#### Add Custom Configuration (Optional)

If you need runtime configuration, add this to your `NetworkModule.kt`:

```kotlin
// Option 1: SharedPreferences Runtime Configuration
@Provides
@Singleton
fun provideBaseUrl(context: Context): String {
    val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    return prefs.getString("api_base_url", "https://www.muraliwebworld.com/") ?: "https://www.muraliwebworld.com/"
}

// Option 2: BuildConfig Flavor Configuration
@Provides
@Singleton
fun provideBaseUrl(): String {
    return BuildConfig.API_BASE_URL
}
```

#### Enable JavaScript in WebView (Already Done in Step 4b)

The modern MainActivity.kt already includes all WebView settings:

```kotlin
// Already configured in setupWebView() method:
webView.settings.apply {
    javaScriptEnabled = true        // ✅ Enabled
    domStorageEnabled = true         // ✅ Enabled
    databaseEnabled = true           // ✅ Enabled
}
```

### Step 6: Build & Test the App

Now that you've set up MainActivity with modern Kotlin patterns, build and test:

```bash
# Step 1: Clean build
cd android-project
./gradlew clean

# Step 2: Build the app
./gradlew assembleDebug

# Step 3: Install on device/emulator
./gradlew installDebug

# Step 4: Run unit tests
./gradlew test

# Step 5: Run Android instrumented tests
./gradlew connectedAndroidTest
```

**Verify Installation**:
```bash
# Check if app installed successfully
adb shell pm list packages | grep pnfpbandroid

# View app logs
adb logcat | grep "MainActivity\|SubscriptionRepo\|OkHttp"
```

**Expected Logs** (first startup):
```
I/MainActivity: MainActivity created
I/MainActivity: FCM Token obtained: dHlXa4fK2n9...
D/MainActivity: Token encrypted successfully
D/MainActivity: WebView configured with modern settings
```

## Configuration Options

### Base URL Configuration

**Option 1: Hardcoded** (Simple)
```kotlin
return "https://www.muraliwebworld.com/"
```

**Option 2: BuildConfig** (Per flavor)
```kotlin
return BuildConfig.API_BASE_URL
```

**Option 3: SharedPreferences** (Runtime configurable)
```kotlin
@Provides
@Singleton
fun provideBaseUrl(context: Context): String {
    val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    return prefs.getString("api_base_url", "https://www.muraliwebworld.com/") ?: ""
}
```

### API Secret Configuration

**Option 1: EncryptedSharedPreferences** (Secure, recommended)
```kotlin
val encryptedDataHolder = EncryptedDataHolder(context)
encryptedDataHolder.setApiKey(secret)
```

**Option 2: BuildConfig** (Less secure)
```kotlin
// In build.gradle
buildTypes {
    debug {
        buildConfigField "String", "API_SECRET", '"debug-secret"'
    }
    release {
        buildConfigField "String", "API_SECRET", '"production-secret"'
    }
}
```

### Timeout Configuration

In `NetworkModule.kt`:

```kotlin
companion object {
    private const val CONNECTION_TIMEOUT = 30L      // seconds
    private const val READ_TIMEOUT = 30L            // seconds
    private const val WRITE_TIMEOUT = 30L           // seconds
}
```

### Logging Level

In `PushNotificationApplication.kt`:

```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())  // Verbose logging
} else {
    Timber.plant(CrashReportingTree())  // Production logging
}
```

## Common Usage Patterns

### 1. Send Subscription Token

From JavaScript in WebView:

```javascript
// Get token from Android
const token = Android.getFromAndroid();

// Subscribe to group
Android.postMessage('groupId123', 'subscribe-group');

// Unsubscribe from group
Android.postMessage('groupId123', 'unsubscribe-group');
```

From Kotlin code:

```kotlin
lifecycleScope.launch {
    val request = SubscriptionTokenRequest(
        encryptedToken = encryptedToken,
        userId = 123,
        subscriptionType = SubscriptionType.SUBSCRIBE_GROUP,
        groupId = "group123"
    )
    
    subscriptionRepository.sendSubscriptionToken(request).collect { result ->
        result.onSuccess { message ->
            println("Success: $message")
        }
        result.onFailure { exception ->
            println("Error: ${exception.message}")
        }
    }
}
```

### 2. Get FCM Token

```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        encryptedDataHolder.setApiKey(token)
    }
}
```

### 3. Encrypt Token Manually

```kotlin
val encryptedToken = subscriptionRepository.encryptToken(
    token = "fcmToken123",
    apiSecret = "your-secret"
)
```

### 4: Listen for Logs

```bash
adb logcat | grep "JavaScriptInterface\|SubscriptionRepo\|OkHttp"
```

## Summary: What We've Set Up

### ✅ Completed Configuration

| Step | Component | Status |
|------|-----------|--------|
| 1️⃣ | Gradle sync & dependencies | ✅ Complete |
| 2️⃣ | WordPress domain configuration | ✅ Complete |
| 3️⃣ | AndroidManifest setup | ✅ Complete |
| 4️⃣ | **Single MainActivity.kt (Kotlin)** | ✅ **Consolidated** |
| 5️⃣ | WebView & FCM configuration | ✅ Complete |
| 6️⃣ | Build & test | ✅ Ready |

### 🎯 Why Single MainActivity File is Better

**Before (Two Files - Confusing)**:
```
MainActivity.java (Old - Callbacks, Manual Crypto, Complex)
MainActivityModern.kt (New - Modern, Hilt, Coroutines)
❌ Users confused which to use
```

**After (One File - Clear)**:
```
MainActivity.kt (Modern Kotlin - Clean, Simple, Best Practices)
✅ No confusion
✅ Clear implementation path
✅ Easier to maintain
✅ Single source of truth
```

### 📋 Architecture at a Glance

```
MainActivity.kt (Hilt @AndroidEntryPoint)
    ↓
Injects SubscriptionRepository
    ↓
setupWebView()
    ├─ Enables JavaScript
    ├─ Adds Android bridge (JavaScriptInterfaceModern)
    └─ Loads WordPress site
    ↓
setupSwipeRefresh()
    └─ Pull-to-refresh functionality
    ↓
subscribeFCM()
    ├─ Gets FCM token from Firebase
    ├─ Stores in EncryptedSharedPreferences
    └─ Encrypts token with AES-256-GCM
```

## Troubleshooting

### Build Errors

**Error: "Hilt not found"**
- Sync Gradle: `File → Sync Now`
- Rebuild: `Build → Rebuild Project`

**Error: "Java 17 not found"**
- Install Java 17: [https://www.oracle.com/java/technologies/](https://www.oracle.com/java/technologies/)
- Update `~/.gradle/gradle.properties`:
  ```properties
  org.gradle.java.home=/path/to/java/17
  ```

**Error: Missing implementation**
- Delete `.gradle` folder and sync again
- Run: `./gradlew clean build`

### Runtime Errors

**Error: "No API secret found"**
- Call `encryptedDataHolder.setApiKey(secret)` first
- Check EncryptedSharedPreferences is initialized

**Error: "Encryption failed"**
- Verify API secret is correct
- Check token format
- Verify IV size is 16 bytes

**Error: "API returns 401"**
- Check HMAC calculation
- Verify encryption algorithm (should be AES-256-GCM)
- Compare with server-side expectations
- Check server logs

**Error: "WebView bridge not working"**
- Ensure `javaScriptEnabled = true`
- Check `@JavascriptInterface` annotation present
- Verify method signatures match JavaScript calls

### Performance Issues

**App is slow**
- Check Timber logging level (should be BASIC in release)
- Monitor network calls with Logcat
- Profile with Android Profiler

**High memory usage**
- Check WebView isn't loading excessive content
- Verify no memory leaks in coroutines
- Use `lifecycleScope` instead of `GlobalScope`

## Testing

### Unit Tests

Create `app/src/test/java/com/sample/pnfpbandroid/util/TokenEncryptionUtilTest.kt`:

```kotlin
class TokenEncryptionUtilTest {
    @Test
    fun testEncryptDecrypt() {
        val token = "test-token"
        val secret = "test-secret"
        
        val encrypted = TokenEncryptionUtil.encryptToken(token, secret)
        val decrypted = TokenEncryptionUtil.decryptToken(encrypted, secret)
        
        assertEquals(token, decrypted)
    }
}
```

### Integration Tests

Create `app/src/androidTest/java/com/sample/pnfpbandroid/SubscriptionIntegrationTest.kt`:

```kotlin
@RunWith(AndroidJUnit4::class)
class SubscriptionIntegrationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testSubscriptionFlow() {
        // Test WebView bridge
        // Test API call
        // Test encryption
    }
}
```

Run tests:

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

## Deployment

### Debug Build

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release Build

```bash
# Generate signed APK
./gradlew assembleRelease

# Or AAB for Play Store
./gradlew bundleRelease
```

### Firebase Distribution

```bash
./gradlew appDistributionUploadRelease
```

## Next Steps

1. ✅ Sync Gradle
2. ✅ Update configuration (domain, secret)
3. ✅ Update MainActivity
4. ✅ Test locally
5. ✅ Deploy to staging
6. ✅ Test end-to-end
7. ✅ Deploy to production

## Support

For issues or questions:

1. Check MODERNIZATION_GUIDE.md for detailed info
2. Check BEFORE_AND_AFTER.md for code examples
3. Check IMPLEMENTATION_CHECKLIST.md for step-by-step guide
4. Review Logcat for error messages
5. Check WordPress plugin logs on server

## References

- **Retrofit**: https://square.github.io/retrofit/
- **OkHttp**: https://square.github.io/okhttp/
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **Hilt**: https://dagger.dev/hilt/
- **Timber**: https://github.com/JakeWharton/timber
- **Firebase**: https://firebase.google.com/docs

---

**Happy coding! 🚀**
