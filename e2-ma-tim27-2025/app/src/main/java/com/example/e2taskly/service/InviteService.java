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
import java.util.ArrayList;
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
    private final NotificationSendingService notificationSendingService;

    public InviteService(Context context) {
        this.inviteRepository = new InviteRepository(context);
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(this.context);
        notificationSendingService = new NotificationSendingService(context);
    }

    public Task<Void> sendInvites(String inviterId, String inviterUsername, Alliance alliance, List<User> friendsToInvite) {
        List<AllianceInvite> invitesToSend = new ArrayList<>();
        for (User friend : friendsToInvite) {
            invitesToSend.add(new AllianceInvite(
                    alliance.getAllianceId(),
                    alliance.getName(),
                    inviterId,
                    inviterUsername,
                    friend.getUid()
            ));
        }

        return inviteRepository.sendInvites(invitesToSend)
                .onSuccessTask(aVoid -> {
                    for (int i = 0; i < friendsToInvite.size(); i++) {
                        User friend = friendsToInvite.get(i);
                        AllianceInvite invite = invitesToSend.get(i);

                        if (friend.getFcmToken() != null && !friend.getFcmToken().isEmpty()) {
                            Log.d(TAG, "Preparing to send notification to: " + friend.getUsername());
                            sendInviteFcmNotification(friend.getFcmToken(), inviterUsername, alliance, invite.getInviteId());
                        }
                    }
                    return Tasks.forResult(null);
                });
    }

    private void sendInviteFcmNotification(String receiverToken, String inviterUsername, Alliance alliance, String inviteId) {
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

            notificationSendingService.sendFcmNotification(receiverToken, notificationJson, dataJson);
        } catch (Exception e) {
            Log.e("InviteService", "Error creating invite notification JSON", e);
        }
    }

    public void sendAcceptanceNotification(String leaderToken, String newMemberName, String allianceName) {
        notificationSendingService.getAccessToken().addOnSuccessListener(token -> {
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

                notificationSendingService.sendFcmRequest(mainJson, token);

            } catch (Exception e) {
                Log.e(TAG, "Error creating acceptance notification JSON", e);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Access Token for acceptance notification", e);
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