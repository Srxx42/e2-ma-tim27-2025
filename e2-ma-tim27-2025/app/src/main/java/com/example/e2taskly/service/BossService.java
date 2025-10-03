package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.BossRepository;
import com.example.e2taskly.model.Boss;

import java.time.LocalDate;
import java.util.Random;

public class BossService {

    private BossRepository bossRepository;

    private UserService userService;

    public BossService(Context context){

        bossRepository = new BossRepository(context);
        userService = new UserService(context);
    }

    public void createBoss(String enemyId,boolean isAlliance,int allianceMembers){
        int id = -1;
        LocalDate bossApperience = LocalDate.now();
        Boss boss = new Boss();
        if(isAlliance){
            float bossHp = allianceMembers * 100;
             boss = new Boss(id,enemyId,1,bossHp,0,false,isAlliance,bossApperience);
        } else{
             boss = new Boss(id,enemyId,2,200,200,false,isAlliance,bossApperience);
        }

        bossRepository.createBoss(boss);

    }

    public boolean beatBoss(Boss boss){

        if(boss.isAllianceBoss()){
            boss.setBossBeaten(true);
            return bossRepository.updateBoss(boss);
        }

        int userLevel = userService.getUserLevel();

        if(userLevel != -1) {
            if (userLevel > boss.getBossLevel()){
                return levelUpBoss(boss);
            } else{
                boss.setBossBeaten(true);
                return bossRepository.updateBoss(boss);
            }
        }
        return false;
    }

    public boolean levelUpBoss(Boss boss){
        float currentHp = boss.getBossHp();
        float currentGold = boss.getBossGold();

        float newHp = currentHp * 2 + (currentHp / 2);
        float newGold =(float)(currentGold * 1.2);

        int newLevel = boss.getBossLevel() + 1;

        LocalDate newAppearanceDate = boss.getBossAppearanceDate();

        boss.setBossLevel(newLevel);
        boss.setBossHp(newHp);
        boss.setBossGold(newGold);
        boss.setBossAppearanceDate(newAppearanceDate);

        return bossRepository.updateBoss(boss);

     }

     public Boss getByEnemyId(String enemyId,boolean isAlliance){
        return bossRepository.getByEnemyId(enemyId,isAlliance);
     }

    public boolean isAttackSuccessful(int successPercentage){
        Random random = new Random();

        int randomNumber = random.nextInt(100);

        return randomNumber < successPercentage;
    }
}
