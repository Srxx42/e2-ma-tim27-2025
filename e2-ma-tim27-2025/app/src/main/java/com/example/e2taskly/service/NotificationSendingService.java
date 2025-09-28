package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.e2taskly.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationSendingService {
    private static final String TAG = "NotificationService";
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/e2taskly-33/messages:send";
    private static String accessToken;

    private final Context context;
    private final RequestQueue requestQueue;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NotificationSendingService(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(this.context);
    }

    public void sendFcmNotification(String receiverToken, JSONObject notificationPayload, JSONObject dataPayload) {
        getAccessToken().addOnSuccessListener(token -> {
            try {
                JSONObject messageJson = new JSONObject();
                messageJson.put("token", receiverToken);
                messageJson.put("notification", notificationPayload);
                messageJson.put("data", dataPayload);

                JSONObject mainJson = new JSONObject();
                mainJson.put("message", messageJson);

                sendFcmRequest(mainJson, token);
            } catch (JSONException e) {
                Log.e(TAG, "Error creating notification JSON", e);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Access Token for sending notification", e);
        });
    }

    public void sendFcmRequest(JSONObject mainBody, String token) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_API_URL, mainBody,
                response -> Log.d(TAG, "FCM Notification sent successfully: " + response.toString()),
                error -> {
                    Log.e(TAG, "FCM Notification send error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Error Data: " + new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public Task<String> getAccessToken() {
        if (accessToken != null) {
            return Tasks.forResult(accessToken);
        }
        return Tasks.call(executor, () -> {
            try {
                InputStream serviceAccountStream = context.getResources().openRawResource(R.raw.service_account);
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                AccessToken token = credentials.getAccessToken();
                accessToken = token.getTokenValue();
                return accessToken;
            } catch (Exception e) {
                Log.e(TAG, "Error generating access token", e);
                throw e;
            }
        });
    }
}