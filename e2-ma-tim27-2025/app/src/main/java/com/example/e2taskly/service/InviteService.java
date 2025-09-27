package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.e2taskly.R; // Važno: Import za R.raw.service_account
import com.example.e2taskly.data.repository.InviteRepository;
import com.example.e2taskly.data.repository.UserRepository;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.AllianceInvite;
import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InviteService {
    private static final String TAG = "InviteService";
    private final InviteRepository inviteRepository;
    private final Context context;
    private static String accessToken;
    private final RequestQueue requestQueue;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public InviteService(Context context) {
        this.inviteRepository = new InviteRepository(context);
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(this.context);
    }

    public Task<Void> sendInvites(String inviterId, String inviterUsername, Alliance alliance, List<User> friendsToInvite) {
        Map<String, String> friendIdToInviteIdMap = new HashMap<>();
        for (User friend : friendsToInvite) {
            friendIdToInviteIdMap.put(friend.getUid(), UUID.randomUUID().toString());
        }

        return inviteRepository.sendInvites(inviterId, inviterUsername, alliance, friendsToInvite, friendIdToInviteIdMap)
                .onSuccessTask(aVoid -> {
                    for (User friend : friendsToInvite) {
                        if (friend.getFcmToken() != null && !friend.getFcmToken().isEmpty()) {
                            Log.d("InviteService", "Preparing to send notification to: " + friend.getUsername());
                            Log.d("InviteService", "Target FCM Token: " + friend.getFcmToken());
                            String inviteId = friendIdToInviteIdMap.get(friend.getUid());
                            sendFcmNotification(friend.getFcmToken(), inviterUsername, alliance, inviteId);
                        }
                    }
                    return Tasks.forResult(null);
                });
    }

    private void sendFcmNotification(String receiverToken, String inviterUsername, Alliance alliance, String inviteId) {
        getAccessToken().addOnSuccessListener(token -> {
            try {
                JSONObject notificationJson = new JSONObject();
                notificationJson.put("title", "Alliance Invitation");
                notificationJson.put("body", inviterUsername + " has invited you to join " + alliance.getName());

                JSONObject dataJson = new JSONObject();
                dataJson.put("type", "INVITATION");
                dataJson.put("inviteId", inviteId);
                dataJson.put("allianceId", alliance.getAllianceId());
                dataJson.put("allianceName", alliance.getName());
                dataJson.put("inviterUsername", inviterUsername);

                JSONObject messageJson = new JSONObject();
                messageJson.put("token", receiverToken);
                messageJson.put("notification", notificationJson);
                messageJson.put("data", dataJson);

                JSONObject mainJson = new JSONObject();
                mainJson.put("message", messageJson);

                sendFcmRequest(mainJson, token);

            } catch (Exception e) {
                Log.e(TAG, "Error creating V1 notification JSON", e);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Access Token for sending notification", e);
        });
    }

    private void sendFcmRequest(JSONObject mainBody, String token) {
        String projectId = "e2taskly-33";
        String fcmApiUrl = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, fcmApiUrl, mainBody,
                response -> Log.d(TAG, "V1 Notification sent successfully: " + response.toString()),
                error -> {
                    Log.e(TAG, "V1 Notification send error: " + error.toString());
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
    public void sendAcceptanceNotification(String leaderToken, String newMemberName, String allianceName) {
        getAccessToken().addOnSuccessListener(token -> {
            try {
                JSONObject notificationJson = new JSONObject();
                notificationJson.put("title", "Invitation Accepted!");
                notificationJson.put("body", newMemberName + " has joined your alliance, " + allianceName + "!");

                JSONObject dataJson = new JSONObject();
                dataJson.put("type", "INVITATION_ACCEPTED");
                dataJson.put("memberName", newMemberName);
                dataJson.put("allianceName", allianceName);

                JSONObject messageJson = new JSONObject();
                messageJson.put("token", leaderToken);
                messageJson.put("notification", notificationJson);
                messageJson.put("data", dataJson);

                JSONObject mainJson = new JSONObject();
                mainJson.put("message", mainJson);

                mainJson.put("message", messageJson);

                sendFcmRequest(mainJson, token);

            } catch (Exception e) {
                Log.e(TAG, "Error creating acceptance notification JSON", e);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Access Token for acceptance notification", e);
        });
    }
    private Task<String> getAccessToken() {
        if (accessToken != null) {
            // TODO: Proveriti da li je token istekao pre nego što ga vratimo
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
                Log.e(TAG, "Error generating access token from service account file", e);
                throw e;
            }
        });
    }

    public Task<Void> acceptInvite(String inviteId) {
        return inviteRepository.acceptInvite(inviteId);
    }

    public Task<Void> declineInvite(String inviteId) {
        return inviteRepository.declineInvite(inviteId);
    }
    public Task<AllianceInvite> getInvitationById(String inviteId) {
        return inviteRepository.getInvitationById(inviteId);
    }
}