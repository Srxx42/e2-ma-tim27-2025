package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.BossLocalDataSource;
import com.example.e2taskly.data.remote.BossRemoteDataSource;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.Boss;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BossRepository {

    private BossLocalDataSource localDataSource;
    private BossRemoteDataSource remoteDataSource;

    public BossRepository(Context context){
        localDataSource = new BossLocalDataSource(context);
        remoteDataSource = new BossRemoteDataSource();
    }

    public Task<Void> createBoss(Boss boss){
        return remoteDataSource.createBoss(boss).addOnSuccessListener(v ->{
            localDataSource.createBoss(boss);
        });

    }
    public Task<Boss> getById(String bossId){
        return remoteDataSource.getById(bossId).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
               Boss boss = task.getResult().toObject(Boss.class);
                if (boss != null) {
                    localDataSource.updateBoss(boss);
                    return Tasks.forResult(boss);
                }
            }

            Boss localBoss = localDataSource.getById(bossId);
            if (localBoss != null) {
                return Tasks.forResult(localBoss);
            }

            return Tasks.forException(new Exception("Boss with ID " + bossId + " not found."));
        });
    }

    public Task<List<Boss>> getByEnemyId(String enemyId, boolean isAlliance) {
        return remoteDataSource.getByEnemyId(enemyId, isAlliance).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Boss> bosses = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    Boss boss = document.toObject(Boss.class);
                    if (boss != null) {
                        boss.setBossId(document.getId());
                        bosses.add(boss);
                        localDataSource.updateBoss(boss);
                    }
                }
                return Tasks.forResult(bosses);
            }

            List<Boss> localBosses = localDataSource.getByEnemyId(enemyId, isAlliance);
            return Tasks.forResult(localBosses);
        });
    }

    public Task<Void> updateBoss(Boss boss) {
        return remoteDataSource.updateBoss(boss).addOnSuccessListener(aVoid -> {
            boolean localUpdateSuccess = localDataSource.updateBoss(boss);
        });
    }

}
