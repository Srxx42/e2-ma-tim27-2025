package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;

import com.example.e2taskly.data.repository.AllianceRepository;
import com.example.e2taskly.data.repository.MessageRepository;
import com.example.e2taskly.data.repository.UserRepository;
import com.example.e2taskly.model.Message;
import com.example.e2taskly.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONObject;

import java.util.List;
public class MessageService {

    private final MessageRepository messageRepository;
    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;
    private final NotificationSendingService notificationSendingService;

    public MessageService(Context context) {
        this.messageRepository = new MessageRepository(context);
        this.allianceRepository = new AllianceRepository(context);
        this.userRepository = new UserRepository(context);
        this.notificationSendingService = new NotificationSendingService(context);
    }

    public Task<Void> sendMessage(String text, String allianceId, String senderId, String senderUsername) {
        return messageRepository.sendMessage(text, allianceId, senderId, senderUsername)
                .onSuccessTask(aVoid -> {
                    return sendNotificationsToMembers(text, allianceId, senderId, senderUsername);
                });
    }
    private Task<Void> sendNotificationsToMembers(String text, String allianceId, String senderId, String senderUsername) {
        return allianceRepository.getAlliance(allianceId).onSuccessTask(alliance -> {
            if (alliance == null) return Tasks.forResult(null);

            return userRepository.getUsersByIds(alliance.getMemberIds()).onSuccessTask(members -> {
                for (User member : members) {
                    if (!member.getUid().equals(senderId) && member.getFcmToken() != null && !member.getFcmToken().isEmpty()) {
                        try {
                            JSONObject notificationJson = new JSONObject();
                            notificationJson.put("title", "New message in " + alliance.getName());
                            notificationJson.put("body", senderUsername + ": " + text);

                            JSONObject dataJson = new JSONObject();
                            dataJson.put("type", "CHAT_MESSAGE");
                            dataJson.put("allianceId", alliance.getAllianceId());
                            dataJson.put("allianceName", alliance.getName());

                            notificationSendingService.sendFcmNotification(member.getFcmToken(), notificationJson, dataJson);
                        } catch (Exception e) {
                            Log.e("MessageService", "Error creating chat notification JSON", e);
                        }
                    }
                }
                return Tasks.forResult(null);
            });
        });
    }
    public void getAndListenForMessages(String allianceId, MessageRepository.MessagesCallback callback) {
        messageRepository.getAndListenForMessages(allianceId, callback);
    }

    public void stopListening() {
        messageRepository.stopListening();
    }
}
