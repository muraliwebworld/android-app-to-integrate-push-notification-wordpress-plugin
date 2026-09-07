# Push Notification Integration Guide
## Token Encryption & API Parameters

This guide explains how to encrypt the FCM token and send it to the WordPress push-notification-for-post-and-buddypress plugin.

---

## 📋 API Endpoint

**Method**: POST  
**URL**: `https://<your-domain>/wp-json/PNFPBpush/v1/subscriptiontoken`  
**Content-Type**: `application/json`

---

## 🔐 Encryption Process

### Step 1: Understand the Encryption Algorithm

**Algorithm**: AES-256-GCM (Galois/Counter Mode)  
**IV Size**: 16 bytes (randomly generated)  
**Key Size**: 256 bits  
**Authentication Tag**: 128 bits  

### Step 2: Token Encryption Flow

```
Raw FCM Token
    ↓
Generate random 16-byte IV
    ↓
Encrypt token using AES-256-GCM with IV
    ↓
Calculate HMAC-SHA256 of original token
    ↓
Base64 encode encrypted data
    ↓
Base64 encode IV
    ↓
Hex encode HMAC
    ↓
Combine: encryptedData:iv:hmac:hmac
    ↓
Send in API request
```

### Step 3: Code Implementation

#### Java (Old Approach)
```java
String encryptToken(String token, String secret) throws Exception {
    // Generate random IV (16 bytes)
    SecureRandom secureRandom = new SecureRandom();
    byte[] iv = new byte[16];
    secureRandom.nextBytes(iv);
    
    // Create AES-GCM cipher
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "AES");
    GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
    
    // Encrypt the token
    byte[] encryptedBytes = cipher.doFinal(token.getBytes("UTF-8"));
    
    // Base64 encode
    String encryptedToken = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    String ivString = Base64.encodeToString(iv, Base64.NO_WRAP);
    
    // Calculate HMAC-SHA256
    Mac hmacSha256 = Mac.getInstance("HmacSHA256");
    hmacSha256.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
    byte[] hmacBytes = hmacSha256.doFinal(token.getBytes("UTF-8"));
    
    // Convert to hex
    StringBuilder hmacHex = new StringBuilder();
    for (byte b : hmacBytes) {
        hmacHex.append(String.format("%02x", b));
    }
    
    // Combine all parts
    return encryptedToken + ":" + ivString + ":" + hmacHex + ":" + hmacHex;
}
```

#### Kotlin (Modern Approach)
```kotlin
object TokenEncryptionUtil {
    fun encryptToken(token: String, secret: String): String {
        // Generate random IV
        val secureRandom = SecureRandom()
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)
        
        // Create cipher
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), 0, secret.length, "AES")
        val gcmSpec = GCMParameterSpec(128, iv)
        
        // Encrypt
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec)
        val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        
        // Base64 encode
        val encryptedToken = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        
        // Calculate HMAC
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        hmacSha256.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val hmacBytes = hmacSha256.doFinal(token.toByteArray(Charsets.UTF_8))
        val hmacHex = bytesToHex(hmacBytes)
        
        return "$encryptedToken:$ivString:$hmacHex:$hmacHex"
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f')
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}
```

---

## 📤 API Request Parameters

### Complete Request Structure

```json
{
  "token": "encryptedToken:base64(iv):hex(hmac):hex(hmac)",
  "userid": 0,
  "groupid": "",
  "subscription-type": "subscribe-group|unsubscribe-group|",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

### Parameter Details

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `token` | String | ✅ Yes | Encrypted FCM token | `abc123:def456:ghi789:ghi789` |
| `userid` | Integer | ❌ No | WordPress user ID (if logged in) | `123` or `0` |
| `groupid` | String | ❌ No | Group/Category ID to subscribe to | `"cat_5"` or `""` |
| `subscription-type` | String | ❌ No | Type of subscription | `"subscribe-group"`, `"unsubscribe-group"`, or `""` |
| `subscriptionoptions` | String | ❌ No | Additional subscription options | `"option_123"` or `""` |
| `cookievalue` | String | ❌ No | User cookie (legacy, usually empty) | `""` |

---

## 🎯 Common Scenarios

### Scenario 1: Generic Subscription (No user info)

**Request**:
```json
{
  "token": "encryptedToken:base64(iv):hex(hmac):hex(hmac)",
  "userid": 0,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**Code Example (Kotlin)**:
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = 0,
    groupId = "",
    subscriptionType = "",
    subscriptionOptions = "",
    cookieValue = ""
)
```

### Scenario 2: Subscribe to Group

**Request**:
```json
{
  "token": "encryptedToken:base64(iv):hex(hmac):hex(hmac)",
  "userid": 0,
  "groupid": "5",
  "subscription-type": "subscribe-group",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**Code Example (Kotlin)**:
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = 0,
    groupId = "5",
    subscriptionType = SubscriptionType.SUBSCRIBE_GROUP,
    subscriptionOptions = "",
    cookieValue = ""
)
```

### Scenario 3: User-Specific Subscription

**Request**:
```json
{
  "token": "encryptedToken:base64(iv):hex(hmac):hex(hmac)",
  "userid": 42,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**Code Example (Kotlin)**:
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = 42,
    groupId = "",
    subscriptionType = "",
    subscriptionOptions = "",
    cookieValue = ""
)
```

### Scenario 4: Unsubscribe from Group

**Request**:
```json
{
  "token": "encryptedToken:base64(iv):hex(hmac):hex(hmac)",
  "userid": 0,
  "groupid": "5",
  "subscription-type": "unsubscribe-group",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**Code Example (Kotlin)**:
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = 0,
    groupId = "5",
    subscriptionType = SubscriptionType.UNSUBSCRIBE_GROUP,
    subscriptionOptions = "",
    cookieValue = ""
)
```

---

## 🔑 API Secret Key

### Where to Get It

The API secret key is configured in WordPress:

1. Login to WordPress Admin
2. Go to: **Settings → Push Notification for Post and BuddyPress**
3. Find: **"Mobile App Secret Code"** or **"PNFPB_icfcm_integrate_app_secret_code"**
4. Copy the value

### How to Use It

**In EncryptedSharedPreferences**:
```kotlin
val encryptedDataHolder = EncryptedDataHolder(context)
encryptedDataHolder.setApiKey(apiSecret)

// Later, when encrypting:
val apiSecret = encryptedDataHolder.getApiKey()
val encryptedToken = TokenEncryptionUtil.encryptToken(fcmToken, apiSecret)
```

**In BuildConfig**:
```gradle
buildTypes {
    debug {
        buildConfigField "String", "API_SECRET", '"your-secret-key"'
    }
    release {
        buildConfigField "String", "API_SECRET", '"production-secret"'
    }
}
```

### Security Best Practice
- 🔒 Never hardcode the secret in source code
- 🔒 Store it in EncryptedSharedPreferences (recommended)
- 🔒 Use BuildConfig for per-flavor secrets
- 🔒 Fetch from a secure backend if possible

---

## 🚀 Complete Integration Example

### Step 1: Get FCM Token
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val fcmToken = task.result
        encryptedDataHolder.setApiKey(apiSecret)
        // Now ready to encrypt and send
    }
}
```

### Step 2: Encrypt Token
```kotlin
lifecycleScope.launch {
    try {
        val encryptedToken = subscriptionRepository.encryptToken(
            token = fcmToken,
            apiSecret = apiSecret
        )
        // Token encrypted successfully
    } catch (e: Exception) {
        Log.e("Encryption", "Failed: ${e.message}")
    }
}
```

### Step 3: Build Request
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = getCurrentUserId(),  // 0 if anonymous
    groupId = selectedGroupId,     // "" if not subscribing to group
    subscriptionType = when {
        isSubscribingToGroup -> SubscriptionType.SUBSCRIBE_GROUP
        isUnsubscribingFromGroup -> SubscriptionType.UNSUBSCRIBE_GROUP
        else -> ""
    },
    subscriptionOptions = options,
    cookieValue = ""
)
```

### Step 4: Send Request
```kotlin
lifecycleScope.launch {
    subscriptionRepository.sendSubscriptionToken(request).collect { result ->
        result.onSuccess { message ->
            Log.i("Success", "Subscription updated: $message")
            showMessage("Subscription successful!")
        }
        result.onFailure { exception ->
            Log.e("Error", "Failed: ${exception.message}")
            showError("Subscription failed: ${exception.message}")
        }
    }
}
```

---

## 🔍 Server-Side Validation (PHP)

This is what the WordPress plugin does:

```php
// Extract encrypted token parts
$pnfpb_parts = explode(":", $pnfpb_encrypted);

// Decrypt using AES-256-GCM
$pnfpb_ciphertext = base64_decode($pnfpb_parts[0]);
$pnfpb_iv = base64_decode($pnfpb_parts[1]);
$pnfpb_tag = substr($pnfpb_ciphertext, -16);
$pnfpb_ciphertext = substr($pnfpb_ciphertext, 0, -16);

$pnfpb_decrypted = openssl_decrypt(
    $pnfpb_ciphertext,
    "aes-256-gcm",
    $pnfpb_encryption_key,
    OPENSSL_RAW_DATA,
    $pnfpb_iv,
    $pnfpb_tag
);

// Verify HMAC
$pnfpb_hmac = hash_hmac("sha256", $pnfpb_decrypted, $pnfpb_encryption_key);
if ($pnfpb_hmac !== $pnfpb_parts[3]) {
    // HMAC verification failed
    return error_response(401, "Invalid signature");
}

// Store the token
$pnfpb_table = $wpdb->prefix . "pnfpb_ic_subscribed_deviceids_web";
$wpdb->insert($pnfpb_table, [
    'device_id' => $pnfpb_decrypted,
    'user_id' => $pnfpb_userid,
    // ... other fields
]);
```

---

## 📝 Troubleshooting

### Issue: "Invalid encryption format"
**Cause**: Encrypted token format is wrong  
**Solution**: Ensure format is `encryptedData:iv:hmac:hmac` with all parts Base64/Hex encoded

### Issue: "HMAC verification failed" (401)
**Cause**: HMAC doesn't match on server  
**Solution**:
- Verify API secret is correct
- Check token encoding (UTF-8)
- Verify HMAC calculation uses original token, not encrypted

### Issue: "Invalid data" (401)
**Cause**: Decryption failed  
**Solution**:
- Check API secret matches server config
- Verify IV is 16 bytes
- Ensure encryption algorithm is AES-256-GCM

### Issue: "User not found"
**Cause**: Invalid userid sent  
**Solution**:
- Send `userid: 0` for anonymous users
- Send valid WordPress user ID for logged-in users

---

## 🧪 Testing Your Integration

### Test with Curl
```bash
curl -X POST https://yoursite.com/wp-json/PNFPBpush/v1/subscriptiontoken \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your_encrypted_token:base64_iv:hex_hmac:hex_hmac",
    "userid": 0,
    "groupid": "",
    "subscription-type": "",
    "subscriptionoptions": "",
    "cookievalue": ""
  }'
```

### Test in Android
```kotlin
// Add debug logging
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken.also { 
        Log.d("API", "Encrypted token: $it") 
    },
    userId = 0,
    groupId = "",
    subscriptionType = "",
    subscriptionOptions = "",
    cookieValue = ""
)

// Send and check response
subscriptionRepository.sendSubscriptionToken(request).collect { result ->
    result.onSuccess { message ->
        Log.i("API", "Success: $message")
    }
    result.onFailure { error ->
        Log.e("API", "Error: ${error.message}", error)
    }
}
```

---

## 📚 Related Resources

- **TokenEncryptionUtil.kt**: Handles all encryption logic
- **SubscriptionTokenRequest.kt**: Data model for API request
- **PushNotificationApiService.kt**: Retrofit API interface
- **SubscriptionRepositoryImpl.kt**: Business logic layer

---

## 🎓 Next Steps

1. ✅ Understand encryption algorithm (AES-256-GCM)
2. ✅ Get API secret from WordPress
3. ✅ Implement token encryption
4. ✅ Build request with all parameters
5. ✅ Send to API endpoint
6. ✅ Handle response and errors
7. ✅ Test with different scenarios

---

**For detailed implementation examples, see:**
- `BEFORE_AND_AFTER.md` - Code comparisons
- `QUICK_START.md` - Quick integration guide
- `MODERNIZATION_GUIDE.md` - Complete guide
