package com.example.e2taskly.presentation.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.e2taskly.R;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.service.BossService;
import com.example.e2taskly.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossBattleActivity extends AppCompatActivity implements SensorEventListener {

    private BossService bossService;
    private UserService userService;
    private Boss currentBoss;

    // UI Elementi
    private RelativeLayout rootLayout;
    private ImageView boss_background, ivBoss;
    private ProgressBar bossHpBar;
    private TextView tvBossHp, tvUserPp, tvGoldAmount, tvAttackChance;
    private ImageButton btnAttack;
    private List<ImageView> swordIcons;

    //UI Layouts
    private RelativeLayout battle_ui_container;
    private LinearLayout llBossBeatenMessage, lost_boss_message;

    // UI elementi za nagrade
    private RelativeLayout rewardOverlayContainer;
    private ImageView ivChestGif;
    private LinearLayout llRewardsContainer;
    private TextView tvRewardGoldAmount;
    private LinearLayout llRewardItems;

    // Za detekciju potresa
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;
    private static final int SHAKE_THRESHOLD = 800; // Podesi osetljivost

    private boolean isChestOpen = false; // Sprečava višestruko otvaranje

    private int attacksLeft = 5;
    private  int attackChance ;
    private  int userPP ;
    private int userLvl;
    private float maxBossHp;

    private int goldToGet;
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

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener( this);
        }
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
        boss_background = findViewById(R.id.boss_background);
        ivBoss = findViewById(R.id.iv_boss);
        bossHpBar = findViewById(R.id.boss_hp_bar);
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

        // Inicijalizacija UI elemenata za nagrade
        rewardOverlayContainer = findViewById(R.id.reward_overlay_container);
        ivChestGif = findViewById(R.id.iv_chest_gif);
        llRewardsContainer = findViewById(R.id.ll_rewards_container);
        tvRewardGoldAmount = findViewById(R.id.tv_reward_gold_amount);
        llRewardItems = findViewById(R.id.ll_reward_items);

        setupShakeDetector();
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
            ivBoss.setVisibility(View.GONE);
        }else if (currentBoss.isDidUserFightIt()) {
            // Korisnik je vec pokusao da pobedi boss-a
            battle_ui_container.setVisibility(View.GONE);
            llBossBeatenMessage.setVisibility(View.GONE);
            lost_boss_message.setVisibility(View.VISIBLE);
            ivBoss.setVisibility(View.GONE);
        } else {


        //Da li je boss zmaj?
        isDragon = currentBoss.getBossLevel() % 5 == 0;


        if (isDragon) {
            boss_background.setImageResource(R.drawable.dragon_cave);
            ivBoss.setImageResource(R.drawable.dragon_idle_nb);
        } else {
            boss_background.setImageResource(R.drawable.goblin_forest);
            ivBoss.setImageResource(R.drawable.goblin_idle_nb);
        }


        maxBossHp = currentBoss.getBossHp();
        goldToGet =(int) currentBoss.getBossGold();
        bossHpBar.setMax(100);
        updateHpUI();

        tvGoldAmount.setText(String.valueOf((int) currentBoss.getBossGold()));
        tvAttackChance.setText(String.format("%d%%", attackChance));
        tvUserPp.setText(String.valueOf((int) userPP));

        // TODO: Implementirati logiku za prikaz mogućih i trenutnih itema
        }
    }

    private void performAttack() {
        if (isAttackInProgress || attacksLeft <= 0) {
            return;
        }

        isAttackInProgress = true;
        attacksLeft--;
        updateSwordVisibility();


        boolean isSuccess = bossService.isAttackSuccessful(attackChance);
        executeAttackSequence(isSuccess);
    }
    private void executeAttackSequence(boolean isSuccess) {
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
            Toast.makeText(this, "Miss!", Toast.LENGTH_SHORT).show(); // Dodajemo informaciju o promašaju
        }

        // Vrati bossa u "idle" stanje nakon 1.5 sekunde
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (!currentBoss.isBossBeaten()) {
                ivBoss.setImageResource(isDragon ? R.drawable.dragon_idle_nb : R.drawable.goblin_idle_nb);
            }
            isAttackInProgress = false;
        }, 1500);


        if(currentBoss.getBossHp() > 0 && attacksLeft <= 0) {
            boolean lowerThanHalf = currentBoss.getBossHp() < maxBossHp / 2;

            if(lowerThanHalf){
                handleBossNotBeaten(true);

            } else {
                handleBossNotBeaten(false);
            }
        }
    }

    private void performShakeAttack() {
        if (isAttackInProgress || attacksLeft <= 0) {
            return;
        }

        isAttackInProgress = true;
        attacksLeft--;
        updateSwordVisibility();


        Random random = new Random();
        int roll = random.nextInt(101);
        boolean isSuccess = roll < attackChance;

        executeAttackSequence(isSuccess);
    }

    @SuppressLint("DefaultLocale")
    private void updateHpUI() {
        tvBossHp.setText(String.format("%.0f/%.0f", currentBoss.getBossHp(), maxBossHp));
        int progress = (int) ((currentBoss.getBossHp() / maxBossHp) * 100);
        bossHpBar.setProgress(progress);
    }

    private void updateSwordVisibility() {
        if (attacksLeft >= 0 && attacksLeft < 5) {
            // Sakriva poslednji vidljiv mač
            swordIcons.get(attacksLeft).setVisibility(View.INVISIBLE);
        }
    }

    private void handleBossDefeated(int userLvl) {

        currentBoss.setBossHp(maxBossHp);
        currentBoss.setDidUserFightIt(true);
        bossService.beatBoss(currentBoss,userLvl);
        if (isDragon) {
            ivBoss.setImageResource(R.drawable.dragon_hurt_nb);
        } else {
            ivBoss.setImageResource(R.drawable.goblin_hurt_nb);
        }
        Toast.makeText(this, "Boss defeated!", Toast.LENGTH_LONG).show();

        // TODO: Implementirati logiku za dodavanje nagrada korisniku

        btnAttack.setEnabled(false);
        showRewardScreen(true);
    }
    public void handleBossNotBeaten(boolean isHalflyBeaten){
        currentBoss.setBossHp(maxBossHp);
        currentBoss.setDidUserFightIt(true);
        bossService.updateBoss(currentBoss);

        if (isDragon) {
            ivBoss.setImageResource(R.drawable.dragon_attack_nb);
        } else {
            ivBoss.setImageResource(R.drawable.goblin_attack_nb);
        }

        btnAttack.setEnabled(false);
        if(isHalflyBeaten){
            Toast.makeText(this, "Good attempt, you get reduced reward!", Toast.LENGTH_LONG).show();
            showRewardScreen(false);
        } else{
            Toast.makeText(this, "More luck next time warrior!", Toast.LENGTH_LONG).show();
            lost_boss_message.setVisibility(View.VISIBLE);
        }

    }

    private void showRewardScreen(boolean isBossBeaten) {
        isChestOpen = false;
        rewardOverlayContainer.setVisibility(View.VISIBLE);
        llRewardsContainer.setVisibility(View.INVISIBLE); // Sakrijemo nagrade pre otvaranja

        // Učitaj GIF zatvorenog kovčega koji se ponavlja
        Glide.with(this)
                .asGif()
                .load(R.drawable.chest_closed)
                .into(ivChestGif);

        // Postavi listener za klik na kovčeg
        ivChestGif.setOnClickListener(v -> {
            if (!isChestOpen) {
                openChest(isBossBeaten);
            }
        });
    }

    private void openChest(boolean isBossBeaten) {
        isChestOpen = true;
        ivChestGif.setClickable(false);

        // Učitaj GIF otvaranja kovčega koji se pušta samo jednom
        Glide.with(this)
                .asGif()
                .load(R.drawable.chest_opened)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        // Postavljamo da se animacija izvrši samo jednom
                        resource.setLoopCount(1);
                        return false;
                    }
                })
                .into(ivChestGif);

        // Postavljamo nagrade
        String currenUserId = userService.getCurrentUserId();
        if(isBossBeaten) {
            tvRewardGoldAmount.setText(String.valueOf(goldToGet));
            userService.addCoinsToUser(currenUserId,goldToGet);
        } else {
            int reducedGold = goldToGet / 2;
            tvRewardGoldAmount.setText(String.valueOf(reducedGold));
            userService.addCoinsToUser(currenUserId,reducedGold);
        }
        // TODO: Ovde dodajte logiku za prikaz ikonica itema u llRewardItems

        // Prikazujemo nagrade sa malim zakašnjenjem i fade-in animacijom
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            llRewardsContainer.setVisibility(View.VISIBLE);
            llRewardsContainer.startAnimation(fadeIn);
        }, 1200); // Podesi vreme da odgovara trajanju tvog GIF-a za otvaranje
    }

    private void setupShakeDetector() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        lastShakeTime = System.currentTimeMillis();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            // Postavljamo "cooldown" od 1 sekunde da se izbegne spamovanje potresima
            if ((currentTime - lastShakeTime) > 1000) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                // Proveravamo da li je potres dovoljno jak
                if (acceleration > SHAKE_THRESHOLD / 100.0) {
                    lastShakeTime = currentTime; // Resetujemo tajmer čim se detektuje validan potres

                    // Logika odlučivanja: Da li otvaramo kovčeg ili napadamo?

                    // 1. Ako je ekran sa nagradama vidljiv, potres otvara kovčeg.
                    if (rewardOverlayContainer.getVisibility() == View.VISIBLE && !isChestOpen) {

                        openChest(currentBoss.isBossBeaten());
                    }
                    // 2. Inače, ako je borba u toku, potres aktivira napad.
                    else if (battle_ui_container.getVisibility() == View.VISIBLE && attacksLeft > 0 && !isAttackInProgress) {
                        performShakeAttack();
                    }
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nije potrebno implementirati za ovaj slučaj
    }
}