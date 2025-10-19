package com.example.e2taskly.service;

import android.content.Context;
import android.widget.Toast;

import com.example.e2taskly.data.repository.MissionProgressRepository;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.model.SpecialMissionProgress;
import com.example.e2taskly.model.enums.ProgressType;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MissionProgressService {

    private final MissionProgressRepository missionProgressRepository;
    private final BossService bossService;
    private final Context context;

    public MissionProgressService(Context context) {
        missionProgressRepository = new MissionProgressRepository(context);
        bossService = new BossService(context);
        this.context = context;
    }

    public Task<Void> createProgressesForAlliance(List<String> userIds, String allianceId, String bossId) {
        return getAllAlianceProgresses(allianceId, bossId).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<SpecialMissionProgress> existingProgress = task.getResult();
            List<Task<Void>> operationTasks = new ArrayList<>();

            if (existingProgress == null || existingProgress.isEmpty()) {
                List<Date> emptyMessageCount = new ArrayList<>();
                for (String userId : userIds) {
                    SpecialMissionProgress progress = new SpecialMissionProgress("", userId, allianceId, bossId, 0, 0, 0, 0, false, false, emptyMessageCount);
                    operationTasks.add(missionProgressRepository.createMissionProgress(progress));
                }
            } else {
                List<Date> emptyMessageCount = new ArrayList<>();
                for (SpecialMissionProgress progress : existingProgress) {
                    progress.setCompletedAll(false);
                    progress.setEasyTaskCount(0);
                    progress.setHardTaskCount(0);
                    progress.setShoppingCount(0);
                    progress.setSuccessfulBossHitCount(0);
                    progress.setMessageCount(emptyMessageCount);
                    progress.setDidUserGetReward(false);
                    operationTasks.add(updateProgress(progress));
                }
            }

            return Tasks.whenAll(operationTasks);
        });
    }

    public Task<List<SpecialMissionProgress>> getAllAlianceProgresses(String allianceId, String bossId) {
        return missionProgressRepository.getAllAlianceProgresses(allianceId, bossId);
    }

    public Task<SpecialMissionProgress> getUserProgress(String userId) {
        return missionProgressRepository.getUserProgress(userId);
    }

    public Task<Void> updateProgress(SpecialMissionProgress progress){
        return missionProgressRepository.updateMissionProgress(progress);
    }

    public Task<Boolean> updateMissionProgress(String userId, ProgressType progressType) {
        // 1. Dohvati progress korisnika
        return missionProgressRepository.getUserProgress(userId).continueWithTask(progressTask -> {
            if (!progressTask.isSuccessful() || progressTask.getResult() == null) {
                // Nismo našli progress, vraćamo false (neuspeh)
                return Tasks.forResult(false);
            }

            SpecialMissionProgress progress = progressTask.getResult();

            if (progress.isDidUserGetReward()) {
                // Već je pokupio nagradu, nema šta da se ažurira
                return Tasks.forResult(false);
            }

            // 2. Dohvati Boss-a vezanog za taj progress
            return bossService.getByBossId(progress.getBossId()).continueWithTask(bossTask -> {
                if (!bossTask.isSuccessful() || bossTask.getResult() == null) {
                    // Nismo našli boss-a
                    return Tasks.forResult(false);
                }

                Boss allianceBoss = bossTask.getResult();

                if (allianceBoss.isBossBeaten()) {
                    // Boss je već mrtav
                    return Tasks.forResult(false);
                }


                Date today = new Date();
                Date limit = allianceBoss.getBossAppearanceDate();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(limit);

                calendar.add(Calendar.DAY_OF_YEAR, 14);
                Date expiryDate = calendar.getTime();

                boolean canUpdate = today.before(expiryDate);
                if(!canUpdate){
                    Toast.makeText(context, "This mission has expired!", Toast.LENGTH_SHORT).show(); // Opciono
                    return Tasks.forResult(false);
                }

                // 3. Primeni promene (localno)
                boolean shouldUpdate = applyProgressChanges(progress, allianceBoss, progressType);
                if (!shouldUpdate) {
                    return Tasks.forResult(false);
                }

                checkIfAllMissionsCompleted(progress, allianceBoss);

                if (allianceBoss.getBossHp() <= 0) {
                    allianceBoss.setBossHp(0); // Da ne ode u minus
                    allianceBoss.setBossBeaten(true);
                }

                // 4. Sačuvaj promene (prvo boss, pa ako uspe, onda progress)
                // Lančano vezujemo čuvanje da bismo bili sigurniji
                return bossService.updateBoss(allianceBoss).continueWithTask(updateBossTask -> {
                    if (!updateBossTask.isSuccessful()) {
                        return Tasks.forResult(false);
                    }
                    // Vraćamo task koji na kraju vraća true ako uspe
                    return missionProgressRepository.updateMissionProgress(progress)
                            .continueWith(task -> task.isSuccessful());
                });
            });
        });
    }

    private boolean applyProgressChanges(SpecialMissionProgress progress, Boss allianceBoss, ProgressType progressType) {
        switch (progressType) {
            case SHOPPING:
                if (progress.getShoppingCount() >= 5) {
                    Toast.makeText(context, "You reached maximum of shopping special mission!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                progress.setShoppingCount(progress.getShoppingCount() + 1);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 2);
                break;

            case EASY_TASK:
                if (progress.getEasyTaskCount() >= 10) {
                    Toast.makeText(context, "You reached maximum of easy task special mission!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                progress.setEasyTaskCount(progress.getEasyTaskCount() + 1);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 1);
                break;

            case HARD_TASK:
                if (progress.getHardTaskCount() >= 6) {
                    Toast.makeText(context, "You reached maximum of hard task special mission!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                progress.setHardTaskCount(progress.getHardTaskCount() + 1);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 4);
                break;

            case HIT_BOSS:
                if (progress.getSuccessfulBossHitCount() >= 10) {
                    Toast.makeText(context, "You reached maximum of boss attack special mission!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                progress.setSuccessfulBossHitCount(progress.getSuccessfulBossHitCount() + 1);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 2);
                break;

            case ALLIANCE_MESSAGE:
                List<Date> dates = progress.getMessageCount();
                boolean alreadySentToday = false;
                Date today = new Date();
                for (Date messageDate : dates) {
                    if (isSameDay(messageDate, today)) {
                        alreadySentToday = true;
                        break;
                    }
                }
                if (alreadySentToday) {
                    return false;
                }
                dates.add(today);
                progress.setMessageCount(dates);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 4);
                break;
        }
        return true;
    }

    private void checkIfAllMissionsCompleted(SpecialMissionProgress progress, Boss allianceBoss) {
        if (!progress.isCompletedAll()) {
            boolean didUserCompleteAll = (progress.getShoppingCount() >= 5) &&
                    (progress.getSuccessfulBossHitCount() >= 10) &&
                    (progress.getEasyTaskCount() >= 10) &&
                    (progress.getHardTaskCount() >= 6);
            if (didUserCompleteAll) {
                progress.setCompletedAll(true);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 10);
            }
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}