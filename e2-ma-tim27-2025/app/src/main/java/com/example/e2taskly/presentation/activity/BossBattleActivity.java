package com.example.e2taskly.presentation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.service.BossService;
import com.example.e2taskly.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class BossBattleActivity extends AppCompatActivity {

    private BossService bossService;
    private UserService userService;
    private Boss currentBoss;

    // UI Elementi
    private RelativeLayout rootLayout;
    private ImageView ivBackground, ivBoss;
    private ProgressBar pbBossHp;
    private TextView tvBossHp, tvUserPp, tvGoldAmount, tvAttackChance;
    private ImageButton btnAttack;
    private List<ImageView> swordIcons;

    //UI Layouts
    private RelativeLayout battle_ui_container;
    private LinearLayout llBossBeatenMessage, lost_boss_message;

    private int attacksLeft = 5;
    private  int attackChance ;
    private  int userPP ;
    private int userLvl;

    private float maxBossHp;
    private boolean isDragon;
    private boolean isAttackInProgress = false; // Sprečava višestruke brze klikove

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        bossService = new BossService(this);
        userService = new UserService(this);

        String userId = userService.getCurrentUserId();
        currentBoss = bossService.getByEnemyId(userId,false);

        initializeUI();
        loadUserStats(userId);

    }

    private void loadUserStats(String userId){
        userService.getUserProfile(userId)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    this.attackChance = user.getAttackChance();
                    this.userPP = user.getPowerPoints();
                    this.userLvl = user.getLevel();

                    setupInitialState();
                    btnAttack.setOnClickListener(v -> performAttack());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void initializeUI() {
        rootLayout = findViewById(R.id.boss_battle_root);
        ivBackground = findViewById(R.id.iv_background);
        ivBoss = findViewById(R.id.iv_boss);
        pbBossHp = findViewById(R.id.pb_boss_hp);
        tvBossHp = findViewById(R.id.tv_boss_hp);
        tvUserPp = findViewById(R.id.tv_user_pp);
        tvGoldAmount = findViewById(R.id.tv_gold_amount);
        tvAttackChance = findViewById(R.id.tv_attack_chance);
        btnAttack = findViewById(R.id.btn_attack);

        battle_ui_container = findViewById(R.id.battle_ui_container);
        llBossBeatenMessage = findViewById(R.id.ll_boss_beaten_message);
        lost_boss_message = findViewById(R.id.lost_boss_message);

        swordIcons = new ArrayList<>();
        swordIcons.add(findViewById(R.id.iv_sword1));
        swordIcons.add(findViewById(R.id.iv_sword2));
        swordIcons.add(findViewById(R.id.iv_sword3));
        swordIcons.add(findViewById(R.id.iv_sword4));
        swordIcons.add(findViewById(R.id.iv_sword5));
    }

    private void setupInitialState() {
        if (currentBoss == null) {
            Toast.makeText(this, "Boss not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentBoss.isBossBeaten()) {
            // Boss je pobeđen
            battle_ui_container.setVisibility(View.GONE);
            llBossBeatenMessage.setVisibility(View.VISIBLE);
            lost_boss_message.setVisibility(View.GONE);
            ivBoss.setVisibility(View.GONE); // Sakrij i sliku bossa koja je van kontejnera
        } else {


        //Da li je boss zmaj?
        isDragon = currentBoss.getBossLevel() % 5 == 0;


        if (isDragon) {
            ivBackground.setImageResource(R.drawable.dragon_cave);
            ivBoss.setImageResource(R.drawable.dragon_idle_nb);
        } else {
            ivBackground.setImageResource(R.drawable.goblin_forest);
            ivBoss.setImageResource(R.drawable.goblin_idle_nb);
        }


        maxBossHp = currentBoss.getBossHp();
        pbBossHp.setMax(100);
        updateHpUI();

        tvGoldAmount.setText(String.valueOf((int) currentBoss.getBossGold()));
        tvAttackChance.setText(String.format("%d%%", attackChance));
        tvUserPp.setText(String.valueOf((int) userPP));

        // TODO: Implementirati logiku za prikaz mogućih i trenutnih itema
        }
    }

    private void performAttack() {
        if (isAttackInProgress) {
            return;
        }

        isAttackInProgress = true;
        attacksLeft--;
        updateSwordVisibility();

        boolean isSuccess = bossService.isAttackSuccessful(attackChance);

        if (isSuccess) {

            float newHp = currentBoss.getBossHp() - userPP;
            currentBoss.setBossHp(newHp > 0 ? newHp : 0);
            updateHpUI();

            ivBoss.setImageResource(isDragon ? R.drawable.dragon_hurt_nb : R.drawable.goblin_hurt_nb);

            if (currentBoss.getBossHp() <= 0) {
                handleBossDefeated(userLvl);
            }
        } else {

            ivBoss.setImageResource(isDragon ? R.drawable.dragon_attack_nb : R.drawable.goblin_attack_nb);
        }

        // Vrati bossa u "idle" stanje nakon 1.5 sekunde
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!currentBoss.isBossBeaten()) {
                ivBoss.setImageResource(isDragon ? R.drawable.dragon_idle_nb : R.drawable.goblin_idle_nb);
            }
            isAttackInProgress = false;
        }, 1500);

        if(currentBoss.getBossHp() > 0 && attacksLeft <= 0) {
            lost_boss_message.setVisibility(View.VISIBLE);
            btnAttack.setEnabled(false);
        }

    }


    @SuppressLint("DefaultLocale")
    private void updateHpUI() {
        tvBossHp.setText(String.format("%.0f/%.0f", currentBoss.getBossHp(), maxBossHp));
        int progress = (int) ((currentBoss.getBossHp() / maxBossHp) * 100);
        pbBossHp.setProgress(progress);
    }

    /**
     * Sakriva ikonicu mača nakon svakog iskorišćenog napada.
     */
    private void updateSwordVisibility() {
        if (attacksLeft >= 0 && attacksLeft < 5) {
            // Sakriva poslednji vidljiv mač
            swordIcons.get(attacksLeft).setVisibility(View.INVISIBLE);
        }
    }

    private void handleBossDefeated(int userLvl) {

        currentBoss.setBossHp(maxBossHp);
        bossService.beatBoss(currentBoss,userLvl);
        Toast.makeText(this, "Boss defeated!", Toast.LENGTH_LONG).show();

        // TODO: Implementirati logiku za dodavanje nagrada korisniku

        btnAttack.setEnabled(false);
    }
}