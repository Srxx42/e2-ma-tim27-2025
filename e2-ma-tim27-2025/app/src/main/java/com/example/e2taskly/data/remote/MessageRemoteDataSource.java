package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.Message;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class MessageRemoteDataSource {
    private final FirebaseFirestore db;
    private ListenerRegistration messageListener;
    public MessageRemoteDataSource(){
        this.db = FirebaseFirestore.getInstance();
    }
    private CollectionReference getMessagesCollection(String allianceId){
        return db.collection("alliances").document(allianceId).collection("messages");
    }
    public Task<Void> sendMessage(Message message) {
        CollectionReference messagesRef = getMessagesCollection(message.getAllianceId());

        DocumentReference newDocRef = messagesRef.document();

        message.setMessageId(newDocRef.getId());

        return newDocRef.set(message);
    }
    public interface OnNewMessageListener {
        void onNewMessage(Message message);
        void onError(Exception e);
    }

    public void listenForNewMessages(String allianceId, Date lastSyncTimestamp, OnNewMessageListener listener) {
        Query query = getMessagesCollection(allianceId)
                .whereGreaterThan("timestamp", lastSyncTimestamp)
                .orderBy("timestamp", Query.Direction.ASCENDING);

        messageListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                listener.onError(e);
                return;
            }

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    Message message = dc.getDocument().toObject(Message.class);
                    listener.onNewMessage(message);
                }
            }
        });
    }
    public void stopListeningForMessages() {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
    }
}
