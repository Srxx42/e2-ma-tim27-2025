package com.example.e2taskly.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.e2taskly.presentation.activity.AllianceDecisionActivity;
import com.example.e2taskly.service.AllianceService;
import com.example.e2taskly.service.InviteService;
import com.example.e2taskly.service.UserService;
public class InviteActionReceiver extends BroadcastReceiver {
    public static final String ACTION_ACCEPT = "com.example.e2taskly.ACCEPT_INVITE";
    public static final String ACTION_DECLINE = "com.example.e2taskly.DECLINE_INVITE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        String inviteId = intent.getStringExtra("invite_id");
        if (inviteId == null) return;

        UserService userService = new UserService(context);
        AllianceService allianceService = new AllianceService(context);
        InviteService inviteService = new InviteService(context);
        String currentUserId = userService.getCurrentUserId();

        if (currentUserId == null) {
            return;
        }

        if (ACTION_ACCEPT.equals(action)) {
            String allianceId = intent.getStringExtra("alliance_id");
            if (allianceId != null) {
                allianceService.acceptInvite(currentUserId, allianceId, inviteId)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Joined alliance!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            String errorMessage = e.getMessage();

                            if (errorMessage != null && errorMessage.contains("ALREADY_IN_ALLIANCE")) {
                                Intent decisionIntent = new Intent(context, AllianceDecisionActivity.class);
                                decisionIntent.putExtra("invite_id", inviteId);
                                decisionIntent.putExtra("alliance_id", allianceId);
                                decisionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(decisionIntent);
                            } else {
                                Toast.makeText(context, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else if (ACTION_DECLINE.equals(action)) {
            inviteService.declineInvite(inviteId)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Invite declined.", Toast.LENGTH_SHORT).show());
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(inviteId.hashCode());
    }
}