package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.MissionProgressLocalDataSource;
import com.example.e2taskly.model.SpecialMissionProgress;

import java.util.List;

public class MissionProgressRepository {

    private MissionProgressLocalDataSource localDataSource;

    public MissionProgressRepository(Context context){
        localDataSource = new MissionProgressLocalDataSource(context);
    }

    public long createMissionProgress(SpecialMissionProgress progress){
        return localDataSource.createMissionProgress(progress);
    }

    public boolean updateMissionProgress(SpecialMissionProgress progress){
        return localDataSource.updateMissionProgress(progress);
    }
    public List<SpecialMissionProgress> getAllAlianceProgresses(String allianceId, int bossId){
        return localDataSource.getAllAlianceProgresses(allianceId,bossId);
    }

    public SpecialMissionProgress getUserProgress(String userUid) {
        return localDataSource.getUserProgress(userUid);
    }



}
