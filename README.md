#  Android JAVA app code to integrate with PNFPB WordPress plugin - Push notification for Post and BuddyPress<br/>
Android app sample code in JAVA to integrate Android mobile app with WordPress push notification plugin - PNFPB Push notification for Post and BuddyPress.
PNFPB plugin is designed to send push notifications using Firebase Cloud Messaging (FCM) to websites, Android/iOS mobile apps. This plugin has REST API facility to integrate with native/hybrid Android/iOS mobile apps for push notifications. <br/><br/>

### Download Push notification plugin from WordPress.org repository<br/>
https://wordpress.org/plugins/push-notification-for-post-and-buddypress/<br/><br/>
It sends notification whenever new WordPress post, custom post types,new BuddyPress activities,comments published. It has facility to generate PWA - Progressive Web App. This plugin is able to send push notification to more than 200,000 subscribers unlimited push notifications using background action scheduler.

## PNFPB plugin REST API to integrate with Android App<br/>
REST API to connect mobile native/hybrid apps to send push notification from WordPress site to both mobile apps and WordPress sites.
Using this REST API WordPress site gets Firebase Push Notification subscription token from Mobile app(Android/Ios). 
This allows to send push notifications to WordPress site users as well as to Native mobile app Android/ios users.
REST API url is https:/<domain>/wp-json/PNFPBpush/v1/subscriptiontoken

### Integrate Native mobile apps like mobile app with this WordPress plugin<br />
New API to send push notification subscription from Native mobile apps like mobile app to WordPress backend and to send push notifications from WordPress to Native mobile app using Firebase.
1. Generate secret key in mobile app tab to communicate between mobile app(in Integrate app api tab plugin settings)
2. REST api to send subscription token from Mobile app using WebView to this WordPress plugin to store it in WordPress db to send push notification whenever new activities/post are published.

Note:- All REST api code is already included in the code, below is only for reference as guide,

REST API using POST method, to send push notification in secured way using AES 256 cryptography encryption method to avoid spams

REST API url post method to send push notification
https://domainname.com/wp-json/PNFPBpush/v1/subscriptiontoken

Input parameters in body in http post method in mobile APP,
token – it should be encrypted according to AES 256 cryptography standards,


Using secret key generated from step 1, enter secret key in mobile app code

store token in global variable for other user
Generate envrypted token as mentioned below using below coding (AES 256 cryptography encryption)
Once plugin receives this token, it will unencrypt using the secret key generate and compare hash code to confirm it is sent from mobile app

Firebase httpv1 version requires separate intent filter, please use intent filter for mainactivity like below

```XML
        <activity
                android:name=".MainActivity"
                android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="OPEN_MAIN_ACTIVITY" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
```
Following is optional, Google requires encryption needs to be AES/GCM/NoPadding, Following is updated encryption logic using AES/GCM/NoPadding, at present plugin works for both old encryption method (AES/CBC/PKCS5Padding) as well as for new method AES/GCM/NoPadding.

Following is example code encryption using AES/GCM/NoPadding to connect to PNFPB plugin REST API

```JAVA
    /*** NEW method of encrypting using AES GCM Nopadding - 2024 */
     SecureRandom secureRandom = new SecureRandom();
     byte[] iv = new byte[16]; // GCM mode typically uses a 12-byte IV
     secureRandom.nextBytes(iv);
     IvParameterSpec ivSpec = new IvParameterSpec(iv);
     // Create an AES key from the secret
     SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "AES");
     // Initialize Cipher in AES/GCM/NoPadding mode
     Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
     cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
     // Encrypt the token
     byte[] encryptedToken = cipher.doFinal(token.getBytes("UTF-8"));
     String finalresultstring = Base64.encodeToString(encryptedToken, Base64.NO_WRAP);
     String ivString = Base64.encodeToString(iv, Base64.NO_WRAP);
     // HMAC calculation for integrity check (if needed)
     Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
     sha256_HMAC.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
     byte[] hmacBytes = sha256_HMAC.doFinal(token.getBytes("UTF-8"));
     StringBuilder byteContent = new StringBuilder();
     for (byte b : hmacBytes)
     {
        byteContent.append(String.format("%02x", b));
     }
     POST_PARAMS = finalresultstring + ":" + ivstring + ":" + byteContent + ":" + byteContent;
     postRequest(POST_PARAMS);
     WebSettings settings = mywebView.getSettings();
```

### Video tutorial showing how to configure Firebase for this plugin<br />

https://youtu.be/T07qpqao_-E?si=LX1pAl1ZHCiyn4Fi <br/>
	
https://www.youtube.com/watch?v=02oymYLt3qo <br />
	
