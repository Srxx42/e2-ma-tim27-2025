package com.example.e2taskly.service;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.e2taskly.model.AllianceInvite;
import com.example.e2taskly.util.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0 && data.containsKey("type")) {
            String type = data.get("type");
            Log.d("MyFCMService", "Received FCM message of type: " + type);

            if ("INVITATION".equals(type)) {
                handleInvitation(data);
            } else if ("INVITATION_ACCEPTED".equals(type)) {
                handleInvitationAccepted(data);
            }
        }
    }
    private void handleInvitation(Map<String, String> data) {
        AllianceInvite invite = new AllianceInvite();
        invite.setInviteId(data.get("inviteId"));
        invite.setAllianceId(data.get("allianceId"));
        invite.setAllianceName(data.get("allianceName"));
        invite.setInviterUsername(data.get("inviterUsername"));
        invite.setSenderId(data.get("senderId"));
        invite.setReceiverId(data.get("receiverId"));

        NotificationHelper.showInviteNotification(this, invite);
    }
    private void handleInvitationAccepted(Map<String, String> data) {
        String memberName = data.get("memberName");
        String allianceName = data.get("allianceName");

        if (memberName != null && allianceName != null) {
            NotificationHelper.showInfoNotification(
                    this,
                    "Invitation Accepted",
                    memberName + " has joined your alliance '" + allianceName + "'!"
            );
        }
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("MyFCMService", "New FCM Token generated: " + token);
         UserService userService = new UserService(this);
         String currentUserId = userService.getCurrentUserId();
         if (currentUserId != null) {
             userService.updateUserFcmToken(currentUserId, token);
         }
    }
}
