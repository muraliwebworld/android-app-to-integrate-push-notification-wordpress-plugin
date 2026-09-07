package com.sample.pnfpbandroid.domain.repository

import com.sample.pnfpbandroid.data.model.SubscriptionTokenRequest
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for subscription token operations
 * Handles business logic and data flow
 */
interface SubscriptionRepository {
    
    /**
     * Send encrypted subscription token to server
     *
     * @param request The subscription request containing encrypted token
     * @return Flow emitting the result of the operation
     */
    fun sendSubscriptionToken(request: SubscriptionTokenRequest): Flow<Result<String>>
    
    /**
     * Get encrypted subscription token by encrypting the input token
     *
     * @param token The plain FCM token to encrypt
     * @param apiSecret The API secret key for encryption
     * @return Encrypted token in format: "encryptedToken:iv:hmac:hmac"
     */
    suspend fun encryptToken(token: String, apiSecret: String): String
}
