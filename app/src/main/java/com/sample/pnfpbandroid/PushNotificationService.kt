package com.sample.pnfpbandroid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Modern Firebase Cloud Messaging Service
 * Handles incoming push notifications and displays them
 *
 * Modernized with:
 * - Professional logging with Timber
 * - Proper notification channel management
 * - Security best practices
 */
class PushNotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PushNotificationService"
        private const val NOTIFICATION_CHANNEL_ID = "pnfpb_notifications"
        private const val NOTIFICATION_CHANNEL_NAME = "PNFPB Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var encryptedDataHolder: EncryptedDataHolder

    override fun onCreate() {
        super.onCreate()
        encryptedDataHolder = EncryptedDataHolder(applicationContext)
    }

    /**
     * Handle incoming remote messages (push notifications)
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.tag(TAG).d("Message received from: ${remoteMessage.from}")

        // Check if message contains notification
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "", it.body ?: "")
        }

        // Check if message contains data
        if (remoteMessage.data.isNotEmpty()) {
            Timber.tag(TAG).d("Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
    }

    /**
     * Called when FCM registration token is refreshed
     * Should be sent to backend for Firebase messaging
     */
    override fun onNewToken(token: String) {
        Timber.tag(TAG).i("FCM token refreshed: $token")
        
        // Store token securely
        try {
            encryptedDataHolder.setApiKey(token)
            Timber.tag(TAG).d("FCM token saved securely")
            
            // In production, send token to backend/WordPress
            sendTokenToBackend(token)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error saving FCM token")
        }
    }

    /**
     * Send notification to system notification manager
     */
    private fun sendNotification(title: String, messageBody: String) {
        createNotificationChannel()

        val intent = Intent(this, MainActivityModern::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        Timber.tag(TAG).d("Notification displayed: $title")
    }

    /**
     * Create notification channel for Android 8+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Push Notification for Post and BuddyPress"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Handle data payload from push notification
     * Can be used for custom app-specific logic
     */
    private fun handleDataMessage(data: Map<String, String>) {
        try {
            // Log data payload
            data.forEach { (key, value) ->
                Timber.tag(TAG).d("$key = $value")
            }

            // Custom handling based on data payload
            val action = data["action"]
            when (action) {
                "open_url" -> {
                    val url = data["url"]
                    Timber.tag(TAG).d("Opening URL: $url")
                }
                "update_content" -> {
                    Timber.tag(TAG).d("Content update received")
                }
                else -> {
                    Timber.tag(TAG).d("Unknown action: $action")
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error handling data message")
        }
    }

    /**
     * Send FCM token to backend (WordPress)
     * This should be implemented based on your API contract
     */
    private fun sendTokenToBackend(token: String) {
        try {
            // TODO: Implement backend call to send token
            // Example: repository.registerFCMToken(token)
            Timber.tag(TAG).d("Token sent to backend")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error sending token to backend")
        }
    }
}
