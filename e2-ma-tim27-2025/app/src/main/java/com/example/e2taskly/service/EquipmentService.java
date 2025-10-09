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
    private final LevelingService levelingService;
    private final UserService userService;

    public EquipmentService(Context context){
        this.equipmentRepository = new EquipmentRepository(context);
        this.levelingService = new LevelingService();
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
    public Task<List<EquipmentTemplate>> getEquipmentTemplates() {
        return equipmentRepository.syncAndGetEquipmentTemplates().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<EquipmentTemplate> allTemplates = task.getResult();
            List<EquipmentTemplate> items = new ArrayList<>();

            if (allTemplates != null) {
                for (EquipmentTemplate template : allTemplates) {
                        items.add(template);
                    }
                }
            return items;
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
    public Task<Void> activateItemForBattle(String userId, UserInventoryItem itemToActivate, EquipmentTemplate template) {
        if (itemToActivate.isActivated()) {
            return Tasks.forResult(null);
        }

        return getUserInventory(userId).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<UserInventoryItem> currentInventory = task.getResult();
            UserInventoryItem existingActiveItem = null;

            if (template.getType() == EquipmentType.CLOTHING) {
                for (UserInventoryItem item : currentInventory) {
                    if (item.getTemplateId().equals(itemToActivate.getTemplateId()) && item.isActivated()) {
                        existingActiveItem = item;
                        break;
                    }
                }
            }

            if (existingActiveItem != null) {
                double newBonus = existingActiveItem.getCurrentBonusValue() + template.getBonusValue();
                existingActiveItem.setCurrentBonusValue(newBonus);

                existingActiveItem.setFightsRemaining(template.getDurationInFights());

                Task<Void> updateTask = equipmentRepository.updateInventoryItem(userId, existingActiveItem);
                Task<Void> deleteTask = equipmentRepository.deleteInventoryItem(userId, itemToActivate.getInventoryId());

                return Tasks.whenAll(updateTask, deleteTask);

            } else {
                itemToActivate.setActivated(true);

                if (template.getDurationInFights() > 0) {
                    itemToActivate.setFightsRemaining(template.getDurationInFights());
                } else if (template.getType() == EquipmentType.POTION) {
                    itemToActivate.setFightsRemaining(1);
                } else if(template.getType() == EquipmentType.WEAPON) {
                    itemToActivate.setFightsRemaining(template.getDurationInFights());
                }

                return equipmentRepository.updateInventoryItem(userId, itemToActivate);
            }
        });
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
    public Task<Void> upgradeWeapon(User user, UserInventoryItem weapon, EquipmentTemplate template) {
        if (template.getType() != EquipmentType.WEAPON) {
            return Tasks.forException(new Exception("Only weapons can be upgraded."));
        }

        int reward = levelingService.getCoinsRewardForLevel(user.getLevel());
        int upgradeCost = (int) (reward * 0.60);

        if (user.getCoins() < upgradeCost) {
            return Tasks.forException(new Exception("Not enough coins to upgrade."));
        }

        double newBonus = weapon.getCurrentBonusValue() + 0.01;
        weapon.setCurrentBonusValue(newBonus);
        int newCoinAmount = user.getCoins() - upgradeCost;

        Task<Void> updateWeaponTask = equipmentRepository.updateInventoryItem(user.getUid(), weapon);
        Task<Void> updateUserCoinsTask = userService.updateUserCoins(user.getUid(), newCoinAmount);

        return Tasks.whenAll(updateWeaponTask, updateUserCoinsTask);
    }
    public Task<Void> handleDuplicateWeaponDrop(String userId, EquipmentTemplate droppedWeaponTemplate) {
        return getUserInventory(userId).continueWithTask(inventoryTask -> {
            if (!inventoryTask.isSuccessful()) throw inventoryTask.getException();

            UserInventoryItem existingWeapon = null;
            for (UserInventoryItem item : inventoryTask.getResult()) {
                if (item.getTemplateId().equals(droppedWeaponTemplate.getId())) {
                    existingWeapon = item;
                    break;
                }
            }

            if (existingWeapon != null) {
                double newBonus = existingWeapon.getCurrentBonusValue() + 0.02;
                existingWeapon.setCurrentBonusValue(newBonus);
                return equipmentRepository.updateInventoryItem(userId, existingWeapon);
            } else {
                UserInventoryItem newItem = new UserInventoryItem();
                newItem.setTemplateId(droppedWeaponTemplate.getId());
                newItem.setCurrentBonusValue(droppedWeaponTemplate.getBonusValue());
                newItem.setActivated(false);
                newItem.setFightsRemaining(0);
                return equipmentRepository.buyInventoryItem(userId, newItem).continueWith(task -> null);
            }
        });
    }
}

