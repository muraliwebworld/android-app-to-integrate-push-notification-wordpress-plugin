package com.sample.pnfpbandroid.util

import android.util.Base64
import timber.log.Timber
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

/**
 * Utility class for token encryption using AES-GCM
 * Matches the encryption standards used by push-notification-for-post-and-buddypress WordPress plugin
 *
 * Encryption Format: "encryptedToken:base64(iv):hmac:hmac"
 */
object TokenEncryptionUtil {
    
    private const val TAG = "TokenEncryption"
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_SIZE = 16 // 12 bytes for GCM, but 16 for compatibility
    private const val GCM_TAG_SIZE = 128 // bits
    
    /**
     * Encrypt token using AES-GCM encryption
     *
     * @param token The plain token to encrypt
     * @param secret The encryption secret key
     * @return Encrypted token string in format: "encryptedToken:iv:hmac:hmac"
     * @throws Exception if encryption fails
     */
    @Throws(Exception::class)
    fun encryptToken(token: String, secret: String): String {
        return try {
            // Generate random IV (Initialization Vector)
            val secureRandom = SecureRandom()
            val iv = ByteArray(GCM_IV_SIZE)
            secureRandom.nextBytes(iv)
            
            // Create AES-GCM cipher
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKeySpec = SecretKeySpec(secret.toByteArray(), 0, secret.length, "AES")
            val gcmSpec = GCMParameterSpec(GCM_TAG_SIZE, iv)
            
            // Encrypt the token
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec)
            val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            
            // Encode as Base64
            val encryptedToken = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
            
            // Calculate HMAC-SHA256 for integrity check
            val hmacSha256 = Mac.getInstance("HmacSHA256")
            hmacSha256.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
            val hmacBytes = hmacSha256.doFinal(token.toByteArray(Charsets.UTF_8))
            
            // Convert HMAC to hex string
            val hmacHex = bytesToHex(hmacBytes)
            
            // Combine all parts
            "$encryptedToken:$ivString:$hmacHex:$hmacHex"
        } catch (e: Exception) {
            Timber.e(e, "Token encryption failed")
            throw e
        }
    }
    
    /**
     * Decrypt token (for testing purposes)
     *
     * @param encryptedTokenString The encrypted token string in format "token:iv:hmac:hmac"
     * @param secret The decryption secret key
     * @return The decrypted plain token
     * @throws Exception if decryption fails
     */
    @Throws(Exception::class)
    fun decryptToken(encryptedTokenString: String, secret: String): String {
        return try {
            val parts = encryptedTokenString.split(":")
            if (parts.size < 2) {
                throw IllegalArgumentException("Invalid encrypted token format")
            }
            
            val encryptedToken = Base64.decode(parts[0], Base64.NO_WRAP)
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            
            // Create cipher for decryption
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKeySpec = SecretKeySpec(secret.toByteArray(), 0, secret.length, "AES")
            val gcmSpec = GCMParameterSpec(GCM_TAG_SIZE, iv)
            
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec)
            val decryptedBytes = cipher.doFinal(encryptedToken)
            
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Token decryption failed")
            throw e
        }
    }
    
    /**
     * Convert byte array to hexadecimal string
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        val hexChars = CharArray(bytes.size * 2)
        
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        
        return String(hexChars)
    }
}
