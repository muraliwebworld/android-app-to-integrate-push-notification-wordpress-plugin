package com.sample.pnfpbandroid.data.repository

import com.sample.pnfpbandroid.data.api.PushNotificationApiService
import com.sample.pnfpbandroid.data.model.SubscriptionTokenRequest
import com.sample.pnfpbandroid.domain.repository.SubscriptionRepository
import com.sample.pnfpbandroid.util.TokenEncryptionUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of SubscriptionRepository
 * Handles API calls and token encryption/decryption
 */
class SubscriptionRepositoryImpl @Inject constructor(
    private val apiService: PushNotificationApiService
) : SubscriptionRepository {
    
    private val tag = "SubscriptionRepo"
    
    override fun sendSubscriptionToken(request: SubscriptionTokenRequest): Flow<Result<String>> = flow {
        try {
            Timber.tag(tag).i("Sending subscription token to server")
            
            val response = apiService.sendSubscriptionToken(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == 200) {
                    Timber.tag(tag).i("Subscription token sent successfully: ${body.message}")
                    emit(Result.success(body.message))
                } else {
                    val errorMsg = body?.message ?: "Unknown error"
                    Timber.tag(tag).w("Server returned error: $errorMsg")
                    emit(Result.failure(Exception(errorMsg)))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(tag).e("API call failed: ${response.code()} - $errorBody")
                emit(Result.failure(Exception("HTTP ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to send subscription token")
            emit(Result.failure(e))
        }
    }
    
    override suspend fun encryptToken(token: String, apiSecret: String): String {
        return try {
            Timber.tag(tag).d("Encrypting token")
            TokenEncryptionUtil.encryptToken(token, apiSecret)
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Token encryption failed")
            throw e
        }
    }
}
