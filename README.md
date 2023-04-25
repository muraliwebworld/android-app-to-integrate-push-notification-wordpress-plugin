#  Android JAVA app code to integrate with PNFPB WordPress plugin - Push notification for Post and BuddyPress<br/>
Android app sample code in JAVA to integrate Android mobile app with WordPress push notification plugin - PNFPB Push notification for Post and BuddyPress.
PNFPB plugin is designed to send push notifications using Firebase Cloud Messaging (FCM) to websites, Android/iOS mobile apps. This plugin has REST API facility to integrate with native/hybrid Android/iOS mobile apps for push notifications. <br/><br/>

# Download Push notification plugin<br/>
https://wordpress.org/plugins/push-notification-for-post-and-buddypress/<br/><br/>
It sends notification whenever new WordPress post, custom post types,new BuddyPress activities,comments published. It has facility to generate PWA - Progressive Web App. This plugin is able to send push notification to more than 200,000 subscribers unlimited push notifications using background action scheduler.

# PNFPB plugin REST API for Android App<br/>
REST API to connect mobile native/hybrid apps to send push notification from WordPress site to both mobile apps and WordPress sites.
Using this REST API WordPress site gets Firebase Push Notification subscription token from Mobile app(Android/Ios). 
This allows to send push notifications to WordPress site users as well as to Native mobile app Android/ios users.
REST API url is https:/<domain>/wp-json/PNFPBpush/v1/subscriptiontoken

# Integrate Native mobile apps like mobile app with this WordPress plugin<br />
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

# Video tutorial showing how to configure Firebase for this plugin<br />
	
https://www.youtube.com/watch?v=02oymYLt3qo <br />
	
