package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.BossLocalDataSource;
import com.example.e2taskly.model.Boss;

public class BossRepository {

    private BossLocalDataSource localDataSource;

    public BossRepository(Context context){
        localDataSource = new BossLocalDataSource(context);
    }

    public  long createBoss(Boss boss){
        return localDataSource.createBoss(boss);
    }

    public Boss getById(int bossId){
        return  localDataSource.getById(bossId);
    }

    public Boss getByEnemyId(String enemyId,boolean isAlliance){
        return localDataSource.getByEnemyId(enemyId,isAlliance);
    }

    public boolean updateBoss(Boss boss){
        return localDataSource.updateBoss(boss);
    }


}
