package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.enums.ProgressType;
import com.example.e2taskly.presentation.adapter.ShopAdapter;
import com.example.e2taskly.service.EquipmentService;
import com.example.e2taskly.service.LevelingService;
import com.example.e2taskly.service.MissionProgressService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;

import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageView menuButton;
    private ShopAdapter shopAdapter;
    private EquipmentService equipmentService;
    private LevelingService levelingService;
    private UserService userService;
    private MissionProgressService missionProgressService;
    private List<EquipmentTemplate> equipmentList;
    private User currentUser;
    private SharedPreferencesUtil sharedPreferences;
    private int previousBossReward = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        EdgeToEdge.enable(this);
        recyclerView = findViewById(R.id.recyclerViewShop);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        equipmentService = new EquipmentService(this);
        levelingService = new LevelingService();
        sharedPreferences = new SharedPreferencesUtil(this);
        missionProgressService = new MissionProgressService(this);
        userService = new UserService(this);
        sharedPreferences = new SharedPreferencesUtil(this);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setVisibility(View.GONE);

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            int originalPaddingLeft = v.getPaddingLeft();
            int originalPaddingTop = v.getPaddingTop();
            int originalPaddingRight = v.getPaddingRight();

            v.setPadding(
                    originalPaddingLeft,
                    originalPaddingTop,
                    originalPaddingRight,
                    insets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
        loadShopItems();
    }

    private void loadShopItems() {
        String currentUserId = sharedPreferences.getActiveUserUid();
        if (currentUserId != null) {
            userService.getUserProfile(currentUserId).addOnSuccessListener(user -> {
                currentUser = user;
                previousBossReward = levelingService.getCoinsRewardForLevel(currentUser.getLevel());
                equipmentService.getShopEquipment().addOnSuccessListener(templates -> {
                    equipmentList = templates;
                    Log.e("Greska",equipmentList.toString());
                    shopAdapter = new ShopAdapter(this, equipmentList, this::onPurchaseClick, previousBossReward);
                    recyclerView.setAdapter(shopAdapter);
                });
            });
        }
    }

    private void onPurchaseClick(EquipmentTemplate item) {
        int itemPrice = equipmentService.calculateItemPrice(item, previousBossReward);
        if (currentUser.getCoins() >= itemPrice) {
            equipmentService.purchaseItem(currentUser, item, itemPrice).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show();
                    missionProgressService.updateMissionProgress(currentUser.getUid(), ProgressType.SHOPPING);
                    currentUser.setCoins(currentUser.getCoins() - itemPrice);
                } else {
                    Toast.makeText(this, "Purchase failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "You don't have enough coins.", Toast.LENGTH_SHORT).show();
        }
    }
}