package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.UserInventoryItem;
import com.example.e2taskly.presentation.adapter.EquipmentAdapter;
import com.example.e2taskly.service.EquipmentService;
import com.example.e2taskly.service.UserService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentActivity extends AppCompatActivity implements EquipmentAdapter.OnActivateClickListener {

    private RecyclerView recyclerView;
    private EquipmentAdapter adapter;
    private EquipmentService equipmentService;
    private UserService userService;
    private User currentUser;
    private List<UserInventoryItem> inventoryList = new ArrayList<>();
    private Map<String, EquipmentTemplate> templateMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_equipment);

        recyclerView = findViewById(R.id.recyclerViewEquipment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        equipmentService = new EquipmentService(this);
        userService = new UserService(this);

        loadInventoryAndTemplates();
    }

    private void loadInventoryAndTemplates() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        Task<User> userTask = userService.getUserProfile(currentUserId);
        Task<List<UserInventoryItem>> inventoryTask = equipmentService.getUserInventory(currentUserId);
        Task<List<EquipmentTemplate>> templatesTask = equipmentService.getEquipmentTemplates();

        Tasks.whenAllSuccess(userTask, inventoryTask, templatesTask).addOnSuccessListener(results -> {
            this.currentUser = (User) results.get(0);
            inventoryList.clear();
            inventoryList.addAll((List<UserInventoryItem>) results.get(1));

            List<EquipmentTemplate> templates = (List<EquipmentTemplate>) results.get(2);
            templateMap.clear();
            for (EquipmentTemplate template : templates) {
                templateMap.put(template.getId(), template);
            }

            setupRecyclerView();
        }).addOnFailureListener(e -> {
            Toast.makeText(EquipmentActivity.this, "Failed to load equipment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new EquipmentAdapter(currentUser, inventoryList, templateMap, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivateClick(UserInventoryItem item) {
        String userId = userService.getCurrentUserId();
        EquipmentTemplate template = templateMap.get(item.getTemplateId());
        if (userId != null && template != null) {
            equipmentService.activateItemForBattle(userId, item, template).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, template.getName() + " activated!", Toast.LENGTH_SHORT).show();
                    loadInventoryAndTemplates();
                } else {
                    Toast.makeText(this, "Failed to activate item.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public void onUpgradeClick(UserInventoryItem item) {
        EquipmentTemplate template = templateMap.get(item.getTemplateId());
        if (template == null) return;

        equipmentService.upgradeWeapon(currentUser, item, template)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, template.getName() + " upgraded!", Toast.LENGTH_SHORT).show();
                        loadInventoryAndTemplates();
                    } else {
                        Toast.makeText(this, "Upgrade failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}