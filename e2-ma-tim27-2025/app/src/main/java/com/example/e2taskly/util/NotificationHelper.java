package com.example.e2taskly.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.e2taskly.R;
import com.example.e2taskly.broadcast.InviteActionReceiver;
import com.example.e2taskly.model.AllianceInvite;
import com.example.e2taskly.presentation.activity.AllianceMessagesActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "alliance_invites_channel";
    private static final String CHANNEL_ID_INFO = "alliance_info_channel";

    public static void showInviteNotification(Context context, AllianceInvite invite) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alliance Invites", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent acceptIntent = new Intent(context, InviteActionReceiver.class);
        acceptIntent.setAction(InviteActionReceiver.ACTION_ACCEPT);
        acceptIntent.putExtra("invite_id", invite.getInviteId());
        acceptIntent.putExtra("alliance_id", invite.getAllianceId());
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                context, invite.getInviteId().hashCode(), acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent declineIntent = new Intent(context, InviteActionReceiver.class);
        declineIntent.setAction(InviteActionReceiver.ACTION_DECLINE);
        declineIntent.putExtra("invite_id", invite.getInviteId());
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                context, invite.getInviteId().hashCode() + 1, declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alliance)
                .setContentTitle("Alliance Invitation")
                .setContentText(invite.getInviterUsername() + " invited you to join " + invite.getAllianceName())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(0, "Accept", acceptPendingIntent)
                .addAction(0, "Decline", declinePendingIntent);

        notificationManager.notify(invite.getInviteId().hashCode(), builder.build());
    }
    public static void showInfoNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_INFO, "Alliance Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alliance)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    public static void showChatMessageNotification(Context context, String title, String message, String allianceId, String allianceName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_INFO, "Alliance Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, AllianceMessagesActivity.class);
        intent.putExtra(AllianceMessagesActivity.EXTRA_ALLIANCE_ID, allianceId);
        intent.putExtra(AllianceMessagesActivity.EXTRA_ALLIANCE_NAME, allianceName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, allianceId.hashCode(), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_INFO)
                .setSmallIcon(R.drawable.ic_chat) // Možete koristiti drugu ikonicu za chat
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(allianceId.hashCode(), builder.build());
    }
}
