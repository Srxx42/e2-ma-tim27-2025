package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.Alliance;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class AllianceRemoteDataSource {
    private final FirebaseFirestore db;
    public AllianceRemoteDataSource(){
        db = FirebaseFirestore.getInstance();
    }
    public Task<Void> createAlliance(Alliance alliance) {
        DocumentReference allianceRef = db.collection("alliances").document();
        alliance.setAllianceId(allianceRef.getId());
        return allianceRef.set(alliance);
    }
    public Task<DocumentSnapshot> getAlliance(String allianceId) {
        return db.collection("alliances").document(allianceId).get();
    }
    public Task<Void> deleteAlliance(String allianceId) {
        return db.collection("alliances").document(allianceId).delete();
    }
    public Task<Void> addMemberToAlliance(String allianceId, String userId) {
        return db.collection("alliances").document(allianceId)
                .update("memberIds", FieldValue.arrayUnion(userId));
    }

    public Task<Void> removeMemberFromAlliance(String allianceId, String userId) {
        return db.collection("alliances").document(allianceId)
                .update("memberIds", FieldValue.arrayRemove(userId));
    }
}
