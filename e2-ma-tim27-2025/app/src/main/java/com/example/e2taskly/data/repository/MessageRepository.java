package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.MessageLocalDataSource;
import com.example.e2taskly.data.remote.MessageRemoteDataSource;
import com.example.e2taskly.model.Message;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.List;

public class MessageRepository {
    private final MessageRemoteDataSource remoteDataSource;
    private final MessageLocalDataSource localDataSource;

    public interface MessagesCallback {
        void onInitialMessagesLoaded(List<Message> messages);
        void onNewMessage(Message message);
        void onError(Exception e);
    }

    public MessageRepository(Context context) {
        this.remoteDataSource = new MessageRemoteDataSource();
        this.localDataSource = new MessageLocalDataSource(context);
    }
    public Task<Void> sendMessage(String text, String allianceId, String senderId, String senderUsername) {
        Message message = new Message();
        message.setAllianceId(allianceId);
        message.setSenderId(senderId);
        message.setSenderUsername(senderUsername);
        message.setText(text);
        message.setTimestamp(new Date());

        return remoteDataSource.sendMessage(message);
    }

    public void getAndListenForMessages(String allianceId, MessagesCallback callback) {
        Date fetchFromTimestamp = new Date(0);

        remoteDataSource.listenForNewMessages(allianceId, fetchFromTimestamp, new MessageRemoteDataSource.OnNewMessageListener() {
            @Override
            public void onNewMessage(Message message) {
                callback.onNewMessage(message);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void stopListening() {
        remoteDataSource.stopListeningForMessages();
    }
}
