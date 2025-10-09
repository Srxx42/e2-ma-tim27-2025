package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.UserInventoryItem;
import com.example.e2taskly.model.enums.BonusType;
import com.example.e2taskly.model.enums.EquipmentType;

import java.util.ArrayList;
import java.util.List;

public class EquipmentLocalDataSource {
    private final SQLiteHelper dbHelper;
    public EquipmentLocalDataSource(Context context) {
        this.dbHelper = new SQLiteHelper(context);
    }
    public void saveTemplates(List<EquipmentTemplate> templates){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            db.delete(SQLiteHelper.T_EQUIPMENT_TEMPLATES,null,null);

            for(EquipmentTemplate template : templates){
                ContentValues values = new ContentValues();
                values.put("id", template.getId());
                values.put("name", template.getName());
                values.put("description", template.getDescription());
                values.put("type", template.getType().name());
                values.put("bonus_type", template.getBonusType().name());
                values.put("bonus_value", template.getBonusValue());
                values.put("duration_in_fights", template.getDurationInFights());
                values.put("cost_percentage", template.getCostPercentage());
                values.put("upgrade_cost_percentage", template.getUpgradeCostPercentage());
                db.insert(SQLiteHelper.T_EQUIPMENT_TEMPLATES, null, values);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
            db.close();
        }
    }
    public List<EquipmentTemplate> getTemplates() {
        List<EquipmentTemplate> templates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.T_EQUIPMENT_TEMPLATES, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                EquipmentTemplate template = new EquipmentTemplate();
                template.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                template.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                template.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                template.setType(EquipmentType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type"))));
                template.setBonusType(BonusType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("bonus_type"))));
                template.setBonusValue(cursor.getDouble(cursor.getColumnIndexOrThrow("bonus_value")));
                template.setDurationInFights(cursor.getInt(cursor.getColumnIndexOrThrow("duration_in_fights")));
                template.setCostPercentage(cursor.getInt(cursor.getColumnIndexOrThrow("cost_percentage")));
                template.setUpgradeCostPercentage(cursor.getInt(cursor.getColumnIndexOrThrow("upgrade_cost_percentage")));
                templates.add(template);
            }
            cursor.close();
        }
        db.close();
        return templates;
    }
    public void saveInventoryForUser(String userId, List<UserInventoryItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SQLiteHelper.T_USER_INVENTORY, "user_id = ?", new String[]{userId});
            for (UserInventoryItem item : items) {
                ContentValues values = new ContentValues();
                values.put("inventory_id", item.getInventoryId());
                values.put("user_id", userId);
                values.put("template_id", item.getTemplateId());
                values.put("is_activated", item.isActivated() ? 1 : 0);
                values.put("fights_remaining", item.getFightsRemaining());
                values.put("current_bonus_value", item.getCurrentBonusValue());
                db.insert(SQLiteHelper.T_USER_INVENTORY, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public List<UserInventoryItem> getInventoryForUser(String userId) {
        List<UserInventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.T_USER_INVENTORY, null, "user_id = ?", new String[]{userId}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                UserInventoryItem item = new UserInventoryItem();
                item.setInventoryId(cursor.getString(cursor.getColumnIndexOrThrow("inventory_id")));
                item.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
                item.setTemplateId(cursor.getString(cursor.getColumnIndexOrThrow("template_id")));
                item.setActivated(cursor.getInt(cursor.getColumnIndexOrThrow("is_activated")) == 1);
                item.setFightsRemaining(cursor.getInt(cursor.getColumnIndexOrThrow("fights_remaining")));
                item.setCurrentBonusValue(cursor.getDouble(cursor.getColumnIndexOrThrow("current_bonus_value")));
                items.add(item);
            }
            cursor.close();
        }
        db.close();
        return items;
    }
    public void clearUserInventory(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLiteHelper.T_USER_INVENTORY, "user_id = ?", new String[]{userId});
        db.close();
    }
}
