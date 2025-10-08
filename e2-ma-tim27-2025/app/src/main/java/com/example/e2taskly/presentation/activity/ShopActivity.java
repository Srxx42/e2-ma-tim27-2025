package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.util.Log;
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
import com.example.e2taskly.presentation.adapter.ShopAdapter;
import com.example.e2taskly.service.EquipmentService;
import com.example.e2taskly.service.UserService;

import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private EquipmentService equipmentService;
    private UserService userService;
    private List<EquipmentTemplate> equipmentList;
    private User currentUser;
    private int previousBossReward = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        EdgeToEdge.enable(this);
        recyclerView = findViewById(R.id.recyclerViewShop);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        equipmentService = new EquipmentService(this);
        userService = new UserService(this);

        loadShopItems();
    }

    private void loadShopItems() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId != null) {
            userService.getUserProfile(currentUserId).addOnSuccessListener(user -> {
                currentUser = user;
                // Ovde bi trebalo implementirati logiku za dobavljanje nagrade prethodnog bosa
                // previousBossReward = ...
                previousBossReward = 200;
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