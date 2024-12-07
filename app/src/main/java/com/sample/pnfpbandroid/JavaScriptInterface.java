package com.sample.pnfpbandroid;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.webkit.JavascriptInterface;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import java.io.IOException;
import java.util.Objects;
import java.security.SecureRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JavaScriptInterface {
    String PNFPB_token;
    String PNFPB_subscription;

    private static final String POST_URL = "https://www.muraliwebworld.com/wp-json/PNFPBpush/v1/subscriptiontoken";
    public String POST_PARAMS = "";

    EncryptedDataHolder encryptedDataHolder;

    JavaScriptInterface(String PNFPB_token_param,String PNFPB_group_subscription,EncryptedDataHolder encrypteddataHolder) {
        PNFPB_token = PNFPB_token_param;
        PNFPB_subscription = PNFPB_group_subscription;
        encryptedDataHolder = encrypteddataHolder;
        //Log.d(TAG, "Frontend subscription ");
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //    encryptedDataHolder = new EncryptedDataHolder(JavaScriptInterface);
        //}
    }
    @JavascriptInterface
    public String getFromAndroid() {
        return PNFPB_token;
    }
    @JavascriptInterface
    public void postMessage(String subscriptionoptions) {
        Log.d(TAG, "Frontend subscription " + subscriptionoptions);
        //return false; // here we return true if we handled the post.

        String token = PNFPB_token;

        // Log and toast
        //String msg = getString(R.string.msg_token_fmt, token);
        String secret = encryptedDataHolder.getApiKey();
        try {

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
            postsubscriptionoptionsRequest(POST_PARAMS,subscriptionoptions);
        } catch (Exception e) {
            Log.d(TAG, String.valueOf(e));
        }

    }

    void postsubscriptionoptionsRequest(String postBody,String subscriptionoptions) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, "Frontend subscription " + subscriptionoptions);
        Log.d(TAG, "token " + PNFPB_token);
        Log.d(TAG, "PNFPB_subscription " + PNFPB_subscription);
        Log.d(TAG, "pnfpbuserid " + PNFPB_subscription);
        // create your json here
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", POST_PARAMS);
            if (Objects.equals(PNFPB_subscription, "subscribe_group") || Objects.equals(PNFPB_subscription, "unsubscribe_group")) {
                jsonObject.put("groupid", subscriptionoptions);
                if (Objects.equals(PNFPB_subscription, "subscribe_group")) {
                    jsonObject.put("subscription-type", "subscribe-group");
                }
                else {
                    jsonObject.put("subscription-type", "unsubscribe-group");
                }
            }
            else {
                jsonObject.put("groupid", "");
                jsonObject.put("subscription-type", "");
            }
            jsonObject.put("cookievalue", "");
            if (Objects.equals(PNFPB_subscription, "frontendsubscriptionOptions")) {
                jsonObject.put("subscriptionoptions", subscriptionoptions);
            }
            else {
                jsonObject.put("subscriptionoptions", "");
            }
            jsonObject.put("cookievalue", "");
            if (Objects.equals(PNFPB_subscription, "pnfpbuserid")) {
                jsonObject.put("userid", Integer.parseInt(subscriptionoptions));
            }
            else {
                jsonObject.put("userid", 0);
            }
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

}