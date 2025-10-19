package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.AllianceLocalDataSource;
import com.example.e2taskly.data.remote.AllianceRemoteDataSource;
import com.example.e2taskly.model.Alliance;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class AllianceRepository {
    private AllianceLocalDataSource localDataSource;
    private AllianceRemoteDataSource remoteDataSource;
    public AllianceRepository(Context context){
        localDataSource = new AllianceLocalDataSource(context);
        remoteDataSource = new AllianceRemoteDataSource();
    }
    public Task<Void> createAlliance(Alliance alliance) {
        return remoteDataSource.createAlliance(alliance).addOnSuccessListener(aVoid -> {
            localDataSource.saveOrUpdateAlliance(alliance);
        });
    }

    public Task<Alliance> getAlliance(String allianceId) {
        return remoteDataSource.getAlliance(allianceId).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Alliance remoteAlliance = task.getResult().toObject(Alliance.class);
                if (remoteAlliance != null) {
                    localDataSource.saveOrUpdateAlliance(remoteAlliance);
                    return Tasks.forResult(remoteAlliance);
                }
            }

            Alliance localAlliance = localDataSource.getAllianceById(allianceId);
            if (localAlliance != null) {
                return Tasks.forResult(localAlliance);
            }

            return Tasks.forException(new Exception("Alliance with ID " + allianceId + " not found."));
        });
    }

    public Task<Void> addMember(String allianceId, String userId) {
        return remoteDataSource.addMemberToAlliance(allianceId, userId).addOnSuccessListener(aVoid -> {
            localDataSource.addMemberToAlliance(allianceId, userId);
        });
    }

    public Task<Void> removeMember(String allianceId, String userId) {
        return remoteDataSource.removeMemberFromAlliance(allianceId, userId).addOnSuccessListener(aVoid -> {
            localDataSource.removeMemberFromAlliance(allianceId, userId);
        });
    }

    public Task<Void> deleteAlliance(String allianceId) {
        return remoteDataSource.deleteAlliance(allianceId).addOnSuccessListener(aVoid -> {
            localDataSource.deleteAlliance(allianceId);
        });
    }

}
