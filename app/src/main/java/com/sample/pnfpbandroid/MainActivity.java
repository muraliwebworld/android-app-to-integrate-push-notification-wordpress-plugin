package com.sample.pnfpbandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private WebView mywebView;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_REQUEST_CODE = 1234;
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String POST_URL = "https://www.muraliwebworld.com/wp-json/PNFPBpush/v1/subscriptiontoken";
    public String POST_PARAMS = "";
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    public String intentUrl = "https://www.muraliwebworld.com";

    EncryptedDataHolder encryptedDataHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = this.getIntent();
        Log.d(TAG, "test-------->");
        Log.d(TAG, intent.toString());
        Bundle extras = getIntent().getExtras();

        Log.d(TAG, "extras");
        if(extras != null){
            Log.d(TAG, extras.toString());
            if(extras.containsKey("URL"))
            {
                intentUrl = extras.getString("URL");
            }
        }

        super.onCreate(savedInstanceState);

        //ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encryptedDataHolder = new EncryptedDataHolder(this);
        }
        //Your security key generated from PNFPB plugin under mobile app admin settings tab
        encryptedDataHolder.setApiKey("4344353432343xfd");

        mywebView=(WebView) findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress);
        swipeRefreshLayout = findViewById(R.id.swipe);




        mywebView.setWebViewClient(new myWebViewClient());

        if (intentUrl != null) {
            mywebView.loadUrl(intentUrl);
        } else {
            intentUrl = "https://www.muraliwebworld.com/";
            mywebView.loadUrl("https://www.muraliwebworld.com/");
        }


        WebSettings webSettings=mywebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mywebView.getSettings().setDomStorageEnabled(true);
        mywebView.getSettings().setMinimumFontSize(1);
        mywebView.getSettings().setMinimumLogicalFontSize(1);
        mywebView.setClickable(true);
        send_Firebase_tokens_tobackend();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        mywebView.loadUrl(intentUrl);
                        send_Firebase_tokens_tobackend();
                    }
                },  3000);
            }
        });

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_orange_dark),
                getResources().getColor(android.R.color.holo_green_dark),
                getResources().getColor(android.R.color.holo_red_dark)
        );
        mywebView.setWebChromeClient(new WebChromeClient() {
            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }


        // [END handle_data_extras]

        askNotificationPermission();
    }

    public void  send_Firebase_tokens_tobackend() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d("token",token);
                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        String secret = encryptedDataHolder.getApiKey();
                        try {
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
                            /*** old method of encrypting using PKCS5PADDING CBC */
                           /*final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                            byte[] tokenbyte_string = token.getBytes();
                            byte[] iv = new byte[16];
                            final IvParameterSpec ivspec = new IvParameterSpec(iv);
                            final SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
                            cipher.init(cipher.ENCRYPT_MODE, secretKey, ivspec);
                            final byte[] finalresult = cipher.doFinal(tokenbyte_string);
                            final String finalresultstring = Base64.encodeToString(finalresult, Base64.NO_WRAP);
                            sha256_HMAC.init(secretKey);
                            byte[] hmacstring1 = sha256_HMAC.doFinal(token.getBytes());
                            StringBuilder byteContent = new StringBuilder();
                            for(byte b: hmacstring1){
                                byteContent.append(String.format("%02x",b));
                            }*/
                            String ivstring = Base64.encodeToString(iv, Base64.NO_WRAP);
                            POST_PARAMS = finalresultstring + ":" + ivstring + ":" + byteContent + ":" + byteContent;
                            postRequest(POST_PARAMS);
                            WebSettings settings = mywebView.getSettings();
                            settings.setJavaScriptEnabled(true);
                            settings.setDomStorageEnabled(true);
                            mywebView.addJavascriptInterface(new JavaScriptInterface(token,"",encryptedDataHolder), "Android");
                            mywebView.addJavascriptInterface(new JavaScriptInterface(token,"subscribe_group",encryptedDataHolder), "subscribeGroupid");
                            mywebView.addJavascriptInterface(new JavaScriptInterface(token,"unsubscribe_group",encryptedDataHolder), "unsubscribeGroupid");
                            mywebView.addJavascriptInterface(new JavaScriptInterface(token,"frontendsubscriptionOptions",encryptedDataHolder), "frontendsubscriptionOptions");
                            mywebView.addJavascriptInterface(new JavaScriptInterface(token,"pnfpbuserid",encryptedDataHolder), "pnfpbuserid");
                            mywebView.loadUrl("javascript:PNFPB_from_Java_androidapp(token)");
                            Log.d("token",token);
                        } catch (Exception e) {
                            Log.d(TAG, String.valueOf(e));
                        }
                    }
                });

    }

    void postRequest(String postBody) throws IOException {

        OkHttpClient client = new OkHttpClient();


        // create your json here
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", POST_PARAMS);
            jsonObject.put("subscription-type", "");
            jsonObject.put("groupid", "");
            jsonObject.put("cookievalue", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        // put your json here
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(POST_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
            }
        });
    }



    public class myWebViewClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
            mywebView.loadUrl("file:///android_asset/lost.html");
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.cancel();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            //send_Firebase_tokens_tobackend();
        }
    }

    @Override
    public void onBackPressed() {
        if (mywebView.canGoBack()) {
            mywebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK) && mywebView.canGoBack()) {
            mywebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }


    }



    // [START ask_post_notifications]
    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(MainActivity.this, "Notification Permission granted", Toast.LENGTH_SHORT).show();
                    // FCM SDK (and your app) can post notifications.
                    send_Firebase_tokens_tobackend();
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                    Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            });

    private void askNotificationPermission() {
        // Toast.makeText(MainActivity.this, "askNotificationPermission", Toast.LENGTH_SHORT).show();
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
                send_Firebase_tokens_tobackend();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        else
        {
            // Directly ask for the permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

    }
    // [END ask_post_notifications]
}