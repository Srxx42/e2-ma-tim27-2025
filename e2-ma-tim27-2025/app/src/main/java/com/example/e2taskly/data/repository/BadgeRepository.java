package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.remote.BadgesRemoteDataSource;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.UserBadge;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BadgeRepository {

    private BadgesRemoteDataSource remoteDataSource;

    public BadgeRepository(Context context){
        remoteDataSource = new BadgesRemoteDataSource();
    }
    public Task<Void> createBadge(UserBadge badge){
        return remoteDataSource.createBadge(badge);
    }

    public Task<UserBadge> getBadge(String badgeId){
        return remoteDataSource.getBadge(badgeId).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                UserBadge bdg = task.getResult().toObject(UserBadge.class);
                if (bdg != null) {
                    return Tasks.forResult(bdg);
                }
            }

            return Tasks.forException(new Exception("Badge with ID " + badgeId + " not found."));
        });
    }

    public Task<List<UserBadge>> getUserBadges(String userId) {
        return remoteDataSource.getUserBadges(userId).continueWithTask(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {

                    List<UserBadge> badgeList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        UserBadge badge = document.toObject(UserBadge.class);
                        badgeList.add(badge);
                    }
                    return Tasks.forResult(badgeList);
                }
            }
            return Tasks.forException(task.getException() != null ? task.getException() : new Exception("Failed to get badges for user " + userId));
        });
    }

}
