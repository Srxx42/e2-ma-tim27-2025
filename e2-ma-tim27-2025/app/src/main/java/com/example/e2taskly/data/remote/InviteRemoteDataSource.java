package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.AllianceInvite;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.enums.Status;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class InviteRemoteDataSource {
    private final FirebaseFirestore db;
    public InviteRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }
    public Task<Void> sendInvites(List<AllianceInvite> invites) {
        WriteBatch batch = db.batch();
        for (AllianceInvite invite : invites) {
            DocumentReference inviteRef = db.collection("alliance_invites").document(invite.getInviteId());
            batch.set(inviteRef, invite);
        }
        return batch.commit();
    }
    public Task<DocumentSnapshot> getInvitationById(String inviteId) {
        return db.collection("alliance_invites").document(inviteId).get();
    }


    public Task<Void> updateInviteStatus(String inviteId, Status newStatus) {
        return db.collection("alliance_invites").document(inviteId).update("status", newStatus);
    }
}
