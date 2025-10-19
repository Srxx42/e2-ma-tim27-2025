package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.SpecialMissionProgress;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

public class MissionProgressDataSource {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "missionProgress";

    public MissionProgressDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> createMissionProgress(SpecialMissionProgress progress) {
        DocumentReference progressRef = db.collection(COLLECTION_NAME).document();
        progress.setSmpId(progressRef.getId());
        return progressRef.set(progress);
    }

    public Task<Void> updateMissionProgress(SpecialMissionProgress progress) {
        String docId = String.valueOf(progress.getSmpId());
        return db.collection(COLLECTION_NAME).document(docId).set(progress, SetOptions.merge());
    }

    public Task<QuerySnapshot> getAllAlianceProgresses(String allianceId, String bossId) {
        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("bossId", bossId);
        return query.get();
    }

    public Task<QuerySnapshot> getUserProgress(String userUid) {
        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("userUid", userUid);
        return query.get();
    }
}