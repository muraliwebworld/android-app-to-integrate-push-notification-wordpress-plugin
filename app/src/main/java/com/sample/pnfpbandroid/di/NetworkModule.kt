package com.sample.pnfpbandroid.di

import com.google.gson.GsonBuilder
import com.sample.pnfpbandroid.BuildConfig
import com.sample.pnfpbandroid.EncryptedDataHolder
import com.sample.pnfpbandroid.data.api.PushNotificationApiService
import com.sample.pnfpbandroid.data.repository.SubscriptionRepositoryImpl
import com.sample.pnfpbandroid.domain.repository.SubscriptionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

/**
 * Hilt module for dependency injection configuration
 * Provides singleton instances for API services and HTTP client
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val CONNECTION_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    /**
     * Provides OkHttpClient with logging and timeout configurations
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }
        
        loggingInterceptor.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * Provides Retrofit instance configured with Gson converter
     *
     * @param baseUrl The base URL for the API
     * @param okHttpClient The OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val gson = GsonBuilder()
            .serializeNulls()
            .create()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Provides base URL for the API
     * Configured via BuildConfig.API_BASE_URL from build.gradle
     */
    @Provides
    @Singleton
    fun provideBaseUrl(): String {
        return BuildConfig.API_BASE_URL
    }
    
    /**
     * Provides PushNotificationApiService instance
     */
    @Provides
    @Singleton
    fun providePushNotificationApiService(retrofit: Retrofit): PushNotificationApiService {
        return retrofit.create(PushNotificationApiService::class.java)
    }
    
    /**
     * Provides SubscriptionRepository implementation
     */
    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        apiService: PushNotificationApiService
    ): SubscriptionRepository {
        return SubscriptionRepositoryImpl(apiService)
    }
}

/**
 * Module for secure data storage (EncryptedSharedPreferences)
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideEncryptedDataHolder(): EncryptedDataHolderProvider {
        return EncryptedDataHolderProvider()
    }
}

/**
 * Simple provider for EncryptedDataHolder (Hilt-compatible)
 * Usage: Inject this and call getEncryptedDataHolder(context)
 */
class EncryptedDataHolderProvider {
    private val holders = mutableMapOf<String, EncryptedDataHolder>()
    
    fun getEncryptedDataHolder(context: android.content.Context): EncryptedDataHolder {
        // Use package name as key to ensure single instance
        val key = context.packageName
        
        return holders.getOrPut(key) {
            EncryptedDataHolder(context)
        }
    }
}
