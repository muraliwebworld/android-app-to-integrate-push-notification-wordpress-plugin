package com.sample.pnfpbandroid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.sample.pnfpbandroid.R
import com.sample.pnfpbandroid.domain.repository.SubscriptionRepository
import com.sample.pnfpbandroid.data.model.SubscriptionTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Activity for hosting WebView
 * Manages Firebase Cloud Messaging subscription and WebView setup
 * 
 * Modernized with:
 * - Hilt dependency injection
 * - Proper lifecycle management
 * - Kotlin coroutines
 * - Professional logging
 */
@AndroidEntryPoint
class MainActivityModern : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_CHANNEL_ID = "pnfpb_notifications"
    }
    
    @Inject
    lateinit var subscriptionRepository: SubscriptionRepository
    
    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var encryptedDataHolder: EncryptedDataHolder
    
    // Base URL injected from NetworkModule (configured in build.gradle)
    @Inject
    lateinit var baseUrl: String
    
    // Request notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
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
        encryptedDataHolder.setApiSecret(BuildConfig.PNFPB_API_SECRET)
        
        // Setup views
        setupWebView()
        setupSwipeRefresh()
        
        // Create notification channel
        createNotificationChannel()
        
        // Request notification permission and subscribe to FCM
        requestNotificationPermission()

        // Handle back press for WebView navigation
        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                this.isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        
        Timber.tag(TAG).i("MainActivity created")
    }
    
    /**
     * Setup WebView with proper settings and JavaScript interface
     */
    private fun setupWebView() {
        webView = findViewById(R.id.webView)
        
        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            // Allow mixed content for development if API 21+ (value 1 = MIXED_CONTENT_COMPAT_MODE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                mixedContentMode = 1 // MIXED_CONTENT_COMPAT_MODE
            }
            userAgentString = "PNFPBAndroid/2.0"
        }
        
        // Add JavaScript interface for communication
        val jsInterface = JavaScriptInterfaceModern(
            this,
            this,
            encryptedDataHolder,
            subscriptionRepository
        )
        webView.addJavascriptInterface(jsInterface, "Android")
        webView.addJavascriptInterface(JavaScriptInterfaceModern(this, this, encryptedDataHolder, subscriptionRepository, "subscribe-group"), "subscribeGroupid")
        webView.addJavascriptInterface(JavaScriptInterfaceModern(this, this, encryptedDataHolder, subscriptionRepository, "unsubscribe-group"), "unsubscribeGroupid")
        webView.addJavascriptInterface(JavaScriptInterfaceModern(this, this, encryptedDataHolder, subscriptionRepository, "frontend-options"), "frontendsubscriptionOptions")
        webView.addJavascriptInterface(JavaScriptInterfaceModern(this, this, encryptedDataHolder, subscriptionRepository, "user-id"), "pnfpbuserid")
        
        // Setup WebViewClient
        webView.webViewClient = WebViewClient()
        
        // Load webpage (domain configured in build.gradle)
        webView.loadUrl(baseUrl)
        Timber.tag(TAG).d("WebView loading: $baseUrl")
        Timber.tag(TAG).d("WebView configured")
    }
    
    /**
     * Setup SwipeRefreshLayout for pull-to-refresh
     */
    private fun setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    
    /**
     * Create notification channel for Android 8+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
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
     * Request notification permission (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            when (ContextCompat.checkSelfPermission(this, permission)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    subscribeFCM()
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(permission)
                }
            }
        } else {
            // For Android 12 and below, permission is granted at install time
            subscribeFCM()
        }
    }
    
    /**
     * Subscribe to Firebase Cloud Messaging
     * Retrieves FCM token and stores it securely
     */
    private fun subscribeFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result as? String
                Timber.tag(TAG).i("FCM Token obtained: ${token?.take(20)}...")
                
                // Store token securely
                if (token != null) {
                    encryptedDataHolder.setFcmToken(token)
                    sendFirebaseTokenToBackend(token)
                }
                
                // Optionally notify JavaScript that token is ready
                lifecycleScope.launch {
                    webView.post {
                        webView.evaluateJavascript(
                            "javascript:if(window.onFCMTokenReady) { window.onFCMTokenReady(); }",
                            null
                        )
                    }
                }
            } else {
                Timber.tag(TAG).e(task.exception, "Failed to get FCM token")
            }
        }
    }

    /**
     * Register the FCM token immediately after Firebase returns it. This is
     * intentionally independent of the WebView JavaScript callbacks so a
     * fresh app install is registered even when the page has not loaded yet.
     */
    private fun sendFirebaseTokenToBackend(token: String) {
        val apiSecret = BuildConfig.PNFPB_API_SECRET
        if (apiSecret.isBlank() || apiSecret == "CHANGE_ME_PNFPB_API_SECRET") {
            Timber.tag(TAG).e("PNFPB API secret is not configured")
            return
        }

        lifecycleScope.launch {
            try {
                val encryptedToken = subscriptionRepository.encryptToken(token, apiSecret)
                val request = SubscriptionTokenRequest(
                    encryptedToken = encryptedToken,
                    userId = 0,
                    groupId = "",
                    subscriptionType = "",
                    subscriptionOptions = "",
                    cookieValue = ""
                )

                subscriptionRepository.sendSubscriptionToken(request).collect { result ->
                    result.onSuccess { message ->
                        Timber.tag(TAG).i("FCM token registered with WordPress: $message")
                    }.onFailure { error ->
                        Timber.tag(TAG).e(error, "Failed to register FCM token with WordPress")
                    }
                }
            } catch (error: Exception) {
                Timber.tag(TAG).e(error, "Unable to register FCM token with WordPress")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
        webView.destroy()
    }
}
