package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;

import com.example.e2taskly.data.repository.EquipmentRepository;
import com.example.e2taskly.model.ActiveBonuses;
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
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final LevelingService levelingService;
    private final UserService userService;

    private List<EquipmentTemplate> allTemplatesCache = null;

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

    public Task<List<UserInventoryItem>> getUserActiveItems(String userId) {
        Task<List<UserInventoryItem>> originalTask = equipmentRepository.syncAndGetUserInventory(userId);

        return originalTask.continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            List<UserInventoryItem> allItems = task.getResult();
            if (allItems == null) {
                return new ArrayList<UserInventoryItem>();
            }

            List<UserInventoryItem> activeItems = allItems.stream()
                    .filter(UserInventoryItem::isActivated)
                    .collect(Collectors.toList());

            return activeItems;
        });
    }
    public Task<ActiveBonuses> getActiveBonusesForBattle(String userId) {
        Task<List<UserInventoryItem>> inventoryTask = getUserInventory(userId);
        Task<List<EquipmentTemplate>> templatesTask = getEquipmentTemplates();

        return Tasks.whenAllSuccess(inventoryTask, templatesTask).onSuccessTask(results -> {
            List<UserInventoryItem> inventory = (List<UserInventoryItem>) results.get(0);
            List<EquipmentTemplate> allTemplates = (List<EquipmentTemplate>) results.get(1);

            Map<String, EquipmentTemplate> templateMap = new HashMap<>();
            for (EquipmentTemplate t : allTemplates) {
                templateMap.put(t.getId(), t);
            }

            ActiveBonuses bonuses = new ActiveBonuses();
            double totalTemporaryPpBonusPercent = 0.0;

            for (UserInventoryItem item : inventory) {
                if (item.isActivated()) {
                    EquipmentTemplate template = templateMap.get(item.getTemplateId());
                    if (template != null) {
                        switch (template.getBonusType()) {
                            case PP_BOOST_TEMPORARY:
                                totalTemporaryPpBonusPercent += item.getCurrentBonusValue();
                                break;
                            case ATTACK_CHANCE_BOOST:
                                bonuses.setSuccessChanceBonus(bonuses.getSuccessChanceBonus() + item.getCurrentBonusValue());
                                break;
                            case EXTRA_ATTACK_CHANCE:
                                bonuses.setExtraAttackChance(bonuses.getExtraAttackChance() + item.getCurrentBonusValue());
                                break;
                            case COIN_GAIN_BOOST:
                                bonuses.setCoinGainBonusPercent(bonuses.getCoinGainBonusPercent() + item.getCurrentBonusValue());
                                break;
                        }
                    }
                }
            }

            bonuses.setPpMultiplier(1.0 + totalTemporaryPpBonusPercent / 100.0);
            return Tasks.forResult(bonuses);
        });
    }
    public Task<Void> activateItemForBattle(User user, UserInventoryItem itemToActivate, EquipmentTemplate template) {
        String userId = user.getUid();
        if (itemToActivate.isActivated()) {
            return Tasks.forResult(null);
        }

        itemToActivate.setActivated(true);
        itemToActivate.setFightsRemaining(template.getDurationInFights());

        if (template.getType() == EquipmentType.CLOTHING) {
            return getUserInventory(userId).continueWithTask(inventoryTask -> {
                if (!inventoryTask.isSuccessful() || inventoryTask.getResult() == null) {
                    throw Objects.requireNonNull(inventoryTask.getException());
                }
                UserInventoryItem existingActiveItem = null;
                for (UserInventoryItem item : inventoryTask.getResult()) {
                    if (item.getTemplateId().equals(itemToActivate.getTemplateId()) && item.isActivated()) {
                        existingActiveItem = item;
                        break;
                    }
                }

                if (existingActiveItem != null) {
                    double newTotalBonus = existingActiveItem.getCurrentBonusValue() + template.getBonusValue();
                    existingActiveItem.setCurrentBonusValue(newTotalBonus);
                    return Tasks.whenAll(
                            equipmentRepository.updateInventoryItem(userId, existingActiveItem),
                            equipmentRepository.deleteInventoryItem(userId, itemToActivate.getInventoryId())
                    );
                } else {
                    return equipmentRepository.updateInventoryItem(userId, itemToActivate);
                }
            });
        }

        if (template.getBonusType() == BonusType.PP_BOOST_PERMANENT) {
            int ppBonus = (int) Math.round(user.getPowerPoints() * (template.getBonusValue() / 100.0));
            int newTotalPp = user.getPowerPoints() + ppBonus;

            return Tasks.whenAll(
                    equipmentRepository.updateInventoryItem(userId, itemToActivate),
                    userService.updatePowerPoints(userId, newTotalPp)
            );
        } else {
            return equipmentRepository.updateInventoryItem(userId, itemToActivate);
        }
    }
    public Task<Void> processInventoryAfterBattle(String userId) {
        return getUserInventory(userId).continueWithTask(inventoryTask -> {
            if (!inventoryTask.isSuccessful() || inventoryTask.getResult() == null) {
                throw Objects.requireNonNull(inventoryTask.getException());
            }

            List<UserInventoryItem> inventory = inventoryTask.getResult();
            List<Task<Void>> updateTasks = new ArrayList<>();

            for (UserInventoryItem item : inventory) {
                if (item.isActivated() && item.getFightsRemaining() > 0) {
                    item.setFightsRemaining(item.getFightsRemaining() - 1);

                    if (item.getFightsRemaining() == 0) {
                        updateTasks.add(equipmentRepository.deleteInventoryItem(userId, item.getInventoryId()));
                    } else {
                        updateTasks.add(equipmentRepository.updateInventoryItem(userId, item));
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
        int upgradeCost = (int) Math.ceil(reward * (template.getUpgradeCostPercentage() / 100.0));

        if (user.getCoins() < upgradeCost) {
            return Tasks.forException(new Exception("You don't have enough coins for the upgrade."));
        }

        double oldBonusValue = weapon.getCurrentBonusValue();
        double newBonusValue = oldBonusValue + 0.01;
        weapon.setCurrentBonusValue(newBonusValue);

        List<Task<Void>> tasks = new ArrayList<>();
        tasks.add(equipmentRepository.updateInventoryItem(user.getUid(), weapon));
        tasks.add(userService.updateUserCoins(user.getUid(), user.getCoins() - upgradeCost));

        if (template.getBonusType() == BonusType.PP_BOOST_PERMANENT) {
            double basePp = user.getPowerPoints() / (1 + oldBonusValue / 100.0);
            double basePpScaled = Math.round(basePp);
            int newTotalPp = (int) Math.round(basePpScaled * (1 + newBonusValue / 100.0));
            if (newTotalPp != user.getPowerPoints()) {
                tasks.add(userService.updatePowerPoints(user.getUid(), newTotalPp));
            }
        }
        return Tasks.whenAll(tasks);
    }
    public Task<Void> handleItemDrop(User user, EquipmentTemplate droppedItemTemplate) {
        String userId = user.getUid();
        return getUserInventory(userId).continueWithTask(inventoryTask -> {
            if (!inventoryTask.isSuccessful() || inventoryTask.getResult() == null) {
                throw Objects.requireNonNull(inventoryTask.getException());
            }

            UserInventoryItem existingItem = null;
            for (UserInventoryItem item : inventoryTask.getResult()) {
                if (item.getTemplateId().equals(droppedItemTemplate.getId())) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null && droppedItemTemplate.getType() == EquipmentType.WEAPON) {
                double oldBonusValue = existingItem.getCurrentBonusValue();
                double newBonusValue = oldBonusValue + 0.02;
                existingItem.setCurrentBonusValue(newBonusValue);

                List<Task<Void>> tasks = new ArrayList<>();
                tasks.add(equipmentRepository.updateInventoryItem(userId, existingItem));

                if (droppedItemTemplate.getBonusType() == BonusType.PP_BOOST_PERMANENT) {
                    double basePp = user.getPowerPoints() / (1 + oldBonusValue / 100.0);
                    double basePpScaled = Math.round(basePp);
                    int newTotalPp = (int) Math.round(basePpScaled * (1 + newBonusValue / 100.0));
                    if (newTotalPp != user.getPowerPoints()) {
                        tasks.add(userService.updatePowerPoints(userId, newTotalPp));
                    }
                }
                return Tasks.whenAll(tasks);
            } else {
                UserInventoryItem newItem = new UserInventoryItem();
                newItem.setTemplateId(droppedItemTemplate.getId());
                newItem.setCurrentBonusValue(droppedItemTemplate.getBonusValue());
                newItem.setActivated(false);
                newItem.setFightsRemaining(0);

                return equipmentRepository.buyInventoryItem(userId, newItem).continueWith(task -> null);
            }
        });
    }
    public Task<Void> loadAllTemplates() {
        return getEquipmentTemplates().onSuccessTask(templates -> {
            this.allTemplatesCache = templates;
            return Tasks.forResult(null);
        });
    }

    public EquipmentTemplate getRandomItem(boolean isAllianceReward,boolean isPotion){
        Random random = new Random();
        int randomNumber = random.nextInt(100);

        List<EquipmentTemplate> allItems = allTemplatesCache;
        EquipmentTemplate bow = new EquipmentTemplate();
        EquipmentTemplate sword = new EquipmentTemplate();
        EquipmentTemplate boots = new EquipmentTemplate();
        EquipmentTemplate gloves = new EquipmentTemplate();
        EquipmentTemplate shield = new EquipmentTemplate();
        EquipmentTemplate potion_40 = new EquipmentTemplate();
        EquipmentTemplate potion_20 = new EquipmentTemplate();
        EquipmentTemplate potion_10 = new EquipmentTemplate();
        EquipmentTemplate potion_5 = new EquipmentTemplate();

        for(EquipmentTemplate newItem : allItems){
            switch (newItem.getId()) {
                case "weapon_sword_1":
                    sword = newItem;
                    break;
                case "weapon_bow_1":
                    bow = newItem;
                    break;
                case "clothing_boots_1":
                    boots = newItem;
                    break;
                case "clothing_gloves_1":
                    gloves = newItem;
                    break;
                case "clothing_shield_1":
                    shield = newItem;
                    break;
                case "potion_pp_perm_5":
                    potion_5 = newItem;
                    break;
                case "potion_pp_perm_10":
                    potion_10 = newItem;
                    break;
                case "potion_pp_boost_20":
                    potion_20 = newItem;
                    break;
                case "potion_pp_boost_40":
                    potion_40 = newItem;
                    break;
            }
        }

        if(!isAllianceReward) {
            if (randomNumber < 3) {
                return sword;
            } else if (randomNumber < 6) {
                return bow;
            } else if (randomNumber < 37) {
                return boots;
            } else if (randomNumber < 68) {
                return gloves;
            } else {
                return shield;
            }
        } else if(!isPotion){
             if (randomNumber < 50) {
                return boots;
            } else {
                return shield;
            }
        } else {
            if (randomNumber < 25) {
                return potion_5;
            } else if (randomNumber < 50) {
                return potion_10;
            } else if (randomNumber < 75) {
                return potion_20;
            } else  {
                return potion_40;
            }
        }


    }
}

