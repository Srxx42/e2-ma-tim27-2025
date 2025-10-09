package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;

import com.example.e2taskly.data.repository.EquipmentRepository;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.UserInventoryItem;
import com.example.e2taskly.model.enums.BonusType;
import com.example.e2taskly.model.enums.EquipmentType;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Task<Void> activateItemForBattle(User user, UserInventoryItem itemToActivate, EquipmentTemplate template) {
        String userId = user.getUid();
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

                double basePp = user.getPowerPoints();
                if (template.getBonusType() == BonusType.PP_BOOST_TEMPORARY) {
                    basePp = user.getPowerPoints() / (1 + existingActiveItem.getCurrentBonusValue() / 100.0);
                }
                double newBonusValue = existingActiveItem.getCurrentBonusValue() + template.getBonusValue();
                int newTotalPp = (int) Math.round(basePp * (1 + newBonusValue / 100.0));
                itemToActivate.setFightsRemaining(template.getDurationInFights());
                itemToActivate.setActivated(true);
                Task<Void> updateItemTask = equipmentRepository.updateInventoryItem(userId, itemToActivate);
                Task<Void> updatePowerPoint = userService.updatePowerPoints(userId, newTotalPp);

                return Tasks.whenAll(updateItemTask, updatePowerPoint);

            } else {
                // Logic to activate a new item
                itemToActivate.setActivated(true);

                if (template.getType() == EquipmentType.POTION) {
                    itemToActivate.setFightsRemaining(1);
                } else if (template.getDurationInFights() > 0) {
                    itemToActivate.setFightsRemaining(template.getDurationInFights());
                } else if ( template.getType() == EquipmentType.WEAPON){
                    itemToActivate.setFightsRemaining(template.getDurationInFights());
                }

                int ppBonus = 0;
                if (template.getBonusType() == BonusType.PP_BOOST_TEMPORARY || template.getBonusType() == BonusType.PP_BOOST_PERMANENT) {
                    ppBonus = (int) Math.round(user.getPowerPoints() * (template.getBonusValue() / 100.0));
                }

                if (ppBonus > 0) {
                    int newTotalPp = user.getPowerPoints() + ppBonus;
                    Task<Void> updateItemTask = equipmentRepository.updateInventoryItem(userId, itemToActivate);
                    Task<Void> updateUserPpTask = userService.updatePowerPoints(userId, newTotalPp);
                    return Tasks.whenAll(updateItemTask, updateUserPpTask);
                } else {
                    return equipmentRepository.updateInventoryItem(userId, itemToActivate);
                }
            }
        });
    }

    public Task<Void> processInventoryAfterBattle(String userId) {
        Task<User> userTask = userService.getUserProfile(userId);
        Task<List<EquipmentTemplate>> templatesTask = getEquipmentTemplates();
        Task<List<UserInventoryItem>> inventoryTask = equipmentRepository.syncAndGetUserInventory(userId);

        return Tasks.whenAllSuccess(userTask, templatesTask, inventoryTask).continueWithTask(task -> {
            User user = (User) task.getResult().get(0);
            List<EquipmentTemplate> allTemplates = (List<EquipmentTemplate>) task.getResult().get(1);
            List<UserInventoryItem> inventory = (List<UserInventoryItem>) task.getResult().get(2);

            Map<String, EquipmentTemplate> templateMap = new HashMap<>();
            for (EquipmentTemplate t : allTemplates) {
                templateMap.put(t.getId(), t);
            }

            List<Task<Void>> updateTasks = new ArrayList<>();
            double currentPp = user.getPowerPoints();
            List<UserInventoryItem> expiredPpItems = new ArrayList<>();

            for (UserInventoryItem item : inventory) {
                if (item.isActivated()) {
                    item.setFightsRemaining(item.getFightsRemaining() - 1);

                    if (item.getFightsRemaining() <= 0) {
                        updateTasks.add(equipmentRepository.deleteInventoryItem(userId, item.getInventoryId()));

                        EquipmentTemplate template = templateMap.get(item.getTemplateId());
                        if (template != null && template.getBonusType() == BonusType.PP_BOOST_TEMPORARY) {
                            expiredPpItems.add(item);
                        }
                    } else {
                        updateTasks.add(equipmentRepository.updateInventoryItem(userId, item));
                    }
                }
            }

            for (UserInventoryItem expiredItem : expiredPpItems) {
                currentPp = currentPp / (1 + expiredItem.getCurrentBonusValue() / 100.0);
            }

            int finalPp = (int) Math.round(currentPp);

            if (finalPp != user.getPowerPoints()) {
                updateTasks.add(userService.updatePowerPoints(userId, finalPp));
            }

            return Tasks.whenAll(updateTasks);
        });
    }
    public Task<Void> upgradeWeapon(User user, UserInventoryItem weapon, EquipmentTemplate template) {
        if (template.getType() != EquipmentType.WEAPON) {
            return Tasks.forException(new Exception("Only weapons can be upgraded."));
        }

        int reward = levelingService.getCoinsRewardForLevel(user.getLevel());
        int upgradeCost = (int) Math.ceil(reward * (template.getUpgradeCostPercentage() / 100.0));

        if (user.getCoins() < upgradeCost) {
            return Tasks.forException(new Exception("Not enough coins to upgrade."));
        }

        double oldBonusValue = weapon.getCurrentBonusValue();
        double newBonusValue = oldBonusValue + 0.01;
        weapon.setCurrentBonusValue(newBonusValue);
        int newCoinAmount = user.getCoins() - upgradeCost;

        List<Task<Void>> tasks = new ArrayList<>();
        tasks.add(equipmentRepository.updateInventoryItem(user.getUid(), weapon));
        tasks.add(userService.updateUserCoins(user.getUid(), newCoinAmount));
        if (template.getBonusType() == BonusType.PP_BOOST_PERMANENT) {
            double currentTotalPp = user.getPowerPoints();


            double basePp = currentTotalPp / (1 + oldBonusValue / 100.0);


            int newTotalPp = (int) Math.round(basePp * (1 + newBonusValue / 100.0));

            if (newTotalPp != user.getPowerPoints()) {
                tasks.add(userService.updatePowerPoints(user.getUid(), newTotalPp));
            }
        }
        return Tasks.whenAll(tasks);

    }
    public Task<Void> handleDuplicateWeaponDrop(User user, EquipmentTemplate droppedWeaponTemplate) {
        return getUserInventory(user.getUid()).continueWithTask(inventoryTask -> {
            if (!inventoryTask.isSuccessful()) throw inventoryTask.getException();

            UserInventoryItem existingWeapon = null;
            for (UserInventoryItem item : inventoryTask.getResult()) {
                if (item.getTemplateId().equals(droppedWeaponTemplate.getId())) {
                    existingWeapon = item;
                    break;
                }
            }

            if (existingWeapon != null) {
                double oldBonusValue = existingWeapon.getCurrentBonusValue();
                double newBonusValue = oldBonusValue + 0.02;
                existingWeapon.setCurrentBonusValue(newBonusValue);
                List<Task<Void>> tasks = new ArrayList<>();
                tasks.add(equipmentRepository.updateInventoryItem(user.getUid(), existingWeapon));
                if (droppedWeaponTemplate.getBonusType() == BonusType.PP_BOOST_PERMANENT) {
                    double basePp = user.getPowerPoints() / (1 + oldBonusValue / 100.0);
                    int newTotalPp = (int) Math.round(basePp * (1 + newBonusValue / 100.0));

                    if (newTotalPp != user.getPowerPoints()) {
                        tasks.add(userService.updatePowerPoints(user.getUid(), newTotalPp));
                    }
                }
                return Tasks.whenAll(tasks);
            } else {
                UserInventoryItem newItem = new UserInventoryItem();
                newItem.setTemplateId(droppedWeaponTemplate.getId());
                newItem.setCurrentBonusValue(droppedWeaponTemplate.getBonusValue());
                newItem.setActivated(false);
                newItem.setFightsRemaining(0);
                return equipmentRepository.buyInventoryItem(user.getUid(), newItem).continueWith(task -> null);
            }
        });
    }
}

