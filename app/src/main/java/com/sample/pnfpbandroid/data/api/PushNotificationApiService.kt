package com.sample.pnfpbandroid.data.api

import com.sample.pnfpbandroid.data.model.SubscriptionTokenRequest
import com.sample.pnfpbandroid.data.model.SubscriptionTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service for Push Notification endpoints
 * Handles communication with WordPress Push Notification plugin REST API
 */
interface PushNotificationApiService {
    
    /**
     * Send subscription token to WordPress
     * POST /wp-json/PNFPBpush/v1/subscriptiontoken
     *
     * @param request The encrypted subscription token request
     * @return Response containing status and message from WordPress
     */
    @POST("PNFPBpush/v1/subscriptiontoken")
    suspend fun sendSubscriptionToken(
        @Body request: SubscriptionTokenRequest
    ): Response<SubscriptionTokenResponse>
}
