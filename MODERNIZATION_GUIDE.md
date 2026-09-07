/**
 * ANDROID APP MODERNIZATION GUIDE
 * Push Notification for WordPress Plugin - Android Client
 * 
 * VERSION: 2.0 (Modernized)
 * Updated: 2026
 * 
 * OVERVIEW
 * ========
 * This Android application has been modernized to use latest Android development standards,
 * libraries, and patterns. The app communicates with the WordPress "push-notification-for-post-and-buddypress"
 * plugin to send FCM push notification subscription tokens.
 * 
 * KEY MODERNIZATIONS
 * ==================
 * 
 * 1. BUILD CONFIGURATION
 *    ✓ Updated compileSdk to 34 (latest Android version)
 *    ✓ Updated targetSdk to 34
 *    ✓ Bumped minSdk to 24 (improved device coverage)
 *    ✓ Java 17 compatibility
 * 
 * 2. DEPENDENCY UPDATES
 *    ✓ Firebase BOM: 33.1.0 (latest)
 *    ✓ OkHttp: 4.12.0 (from 4.9.0)
 *    ✓ Retrofit: 2.11.0 (new - for better API management)
 *    ✓ Coroutines: 1.8.0 (for modern async operations)
 *    ✓ Hilt: 2.51.1 (dependency injection)
 *    ✓ Timber: 5.1.0 (professional logging)
 *    ✓ Gson: 2.10.1 (JSON parsing)
 * 
 * 3. ARCHITECTURE IMPROVEMENTS
 *    ✓ Repository Pattern for data layer separation
 *    ✓ Retrofit for type-safe API calls
 *    ✓ Kotlin Coroutines for async/await operations
 *    ✓ Hilt for dependency injection
 *    ✓ Data classes for type-safe models
 *    ✓ Flow API for reactive streams
 * 
 * 4. CODE QUALITY
 *    ✓ Converted to Kotlin (modern language)
 *    ✓ Proper error handling with Timber logging
 *    ✓ Type-safe API requests and responses
 *    ✓ Separation of concerns (API, Domain, Data layers)
 *    ✓ Null-safety through Kotlin nullable types
 * 
 * PROJECT STRUCTURE
 * =================
 * com/sample/pnfpbandroid/
 * ├── data/
 * │   ├── api/
 * │   │   └── PushNotificationApiService.kt      (Retrofit API interface)
 * │   ├── model/
 * │   │   └── SubscriptionTokenRequest.kt        (Request/Response models)
 * │   └── repository/
 * │       └── SubscriptionRepositoryImpl.kt       (Data layer implementation)
 * ├── domain/
 * │   └── repository/
 * │       └── SubscriptionRepository.kt          (Domain layer interface)
 * ├── util/
 * │   └── TokenEncryptionUtil.kt                 (AES-GCM encryption)
 * ├── di/
 * │   └── NetworkModule.kt                       (Hilt modules for DI)
 * ├── JavaScriptInterfaceModern.kt               (WebView communication)
 * ├── EncryptedDataHolder.java                   (Secure storage)
 * ├── MainActivity.java                          (WebView container)
 * └── PushNotificationApplication.kt             (App class with Hilt)
 * 
 * API ENDPOINT DETAILS
 * ====================
 * 
 * Endpoint: POST /wp-json/PNFPBpush/v1/subscriptiontoken
 * Base URL: https://<your-domain>/
 * 
 * Request Format:
 * {
 *   "token": "encryptedToken:base64(iv):hmac:hmac",
 *   "userid": 0,
 *   "groupid": "",
 *   "subscription-type": "subscribe-group|unsubscribe-group|",
 *   "subscriptionoptions": "",
 *   "cookievalue": ""
 * }
 * 
 * Response Format:
 * {
 *   "status": 200,
 *   "message": "Subscription successful",
 *   "data": { ... }
 * }
 * 
 * ENCRYPTION DETAILS
 * ==================
 * 
 * Algorithm: AES-256-GCM
 * IV Size: 16 bytes (randomly generated)
 * Authentication Tag: 128 bits
 * 
 * Format: "base64(encryptedToken):base64(iv):hex(hmac):hex(hmac)"
 * 
 * Steps:
 * 1. Generate random 16-byte IV
 * 2. Encrypt token using AES-256-GCM with IV
 * 3. Calculate HMAC-SHA256 of original token
 * 4. Base64 encode encrypted data and IV
 * 5. Hex encode HMAC
 * 6. Combine: encryptedData:iv:hmac:hmac
 * 
 * USAGE EXAMPLES
 * ==============
 * 
 * 1. Initialize in MainActivity:
 * 
 *    @AndroidEntryPoint
 *    class MainActivity : AppCompatActivity() {
 *        @Inject
 *        lateinit var subscriptionRepository: SubscriptionRepository
 *        
 *        override fun onCreate(savedInstanceState: Bundle?) {
 *            super.onCreate(savedInstanceState)
 *            setContentView(R.layout.activity_main)
 *            
 *            val encryptedDataHolder = EncryptedDataHolder(this)
 *            val jsInterface = JavaScriptInterfaceModern(
 *                this,
 *                this,
 *                encryptedDataHolder,
 *                subscriptionRepository
 *            )
 *            webView.addJavascriptInterface(jsInterface, "Android")
 *        }
 *    }
 * 
 * 2. JavaScript calls from WebView:
 * 
 *    // Get token from Android
 *    var token = Android.getFromAndroid();
 *    
 *    // Subscribe to group
 *    Android.postMessage('groupId123', 'subscribe-group');
 *    
 *    // Unsubscribe from group
 *    Android.postMessage('groupId123', 'unsubscribe-group');
 * 
 * 3. Send subscription manually:
 * 
 *    lifecycleScope.launch {
 *        val request = SubscriptionTokenRequest(
 *            encryptedToken = "...",
 *            userId = 123,
 *            subscriptionType = SubscriptionType.SUBSCRIBE_GROUP,
 *            groupId = "group123"
 *        )
 *        
 *        subscriptionRepository.sendSubscriptionToken(request).collect { result ->
 *            result.onSuccess { message ->
 *                Log.i("Success", message)
 *            }
 *            result.onFailure { exception ->
 *                Log.e("Error", exception.message)
 *            }
 *        }
 *    }
 * 
 * MIGRATION FROM OLD CODE
 * =======================
 * 
 * Old (OkHttp + synchronous):
 *   - Used raw OkHttpClient
 *   - Synchronous API calls
 *   - Manual JSON parsing
 *   - Basic error handling
 * 
 * New (Retrofit + Coroutines):
 *   - Retrofit for type-safe API
 *   - Coroutines for async/await
 *   - Automatic JSON parsing with Gson
 *   - Comprehensive error handling
 *   - Professional logging with Timber
 *   - Dependency injection with Hilt
 * 
 * TESTING
 * =======
 * 
 * Unit Tests:
 *   - TokenEncryptionUtil.kt - Encryption/Decryption
 *   - SubscriptionRepositoryImpl.kt - API calls
 * 
 * Integration Tests:
 *   - End-to-end subscription flow
 *   - WebView JavaScript bridge
 * 
 * SECURITY CONSIDERATIONS
 * =======================
 * 
 * ✓ Uses EncryptedSharedPreferences for API secret storage
 * ✓ AES-256-GCM encryption for token transmission
 * ✓ HMAC-SHA256 verification for data integrity
 * ✓ No hardcoded credentials
 * ✓ Automatic certificate pinning support (via OkHttp)
 * 
 * OPTIONAL ENHANCEMENTS
 * =====================
 * 
 * 1. Certificate Pinning:
 *    Add certificate pinning to OkHttpClient for production:
 *    
 *    val certificatePinner = CertificatePinner.Builder()
 *        .add("muraliwebworld.com", "sha256/...")
 *        .build()
 *    okHttpBuilder.certificatePinner(certificatePinner)
 * 
 * 2. Retry Policy:
 *    Already implemented with retryOnConnectionFailure = true
 * 
 * 3. Request Timeout:
 *    Currently set to 30 seconds (configurable in NetworkModule.kt)
 * 
 * 4. Network State Monitoring:
 *    Can be added using androidx.work for background retry
 * 
 * TROUBLESHOOTING
 * ===============
 * 
 * Issue: Encryption/Decryption fails
 *   - Check API secret is correct
 *   - Verify IV is 16 bytes
 *   - Check token encoding
 * 
 * Issue: API call fails with 401
 *   - Verify token format is correct
 *   - Check HMAC calculation
 *   - Ensure encryption matches server expectation
 * 
 * Issue: WebView JavaScript bridge not working
 *   - Ensure @JavascriptInterface annotation is present
 *   - Check Activity is LifecycleOwner
 *   - Enable JavaScript in WebView settings
 * 
 * LOGGING
 * =======
 * 
 * Timber is configured for automatic logging:
 * - DEBUG builds: Full BODY logging
 * - RELEASE builds: BASIC logging only
 * 
 * View logs:
 *   adb logcat | grep "JavaScriptInterface\\|OkHttp\\|SubscriptionRepo"
 * 
 * VERSION HISTORY
 * ================
 * 
 * 2.0 (Current)
 *   - Modernized to Kotlin
 *   - Added Retrofit
 *   - Added Coroutines
 *   - Added Hilt DI
 *   - Improved error handling
 *   - Updated all dependencies
 *   - SDK 33 -> 34
 *   - Java 11 -> 17
 * 
 * 1.2
 *   - Initial OkHttp implementation
 *   - Basic encryption support
 *   - Firebase integration
 * 
 * SUPPORT & DOCUMENTATION
 * ========================
 * 
 * For WordPress Plugin Documentation:
 *   See: push-notification-for-post-and-buddypress/README.md
 * 
 * For Retrofit Documentation:
 *   https://square.github.io/retrofit/
 * 
 * For Kotlin Coroutines:
 *   https://kotlinlang.org/docs/coroutines-overview.html
 * 
 * For Hilt Dependency Injection:
 *   https://dagger.dev/hilt/
 */
