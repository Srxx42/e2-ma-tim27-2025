package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.BossRepository;
import com.example.e2taskly.model.Boss;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class BossService {

    private BossRepository bossRepository;

    public BossService(Context context){

        bossRepository = new BossRepository(context);
    }

    public Task<Void> createBoss(String enemyId, boolean isAlliance, int allianceMembers) {
        if (!isAlliance) {
            Date bossAppearance = new Date();
            Boss boss = new Boss("", enemyId, 2, 200, 200, false, false, false, bossAppearance);
            return bossRepository.createBoss(boss);
        }

        return getByEnemyId(enemyId, true).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            Boss existingBoss = task.getResult();
            Date bossAppearance = new Date();
            float bossHp = allianceMembers * 100;

            if (existingBoss != null) {

                existingBoss.setBossBeaten(false);
                existingBoss.setBossHp(bossHp);
                existingBoss.setBossLevel(existingBoss.getBossLevel() + 1);
                existingBoss.setBossAppearanceDate(bossAppearance);

                return bossRepository.updateBoss(existingBoss);
            } else {

                Boss newBoss = new Boss("", enemyId, 1, bossHp, 0, false, false, true, bossAppearance);

                return bossRepository.createBoss(newBoss);
            }
        });
    }

    public Task<Void>  beatBoss(Boss boss,int userLvl){

        if(boss.isAllianceBoss()){
            boss.setBossBeaten(true);
            return bossRepository.updateBoss(boss);
        }

        if(userLvl != -1) {
            if (userLvl > boss.getBossLevel()){
                return levelUpBoss(boss);
            } else{
                boss.setBossBeaten(true);
                return bossRepository.updateBoss(boss);
            }
        }
        return Tasks.forException(new IllegalArgumentException("User level cannot be -1 for this operation."));
    }

    public Task<Void> levelUpBoss(Boss boss){
        float currentHp = boss.getBossHp();
        float currentGold = boss.getBossGold();

        float newHp = currentHp * 2 + (currentHp / 2);
        float newGold =(float)(currentGold * 1.2);

        int newLevel = boss.getBossLevel() + 1;

        Date newAppearanceDate = boss.getBossAppearanceDate();

        boss.setBossLevel(newLevel);
        boss.setBossHp(newHp);
        boss.setBossGold(newGold);
        boss.setBossAppearanceDate(newAppearanceDate);
        boss.setBossBeaten(false);
        boss.setDidUserFightIt(false);
        return bossRepository.updateBoss(boss);

     }

     public Task<Boss> getByEnemyId(String enemyId,boolean isAlliance){
         return bossRepository.getByEnemyId(enemyId, isAlliance).continueWith(task -> {
             if (!task.isSuccessful()) {
                 // Ako je task neuspešan, prosledi grešku dalje
                 throw task.getException();
             }
             List<Boss> bosses = task.getResult();
             if (bosses != null && !bosses.isEmpty()) {
                 // Vraćamo prvog bossa iz liste, jer tvoja logika očekuje jednog
                 return bosses.get(0);
             } else {
                 // Ako lista ne postoji ili je prazna, vraćamo null
                 return null;
             }
         });
     }

    public boolean isAttackSuccessful(int successPercentage){
        Random random = new Random();

        int randomNumber = random.nextInt(100);

        return randomNumber < successPercentage;
    }

    public Task<Void> updateBoss(Boss boss){

        return bossRepository.updateBoss(boss);
    }

    public Task<Boss>  getByBossId(String bossId){
        return bossRepository.getById(bossId);
    }

    public boolean willUserGetItems(Boolean isBossBeaten){
        int successPercentage = 100;
        if(!isBossBeaten) successPercentage = 10;

        Random random = new Random();

        int randomNumber = random.nextInt(100);

        return randomNumber < successPercentage;

    }


}
