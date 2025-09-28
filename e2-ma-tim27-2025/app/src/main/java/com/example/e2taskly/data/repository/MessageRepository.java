package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.MessageLocalDataSource;
import com.example.e2taskly.data.remote.MessageRemoteDataSource;
import com.example.e2taskly.model.Message;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MessageRepository {
    private final MessageRemoteDataSource remoteDataSource;
    private final MessageLocalDataSource localDataSource;
    private final Executor executor;

    public interface MessagesCallback {
        void onInitialMessagesLoaded(List<Message> messages);
        void onNewMessages(List<Message> messages);
        void onError(Exception e);
    }

    public MessageRepository(Context context) {
        this.remoteDataSource = new MessageRemoteDataSource();
        this.localDataSource = new MessageLocalDataSource(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    public Task<Void> sendMessage(String text, String allianceId, String senderId, String senderUsername) {
        Message message = new Message();
        message.setAllianceId(allianceId);
        message.setSenderId(senderId);
        message.setSenderUsername(senderUsername);
        message.setText(text);
        message.setTimestamp(new Date());

        return remoteDataSource.sendMessage(message).addOnSuccessListener(aVoid -> {
            Tasks.call(executor, () -> {
                localDataSource.saveMessage(message);
                return null;
            });
        });
    }

    public void getAndListenForMessages(String allianceId, MessagesCallback callback) {
        List<Message> localMessages = localDataSource.getMessagesForAlliance(allianceId);
        callback.onInitialMessagesLoaded(localMessages);

        Date fetchFromTimestamp = localDataSource.getLastMessageTimestamp(allianceId);

        remoteDataSource.listenForNewMessages(allianceId, fetchFromTimestamp, new MessageRemoteDataSource.OnNewMessagesListener() {
            @Override
            public void onNewMessages(List<Message> messages) {
                Tasks.call(executor, () -> {
                    localDataSource.saveMessages(messages);
                    return null;
                });

                // Prosljeđujemo listu dalje prema UI sloju
                callback.onNewMessages(messages);
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
