package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.MissionProgressLocalDataSource;
import com.example.e2taskly.data.remote.MissionProgressDataSource;
import com.example.e2taskly.model.SpecialMissionProgress;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MissionProgressRepository {

    private MissionProgressLocalDataSource localDataSource;
    private MissionProgressDataSource remoteDataSource;

    public MissionProgressRepository(Context context) {
        localDataSource = new MissionProgressLocalDataSource(context);
        remoteDataSource = new MissionProgressDataSource();
    }


    public Task<Void> createMissionProgress(SpecialMissionProgress progress) {
        return remoteDataSource.createMissionProgress(progress).addOnSuccessListener(v -> {
            localDataSource.createMissionProgress(progress);
        });
    }


    public Task<Void> updateMissionProgress(SpecialMissionProgress progress) {
        return remoteDataSource.updateMissionProgress(progress).addOnSuccessListener(aVoid -> {
            localDataSource.updateMissionProgress(progress);
        });
    }


    public Task<List<SpecialMissionProgress>> getAllAlianceProgresses(String allianceId, String bossId) {
        return remoteDataSource.getAllAlianceProgresses(allianceId, bossId).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<SpecialMissionProgress> progresses = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    SpecialMissionProgress progress = document.toObject(SpecialMissionProgress.class);
                    if (progress != null) {
                        progresses.add(progress);
                        // Ažuriraj lokalnu bazu sa svežim podacima
                        localDataSource.updateMissionProgress(progress);
                    }
                }
                return Tasks.forResult(progresses);
            }

            // Ako remote fetch ne uspe, vrati podatke iz lokalne baze
            List<SpecialMissionProgress> localProgresses = localDataSource.getAllAlianceProgresses(allianceId, bossId);
            return Tasks.forResult(localProgresses);
        });
    }

    public Task<SpecialMissionProgress> getUserProgress(String userUid) {
        return remoteDataSource.getUserProgress(userUid).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                SpecialMissionProgress progress = document.toObject(SpecialMissionProgress.class);
                if (progress != null) {
                    localDataSource.updateMissionProgress(progress);
                    return Tasks.forResult(progress);
                }
            }

            // Ako remote fetch ne uspe ili korisnik nema progress, proveri lokalno
            SpecialMissionProgress localProgress = localDataSource.getUserProgress(userUid);
            if (localProgress != null) {
                return Tasks.forResult(localProgress);
            }

            return Tasks.forException(new Exception("Progress for user " + userUid + " not found."));
        });
    }
}