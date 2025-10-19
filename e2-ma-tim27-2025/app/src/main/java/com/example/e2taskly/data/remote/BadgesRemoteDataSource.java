package com.example.e2taskly.data.remote;

import com.example.e2taskly.model.UserBadge;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class BadgesRemoteDataSource {
    private final FirebaseFirestore db;

    public BadgesRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<Void> createBadge(UserBadge badge){
        DocumentReference badgeRef = db.collection("userBadges").document();
        badge.setBadgeId(badgeRef.getId());
        return badgeRef.set(badge);
    }

    public Task<DocumentSnapshot> getBadge(String badgeId){
        return db.collection("userBadges").document(badgeId).get();
    }

    public Task<QuerySnapshot> getUserBadges(String userId){
        return db.collection("userBadges").whereEqualTo("userId", userId).get();
    }


}
