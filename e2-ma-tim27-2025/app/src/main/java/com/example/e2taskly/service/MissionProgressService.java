package com.example.e2taskly.service;

import static android.app.ProgressDialog.show;

import android.content.Context;
import android.widget.Toast;

import com.example.e2taskly.data.repository.MissionProgressRepository;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.model.SpecialMissionProgress;
import com.example.e2taskly.model.enums.ProgressType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MissionProgressService {

    private MissionProgressRepository missionProgressRepository;

    private BossService bossService;

    private Context context;

    public MissionProgressService(Context context){
        missionProgressRepository = new MissionProgressRepository(context);
        bossService = new BossService(context);
        this.context = context;
    }

    public void CreateProgressesForAlliance(List<String> usersIds,String allianceId,int bossId){
        List<LocalDate> messageCount = new ArrayList<>();
        for(String userId : usersIds){
            SpecialMissionProgress progress = new SpecialMissionProgress(-1,userId,allianceId,bossId,0,0,0,0,false,false,messageCount);
            missionProgressRepository.createMissionProgress(progress);
        }

    }

    public List<SpecialMissionProgress> getAllAlianceProgresses(String allianceId, int bossId){
        return missionProgressRepository.getAllAlianceProgresses(allianceId,bossId);
    }

    public SpecialMissionProgress getUserProgress(String userId){
        return missionProgressRepository.getUserProgress(userId);
    }

    public boolean updateProgress(String userId,ProgressType progressType){
        SpecialMissionProgress progress = getUserProgress(userId);

        if(progress == null){ return false;}
        if(progress.isDidUserGetReward()) {return false;}

        Boss allianceBoss = bossService.getByBossId(progress.getBossId());
        if(allianceBoss.isBossBeaten()){
            return false;
        }

        //Ako je u pitanju SHOPPING
        if(progressType.equals(ProgressType.SHOPPING)){
            if(progress.getShoppingCount() >= 5) {
                Toast.makeText(context, "You reached maximum of shopping special mission!", Toast.LENGTH_SHORT).show();
                return false;
            }
            progress.setShoppingCount(progress.getShoppingCount() + 1);
            allianceBoss.setBossHp(allianceBoss.getBossHp() - 2);
        }

        //Ako je u pitanju LAKSI TASK
        if(progressType.equals(ProgressType.EASY_TASK)){
            if(progress.getEasyTaskCount() >= 10){
                Toast.makeText(context, "You reached maximum of easy task special mission!", Toast.LENGTH_SHORT).show();
                return false;
            }
            progress.setEasyTaskCount(progress.getEasyTaskCount() + 1);
            allianceBoss.setBossHp(allianceBoss.getBossHp() - 1);
        }

        //Ako je u pitanju TEZI TASK
        if(progressType.equals(ProgressType.HARD_TASK)){
            if(progress.getHardTaskCount() >= 6){
                Toast.makeText(context, "You reached maximum of hard task special mission!", Toast.LENGTH_SHORT).show();
                return false;
            }
            progress.setHardTaskCount(progress.getHardTaskCount() + 1);
            allianceBoss.setBossHp(allianceBoss.getBossHp() - 4);
        }

        //Ako je u pitanju HIT BOSS
        if(progressType.equals(ProgressType.HIT_BOSS)){
            if(progress.getSuccessfulBossHitCount() >= 10) {
                Toast.makeText(context, "You reached maximum of boss attack special mission!", Toast.LENGTH_SHORT).show();
                return false;
            }
            progress.setSuccessfulBossHitCount(progress.getSuccessfulBossHitCount() + 1);
            allianceBoss.setBossHp(allianceBoss.getBossHp() - 2);
        }

        //Ako je u pitanju ALLIANCE MESSAGE
        if(progressType.equals(ProgressType.ALLIANCE_MESSAGE)){
            List<LocalDate> dates = progress.getMessageCount();

            if(dates.contains(LocalDate.now())){
                return false;
            } else{
                dates.add(LocalDate.now());
            }
            progress.setMessageCount(dates);
            allianceBoss.setBossHp(allianceBoss.getBossHp() - 4);
        }

        if(!progress.isCompletedAll()){
            boolean didUserCompleteAll = (progress.getShoppingCount() == 5) &&  (progress.getSuccessfulBossHitCount() == 10)
                    && (progress.getEasyTaskCount() == 10) && (progress.getHardTaskCount() == 6);

            if(didUserCompleteAll){
                progress.setCompletedAll(true);
                allianceBoss.setBossHp(allianceBoss.getBossHp() - 10);
            }

        }

        if(allianceBoss.getBossHp() <= 0){
            allianceBoss.setBossBeaten(true);
        }

        bossService.updateBoss(allianceBoss);
        return missionProgressRepository.updateMissionProgress(progress);
    }


}
