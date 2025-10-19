package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.Boss;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class BossRemoteDataSource {

    private final FirebaseFirestore db;

    public BossRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> createBoss(Boss boss) {
        DocumentReference bossRef = db.collection("bosses").document();
        boss.setBossId(bossRef.getId());
        return bossRef.set(boss);
    }

    public Task<DocumentSnapshot> getById(String bossId) {
        return db.collection("bosses").document(bossId).get();
    }

    public Task<QuerySnapshot> getByEnemyId(String enemyId, boolean isAlliance) {
        Query query = db.collection("bosses")
                .whereEqualTo("enemyId", enemyId)
                .whereEqualTo("allianceBoss", isAlliance);
        return query.get();
    }

    public Task<Void> updateBoss(Boss boss) {
        return db.collection("bosses").document(boss.getBossId()).set(boss);
    }

    public Task<Void> deleteBoss(String bossId) {
        return db.collection("bosses").document(bossId).delete();
    }
}