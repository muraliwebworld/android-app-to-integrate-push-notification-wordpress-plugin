# Visual Integration Reference Guide
## Token Encryption Flow & API Parameters

---

## 🔄 Complete Encryption & API Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    ANDROID APP                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌────────────────────────────────────┐
        │   Firebase Cloud Messaging         │
        │   Gets FCM Token                   │
        │  (e.g., dHlXa4fK2n9...)            │
        └────────────────────────────────────┘
                              │
                              ▼
        ┌────────────────────────────────────┐
        │   Retrieve API Secret              │
        │   From EncryptedSharedPreferences   │
        │  (e.g., my-secret-key-123)         │
        └────────────────────────────────────┘
                              │
                ┌─────────────┴─────────────┐
                │   TOKEN ENCRYPTION        │
                │   (AES-256-GCM)           │
                └─────────────┬─────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
  │ Generate IV  │    │ Encrypt      │    │ Calculate    │
  │ (16 bytes)   │    │ Token with   │    │ HMAC-SHA256  │
  │              │    │ AES-256-GCM  │    │              │
  └──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                ┌─────────────┴─────────────┐
                │  ENCODE RESULTS           │
                │  - Base64(IV)             │
                │  - Base64(Ciphertext)     │
                │  - Hex(HMAC)              │
                └─────────────┬─────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
  │encryptedData │    │   base64IV   │    │   hexHmac    │
  │              │    │              │    │              │
  └──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                ┌─────────────▼─────────────┐
                │  COMBINE: format =        │
                │  "data:iv:hmac:hmac"      │
                └─────────────┬─────────────┘
                              │
                ┌─────────────▼──────────────────────┐
                │  BUILD JSON REQUEST                │
                │  {                                 │
                │    "token": "data:iv:hmac:hmac",  │
                │    "userid": 0,                    │
                │    "groupid": "",                  │
                │    "subscription-type": "",        │
                │    "subscriptionoptions": "",      │
                │    "cookievalue": ""               │
                │  }                                 │
                └─────────────┬──────────────────────┘
                              │
                ┌─────────────▼──────────────────────┐
                │  SEND POST REQUEST                 │
                │  POST /wp-json/PNFPBpush/v1/       │
                │       subscriptiontoken            │
                └─────────────┬──────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                  WORDPRESS SERVER                                │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │      SERVER-SIDE PROCESSING              │
        └─────────────────────┬─────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
  │ Decrypt      │    │ Verify       │    │ Extract      │
  │ AES-256-GCM  │    │ HMAC-SHA256  │    │ Token        │
  │              │    │              │    │              │
  └──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │   Store in Database                      │
        │   pnfpb_ic_subscribed_deviceids_web      │
        └─────────────────────┬─────────────────────┘
                              │
        ┌─────────────────────▼──────────────────────┐
        │  SEND JSON RESPONSE                        │
        │  {                                         │
        │    "status": 200,                          │
        │    "message": "Subscription successful"    │
        │  }                                         │
        └─────────────────────┬──────────────────────┘
                              │
                ┌─────────────▼─────────────┐
                │  ANDROID APP              │
                │  Receives 200 Response    │
                │  Subscription Complete!   │
                └───────────────────────────┘
```

---

## 📊 Parameter Mapping Table

### Request Parameters → JavaScript Bridge → API Request

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         PARAMETER MAPPING                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  JAVASCRIPT/WEB                  ANDROID CODE                            │
│  ──────────────────────          ──────────────                         │
│                                                                           │
│  Android.postMessage(            SubscriptionTokenRequest(               │
│    subscriptionOptions,            encryptedToken = "...",              │
│    subscriptionType              groupId = subscriptionOptions,          │
│  )                                 subscriptionType = subscriptionType   │
│                                  )                                        │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Complete Parameter Reference

```
┌──────────────────┬──────────┬──────────┬──────────────────────────────────┐
│ Parameter        │ Type     │ Required │ Description & Examples            │
├──────────────────┼──────────┼──────────┼──────────────────────────────────┤
│                  │          │          │                                  │
│ token            │ String   │   YES    │ Encrypted FCM token              │
│                  │          │          │ Format:                          │
│                  │          │          │ "data:iv:hmac:hmac"              │
│                  │          │          │ Example:                         │
│                  │          │          │ "abc123:def456:ghi789:ghi789"    │
│                  │          │          │                                  │
├──────────────────┼──────────┼──────────┼──────────────────────────────────┤
│                  │          │          │                                  │
│ userid           │ Integer  │    NO    │ WordPress user ID                │
│                  │          │          │ 0 = Anonymous user              │
│                  │          │          │ >0 = Logged-in user              │
│                  │          │          │ Example: 0, 42, 123             │
│                  │          │          │                                  │
├──────────────────┼──────────┼──────────┼──────────────────────────────────┤
│                  │          │          │                                  │
│ groupid          │ String   │    NO    │ Category/Group ID                │
│                  │          │          │ "" = No specific group           │
│                  │          │          │ Example: "5", "cat_news", ""     │
│                  │          │          │                                  │
├──────────────────┼──────────┼──────────┼──────────────────────────────────┤
│                  │          │          │                                  │
│ subscription-    │ String   │    NO    │ Subscription action type         │
│ type             │          │          │ Options:                         │
│                  │          │          │ - "subscribe-group"              │
│                  │          │          │ - "unsubscribe-group"            │
│                  │          │          │ - "" (generic/no type)           │
│                  │          │          │                                  │
├──────────────────┼──────────┼──────────┼──────────────────────────────────┤
│                  │          │          │                                  │
│ subscription     │ String   │    NO    │ Additional options               │
│ options          │          │          │ "" = No additional options       │
│                  │          │          │ Example: "option_123"            │
│                  │          │          │                                  │
├──────────────────┼──────────┼──────────┼──────────────────────────────────┤
│                  │          │          │                                  │
│ cookievalue      │ String   │    NO    │ Legacy/Reserved field            │
│                  │          │          │ Usually: "" (empty)              │
│                  │          │          │                                  │
└──────────────────┴──────────┴──────────┴──────────────────────────────────┘
```

---

## 🎯 Usage Scenarios with Parameter Examples

### Scenario 1: Generic Subscription (No User/Group)

```json
POST /wp-json/PNFPBpush/v1/subscriptiontoken
Content-Type: application/json

{
  "token": "encrypted_token:base64_iv:hex_hmac:hex_hmac",
  "userid": 0,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**JavaScript**:
```javascript
Android.postMessage("", "");
```

**Server Response**:
```json
{
  "status": 200,
  "message": "Subscription successful"
}
```

---

### Scenario 2: Subscribe to Category #5

```json
POST /wp-json/PNFPBpush/v1/subscriptiontoken
Content-Type: application/json

{
  "token": "encrypted_token:base64_iv:hex_hmac:hex_hmac",
  "userid": 0,
  "groupid": "5",
  "subscription-type": "subscribe-group",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**JavaScript**:
```javascript
Android.postMessage("5", "subscribe-group");
```

**Android Kotlin**:
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = 0,
    groupId = "5",
    subscriptionType = "subscribe-group"
)
```

---

### Scenario 3: User-Specific Subscription

```json
POST /wp-json/PNFPBpush/v1/subscriptiontoken
Content-Type: application/json

{
  "token": "encrypted_token:base64_iv:hex_hmac:hex_hmac",
  "userid": 42,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**Android Kotlin**:
```kotlin
val request = SubscriptionTokenRequest(
    encryptedToken = encryptedToken,
    userId = 42,
    groupId = "",
    subscriptionType = ""
)
```

**Note**: `userid` should be the WordPress user ID (obtainable via `get_current_user_id()` in PHP or passed from JavaScript)

---

### Scenario 4: Unsubscribe from Category

```json
POST /wp-json/PNFPBpush/v1/subscriptiontoken
Content-Type: application/json

{
  "token": "encrypted_token:base64_iv:hex_hmac:hex_hmac",
  "userid": 0,
  "groupid": "5",
  "subscription-type": "unsubscribe-group",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

**JavaScript**:
```javascript
Android.postMessage("5", "unsubscribe-group");
```

---

## 🔐 Encryption Step-by-Step

### Step 1: Prepare Inputs

```
Input:
├─ FCM Token: "c1N9_vR2X..."
├─ API Secret: "my-secret-key"
└─ IV: (auto-generated, 16 bytes)
```

### Step 2: Encryption

```
1. Generate random IV (16 bytes)
   └─ IV = [0x12, 0x34, 0x56, ..., 0xAB]

2. Create AES cipher with algorithm "AES/GCM/NoPadding"
   └─ Key = "my-secret-key"
   └─ IV = generated above

3. Encrypt FCM token
   └─ Input: "c1N9_vR2X..."
   └─ Output: encrypted bytes [0xA1, 0xB2, 0xC3, ...]
   └─ Tag: [0xD4, 0xE5, 0xF6, ...]
```

### Step 3: Encoding

```
Base64 encode (No padding):
├─ Encrypted data → "abc123def456"
└─ IV → "ghi789jkl012"

Hex encode:
├─ HMAC-SHA256(token) → "mno345pqr678stu901vwx234yz"
└─ Repeat for 2nd HMAC → "mno345pqr678stu901vwx234yz"
```

### Step 4: Combine

```
Final token format:
"abc123def456:ghi789jkl012:mno345pqr678stu901vwx234yz:mno345pqr678stu901vwx234yz"

Parts:
├─ [0] = Base64(encrypted ciphertext with auth tag)
├─ [1] = Base64(IV)
├─ [2] = Hex(HMAC-SHA256)
└─ [3] = Hex(HMAC-SHA256) [duplicate]
```

---

## 🧪 Testing Checklist by Scenario

### Test Generic Subscription

```
Input:
  └─ groupid: ""
  └─ subscription-type: ""
  └─ userid: 0

Expected Output:
  └─ Status: 200
  └─ Message: "Subscription successful"

Verify:
  ✓ Token is stored in database
  ✓ Device is marked as active
  ✓ No specific group filtering applied
```

---

### Test Group Subscription

```
Input:
  └─ groupid: "5"
  └─ subscription-type: "subscribe-group"
  └─ userid: 0

Expected Output:
  ├─ Status: 200
  └─ Message: "Group subscription successful"

Verify:
  ✓ Token stored with groupid = 5
  ✓ Device receives only category 5 notifications
  ✓ Device does NOT receive other categories
```

---

### Test Unsubscribe

```
Input:
  └─ groupid: "5"
  └─ subscription-type: "unsubscribe-group"
  └─ userid: 0

Expected Output:
  ├─ Status: 200
  └─ Message: "Unsubscribe successful"

Verify:
  ✓ Token removed from category 5
  ✓ Token stays subscribed to other categories
  ✓ Device stops receiving category 5 notifications
```

---

### Test User Subscription

```
Input:
  └─ userid: 42
  └─ groupid: ""
  └─ subscription-type: ""

Expected Output:
  ├─ Status: 200
  └─ Message: "Subscription successful"

Verify:
  ✓ Token linked to WordPress user 42
  ✓ User-specific notifications work
  ✓ Device receives user's notifications only
```

---

## 🔍 Debugging: Parameter Flow

### Example: Subscribe to Category 5

```
┌─────────────────────────────────┐
│  JavaScript in WebView          │
│  Android.postMessage("5", ...)  │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│  JavaScriptInterfaceModern      │
│  postMessage("5", "sub-group")  │
└────────────────┬────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│  buildSubscriptionRequest()           │
│  groupId = "5"                        │
│  subscriptionType = "subscribe-group" │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────┐
│  SubscriptionTokenRequest object         │
│  {                                       │
│    encryptedToken: "...",                │
│    groupId: "5",                         │
│    subscriptionType: "subscribe-group"   │
│  }                                       │
└────────────────┬─────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Retrofit converts to JSON               │
│  {                                      │
│    "token": "...",                      │
│    "userid": 0,                         │
│    "groupid": "5",                      │
│    "subscription-type": "subscribe-...  │
│  }                                      │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  POST to API                            │
│  /wp-json/PNFPBpush/v1/subscriptiontoken│
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  WordPress Server                       │
│  - Decrypts token                       │
│  - Verifies HMAC                        │
│  - Stores in DB with groupid=5          │
│  - Returns 200 status                   │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Android App                            │
│  Shows success message                  │
│  Subscription complete!                 │
└─────────────────────────────────────────┘
```

---

## 📱 Quick Reference Card

### Encryption Formula
```
Encrypted Token = "Base64(Ciphertext) : Base64(IV) : Hex(HMAC) : Hex(HMAC)"
                   └─ AES-256-GCM ────   └─ 16 bytes  └─ SHA256 ──┘
```

### API Endpoint
```
POST /wp-json/PNFPBpush/v1/subscriptiontoken
Content-Type: application/json
```

### Minimum Valid Request
```json
{
  "token": "required",
  "userid": 0,
  "groupid": "",
  "subscription-type": "",
  "subscriptionoptions": "",
  "cookievalue": ""
}
```

### Success Response
```json
{
  "status": 200,
  "message": "Subscription successful"
}
```

### Error Response
```json
{
  "status": 401,
  "message": "failed - invalid data"
}
```

---

**For detailed code examples, see TOKEN_ENCRYPTION_GUIDE.md**  
**For demo app usage, see DEMO_APP_GUIDE.md**
