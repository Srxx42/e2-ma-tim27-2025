package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.EquipmentRepository;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.UserInventoryItem;
import com.example.e2taskly.model.enums.EquipmentType;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final UserService userService;

    public EquipmentService(Context context){
        this.equipmentRepository = new EquipmentRepository(context);
        this.userService = new UserService(context);
    }
    public int calculateItemPrice(EquipmentTemplate template, int previousBossReward){
        if(template == null || template.getCostPercentage()<=0){
            return 0;
        }
        return (int) Math.ceil((template.getCostPercentage()/100.0)*previousBossReward);
    }
    public Task<List<EquipmentTemplate>> getShopEquipment() {
        return equipmentRepository.syncAndGetEquipmentTemplates().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<EquipmentTemplate> allTemplates = task.getResult();
            List<EquipmentTemplate> shopItems = new ArrayList<>();

            if (allTemplates != null) {
                for (EquipmentTemplate template : allTemplates) {
                    if (template.getType() == EquipmentType.POTION ||
                            template.getType() == EquipmentType.CLOTHING) {
                        shopItems.add(template);
                    }
                }
            }
            return shopItems;
        });
    }
    public Task<Void> purchaseItem(User user, EquipmentTemplate template, int itemPrice){
        if(user.getCoins()<itemPrice){
            return Tasks.forException(new Exception("You don't have enough coins!"));
        }
        UserInventoryItem newItem = new UserInventoryItem();
        newItem.setTemplateId(template.getId());
        newItem.setCurrentBonusValue(template.getBonusValue());
        newItem.setActivated(false);
        newItem.setFightsRemaining(0);

        return equipmentRepository.buyInventoryItem(user.getUid(), newItem).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            int newCoinAmount = user.getCoins() - itemPrice;
            return userService.updateUserCoins(user.getUid(), newCoinAmount);
        });
    }
    public Task<List<UserInventoryItem>> getUserInventory(String userId) {
        return equipmentRepository.syncAndGetUserInventory(userId);
    }
    public Task<Void> activateItemForBattle(String userId, UserInventoryItem item, EquipmentTemplate template) {
        if(item.isActivated()) {
            return Tasks.forResult(null);
        }

        item.setActivated(true);
        if (template.getDurationInFights() > 0) {
            item.setFightsRemaining(template.getDurationInFights());
        }

        return equipmentRepository.updateInventoryItem(userId, item);
    }
    public Task<Void> processInventoryAfterBattle(String userId) {
        return equipmentRepository.syncAndGetUserInventory(userId).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<UserInventoryItem> inventory = task.getResult();
            List<Task<Void>> updateTasks = new ArrayList<>();

            for (UserInventoryItem item : inventory) {
                if (item.isActivated()) {
                    if (item.getFightsRemaining() == 0) {
                        updateTasks.add(equipmentRepository.deleteInventoryItem(userId, item.getInventoryId()));
                    }
                    else if (item.getFightsRemaining() > 0) {
                        item.setFightsRemaining(item.getFightsRemaining() - 1);
                        if (item.getFightsRemaining() <= 0) {
                            item.setActivated(false);
                            updateTasks.add(equipmentRepository.deleteInventoryItem(userId, item.getInventoryId()));
                        } else {
                            updateTasks.add(equipmentRepository.updateInventoryItem(userId, item));
                        }
                    }
                }
            }

            return Tasks.whenAll(updateTasks);
        });
    }
}

