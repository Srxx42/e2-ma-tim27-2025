package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.AllianceInvite;
import com.example.e2taskly.model.enums.Status;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class InviteRemoteDataSource {
    private final FirebaseFirestore db;
    public InviteRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }
    public Task<Void> sendInvites(String inviterId, String inviterUsername, Alliance alliance, List<String> friendIds) {
        WriteBatch batch = db.batch();
        for (String friendId : friendIds) {
            DocumentReference inviteRef = db.collection("alliance_invites").document();
            AllianceInvite invite = new AllianceInvite();
            invite.setInviteId(inviteRef.getId());
            invite.setAllianceId(alliance.getAllianceId());
            invite.setAllianceName(alliance.getName());
            invite.setSenderId(inviterId);
            invite.setInviterUsername(inviterUsername);
            invite.setReceiverId(friendId);
            invite.setStatus(Status.PENDING);
            batch.set(inviteRef, invite);
        }
        return batch.commit();
    }

    public Task<Void> updateInviteStatus(String inviteId, Status newStatus) {
        return db.collection("alliance_invites").document(inviteId).update("status", newStatus);
    }
}
