package com.sample.pnfpbandroid.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for sending push notification subscription token to WordPress API
 * Corresponds to /wp-json/PNFPBpush/v1/subscriptiontoken endpoint
 */
data class SubscriptionTokenRequest(
    @SerializedName("token")
    val encryptedToken: String,
    
    @SerializedName("userid")
    val userId: Int = 0,
    
    @SerializedName("groupid")
    val groupId: String = "",
    
    @SerializedName("subscription-type")
    val subscriptionType: String = "",
    
    @SerializedName("subscriptionoptions")
    val subscriptionOptions: String = "",
    
    @SerializedName("cookievalue")
    val cookieValue: String = ""
)

/**
 * Response model from WordPress API
 */
data class SubscriptionTokenResponse(
    @SerializedName("status")
    val status: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: Map<String, Any>? = null
)

/**
 * Subscription type constants
 */
object SubscriptionType {
    const val SUBSCRIBE_GROUP = "subscribe-group"
    const val UNSUBSCRIBE_GROUP = "unsubscribe-group"
    const val FRONTEND_OPTIONS = "frontend-options"
    const val EMPTY = ""
}
