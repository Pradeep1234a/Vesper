package com.vesper.ledger.data.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationSyncService : FirebaseMessagingService() {

    private val TAG = "PushNotificationSync"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log registration token to console so users can target this specific device in Firebase campaigns
        Log.d(TAG, "FCM Token updated successfully: $token")
        
        // Save to SharedPreferences for debugging retrieve
        val sharedPrefs = getSharedPreferences("vesper_settings", MODE_PRIVATE)
        sharedPrefs.edit().putString("fcm_registration_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received new FCM push notification. From: ${remoteMessage.from}")

        // 1. Extract title and body from payload (favoring data payload for automation, fallback to notification payload)
        val dataTitle = remoteMessage.data["title"]
        val dataBody = remoteMessage.data["body"]
        
        val notificationTitle = remoteMessage.notification?.title
        val notificationBody = remoteMessage.notification?.body

        val finalTitle = dataTitle ?: notificationTitle ?: "System Update"
        val finalBody = dataBody ?: notificationBody ?: "Ledger sync event processed."

        // 2. Parse category from data payload
        val categoryStr = remoteMessage.data["category"]
        val category = if (categoryStr != null) {
            try {
                NotificationCategory.valueOf(categoryStr)
            } catch (e: Exception) {
                Log.w(TAG, "Invalid category '$categoryStr' in FCM payload. Defaulting to PRODUCT_UPDATES.")
                NotificationCategory.PRODUCT_UPDATES
            }
        } else {
            NotificationCategory.PRODUCT_UPDATES
        }

        Log.d(TAG, "Dispatching Cloud-Based Notification: Title='$finalTitle', Category=${category.name}")

        // 3. Deliver immediately and log to local DB.
        // We set bypassCooldown = true because cloud-initiated alerts from Firebase should bypass the local deduplication window.
        VesperNotificationApi.sendNotification(
            title = finalTitle,
            body = finalBody,
            category = category,
            bypassCooldown = true
        )
    }
}
