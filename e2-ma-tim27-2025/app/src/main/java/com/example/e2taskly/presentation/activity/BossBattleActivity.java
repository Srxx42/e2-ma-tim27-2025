package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.service.BossService;

import java.util.ArrayList;
import java.util.List;

public class BossBattleActivity extends AppCompatActivity {

    private BossService bossService;
    private Boss currentBoss;

    // UI Elementi
    private RelativeLayout rootLayout;
    private ImageView ivBackground, ivBoss;
    private ProgressBar pbBossHp;
    private TextView tvBossHp, tvUserPp, tvGoldAmount, tvAttackChance;
    private ImageButton btnAttack;
    private List<ImageView> swordIcons;

    private int attacksLeft = 5;
    private final int ATTACK_CHANCE_PERCENTAGE = 63; // Primer procenta, ovo bi trebalo doći iz korisničkih podataka
    private final int USER_POWER_POINTS = 80; // Primer PP, takođe treba doći iz korisničkih podataka
    private float maxBossHp;
    private boolean isDragon;
    private boolean isAttackInProgress = false; // Sprečava višestruke brze klikove

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        bossService = new BossService(this);

        // TODO: Dohvatiti pravog bossa, npr. preko Intenta
        // Primer kreiranja bossa za testiranje
        // currentBoss = bossService.getByEnemyId("nekiUserId", false);
        // if (currentBoss == null) {
        //     bossService.createBoss("nekiUserId", false, 1);
        //     currentBoss = bossService.getByEnemyId("nekiUserId", false);
        // }

        // Hardkodovan boss za demonstraciju
        currentBoss = new Boss(1, "userId1", 5, 1000, 500, false, false, null);


        initializeUI();
        setupInitialState();

        btnAttack.setOnClickListener(v -> performAttack());
    }

    /**
     * Inicijalizuje sve UI komponente sa layout-a.
     */
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

        swordIcons = new ArrayList<>();
        swordIcons.add(findViewById(R.id.iv_sword1));
        swordIcons.add(findViewById(R.id.iv_sword2));
        swordIcons.add(findViewById(R.id.iv_sword3));
        swordIcons.add(findViewById(R.id.iv_sword4));
        swordIcons.add(findViewById(R.id.iv_sword5));
    }

    /**
     * Postavlja početno stanje ekrana na osnovu podataka o bossu.
     */
    private void setupInitialState() {
        if (currentBoss == null) {
            Toast.makeText(this, "Boss not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Provera da li je boss zmaj
        isDragon = currentBoss.getBossLevel() % 5 == 0;

        // Postavljanje pozadine i idle slike bossa
        if (isDragon) {
            ivBackground.setImageResource(R.drawable.dragon_cave);
            ivBoss.setImageResource(R.drawable.dragon_idle_nb);
        } else {
            ivBackground.setImageResource(R.drawable.goblin_forest);
            ivBoss.setImageResource(R.drawable.goblin_idle_nb);
        }

        // Postavljanje HP bara
        maxBossHp = currentBoss.getBossHp();
        pbBossHp.setMax(100);
        updateHpUI();

        // Postavljanje ostalih informacija
        tvUserPp.setText(String.format("%d PP", USER_POWER_POINTS));
        tvGoldAmount.setText(String.valueOf((int)currentBoss.getBossGold()));
        tvAttackChance.setText(String.format("%d%%", ATTACK_CHANCE_PERCENTAGE));

        // TODO: Implementirati logiku za prikaz mogućih i trenutnih itema
    }

    /**
     * Izvršava logiku napada kada korisnik klikne na dugme.
     */
    private void performAttack() {
        if (attacksLeft <= 0 || isAttackInProgress || currentBoss.isBossBeaten()) {
            return; // Nema više napada, napad je u toku ili je boss pobeđen
        }

        isAttackInProgress = true;
        attacksLeft--;
        updateSwordVisibility();

        boolean isSuccess = bossService.isAttackSuccessful(ATTACK_CHANCE_PERCENTAGE);

        if (isSuccess) {
            // Smanji HP bossa
            float newHp = currentBoss.getBossHp() - USER_POWER_POINTS;
            currentBoss.setBossHp(newHp > 0 ? newHp : 0);
            updateHpUI();

            // Pusti "hurt" animaciju
            ivBoss.setImageResource(isDragon ? R.drawable.dragon_hurt_nb : R.drawable.goblin_hurt_nb);

            if (currentBoss.getBossHp() <= 0) {
                handleBossDefeated();
            }
        } else {
            // Pusti "attack" animaciju
            ivBoss.setImageResource(isDragon ? R.drawable.dragon_attack_nb : R.drawable.goblin_attack_nb);
        }

        // Vrati bossa u "idle" stanje nakon 1.5 sekunde
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!currentBoss.isBossBeaten()) {
                ivBoss.setImageResource(isDragon ? R.drawable.dragon_idle_nb : R.drawable.goblin_idle_nb);
            }
            isAttackInProgress = false;
        }, 1500);
    }

    /**
     * Ažurira prikaz HP-a (i progress bar i tekst).
     */
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

    /**
     * Upravlja logikom kada je boss pobeđen.
     */
    private void handleBossDefeated() {
        // Ažuriraj status bossa u bazi
        // bossService.beatBoss(currentBoss);
        Toast.makeText(this, "Boss defeated!", Toast.LENGTH_LONG).show();

        // TODO: Implementirati logiku za dodavanje nagrada korisniku

        // Onemogući dalje napade
        btnAttack.setEnabled(false);
        // Ovde možeš dodati i animaciju nestajanja bossa ili prelazak na drugi ekran
    }
}