package com.sample.pnfpbandroid

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import android.app.Activity
import kotlinx.coroutines.launch
import timber.log.Timber
import com.sample.pnfpbandroid.data.model.SubscriptionTokenRequest
import com.sample.pnfpbandroid.data.model.SubscriptionType
import com.sample.pnfpbandroid.domain.repository.SubscriptionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Modernized JavaScriptInterface for WebView communication
 * Handles push notification subscription token management with modern Kotlin patterns
 *
 * Usage in WebView:
 *   webView.addJavascriptInterface(JavaScriptInterfaceModern(activity, encryptedDataHolder), "Android")
 */
class JavaScriptInterfaceModern(
    private val context: Context,
    private val activity: Activity?,
    private val encryptedDataHolder: EncryptedDataHolder,
    private val subscriptionRepository: SubscriptionRepository
) {
    
    companion object {
        private const val TAG = "JavaScriptInterface"
    }
    
    /**
     * Called from JavaScript to get the push notification token
     * @return The FCM token stored by the mobile app
     */
    @JavascriptInterface
    fun getFromAndroid(): String {
        return try {
            val token = encryptedDataHolder.apiKey
            Timber.tag(TAG).d("Token retrieved from Android")
            token
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to retrieve token")
            ""
        }
    }
    
    /**
     * Called from JavaScript to post subscription message
     * Handles subscription, unsubscription, and user-specific requests
     *
     * @param subscriptionOptions The subscription options (group ID or user ID)
     * @param subscriptionType The type of subscription (subscribe-group, unsubscribe-group, etc.)
     */
    @JavascriptInterface
    fun postMessage(subscriptionOptions: String, subscriptionType: String = "") {
        Timber.tag(TAG).d("PostMessage called with options: $subscriptionOptions, type: $subscriptionType")
        
        val lifecycleOwner = activity as? LifecycleOwner
        if (lifecycleOwner != null) {
            lifecycleOwner.lifecycleScope.launch {
                try {
                    sendSubscriptionToken(subscriptionOptions, subscriptionType)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to send subscription token")
                    onSubscriptionError(e.message ?: "Unknown error")
                }
            }
        } else {
            Timber.tag(TAG).w("Activity is null or not a LifecycleOwner")
        }
    }
    
    /**
     * Simplified postMessage method that accepts subscription type
     * @param subscriptionOptions The subscription options
     */
    @JavascriptInterface
    fun postGroupSubscription(subscriptionOptions: String) {
        postMessage(subscriptionOptions, SubscriptionType.SUBSCRIBE_GROUP)
    }
    
    /**
     * Internal method to send subscription token asynchronously
     */
    private suspend fun sendSubscriptionToken(subscriptionOptions: String, subscriptionType: String) {
        val token = encryptedDataHolder.apiKey
        if (token.isEmpty()) {
            Timber.tag(TAG).w("No token available")
            onSubscriptionError("No authentication token available")
            return
        }
        
        try {
            val apiSecret = encryptedDataHolder.apiKey // This should come from secure storage
            
            // Encrypt the token
            val encryptedToken = subscriptionRepository.encryptToken(token, apiSecret)
            
            // Build the request
            val request = buildSubscriptionRequest(
                encryptedToken,
                subscriptionOptions,
                subscriptionType
            )
            
            Timber.tag(TAG).d("Sending subscription token request: type=$subscriptionType")
            
            // Send the request via repository (Flow-based)
            subscriptionRepository.sendSubscriptionToken(request).collect { result: kotlin.Result<String> ->
                result.onSuccess { message: String ->
                    Timber.tag(TAG).i("Subscription successful: $message")
                    onSubscriptionSuccess(message)
                }
                result.onFailure { exception: kotlin.Throwable ->
                    Timber.tag(TAG).e(exception, "Subscription failed")
                    onSubscriptionError(exception.message ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error processing subscription")
            onSubscriptionError(e.message ?: "Processing error")
        }
    }
    
    /**
     * Build subscription request based on type
     */
    private fun buildSubscriptionRequest(
        encryptedToken: String,
        subscriptionOptions: String,
        subscriptionType: String
    ): SubscriptionTokenRequest {
        return when (subscriptionType) {
            SubscriptionType.SUBSCRIBE_GROUP -> {
                SubscriptionTokenRequest(
                    encryptedToken = encryptedToken,
                    groupId = subscriptionOptions,
                    subscriptionType = SubscriptionType.SUBSCRIBE_GROUP
                )
            }
            SubscriptionType.UNSUBSCRIBE_GROUP -> {
                SubscriptionTokenRequest(
                    encryptedToken = encryptedToken,
                    groupId = subscriptionOptions,
                    subscriptionType = SubscriptionType.UNSUBSCRIBE_GROUP
                )
            }
            SubscriptionType.FRONTEND_OPTIONS -> {
                SubscriptionTokenRequest(
                    encryptedToken = encryptedToken,
                    subscriptionOptions = subscriptionOptions
                )
            }
            else -> {
                // Default subscription
                SubscriptionTokenRequest(
                    encryptedToken = encryptedToken,
                    subscriptionOptions = subscriptionOptions
                )
            }
        }
    }
    
    /**
     * Called when subscription is successful
     * Override or set callback for custom handling
     */
    private fun onSubscriptionSuccess(message: String) {
        Timber.tag(TAG).i("Subscription success: $message")
        // Notify JavaScript side if needed
        activity?.runOnUiThread {
            // You can call JavaScript method here if needed
            // webView.evaluateJavascript("javascript:onSubscriptionSuccess('$message')", null)
        }
    }
    
    /**
     * Called when subscription fails
     * Override or set callback for custom error handling
     */
    private fun onSubscriptionError(errorMessage: String) {
        Timber.tag(TAG).e("Subscription error: $errorMessage")
        // Notify JavaScript side if needed
        activity?.runOnUiThread {
            // You can call JavaScript method here if needed
            // webView.evaluateJavascript("javascript:onSubscriptionError('$errorMessage')", null)
        }
    }
}
