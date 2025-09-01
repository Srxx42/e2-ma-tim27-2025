package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.e2taskly.model.User;


import java.util.Date;

public class UserLocalDataSource {
    private final SQLiteHelper dbHelper;

    public UserLocalDataSource(Context context) {
        this.dbHelper = new SQLiteHelper(context);
    }
    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", user.getUid());
        values.put("email", user.getEmail());
        values.put("username", user.getUsername());
        values.put("avatar", user.getAvatar());
        values.put("level", user.getLevel());
        values.put("xp", user.getXp());
        values.put("is_activated", user.isActivated() ? 1 : 0);
        values.put("registration_time", user.getRegistrationTime().getTime());
        values.put("title", user.getTitle());
        values.put("power_points", user.getPowerPoints());
        values.put("coins", user.getCoins());
        if (user.getBadges() != null) {
            values.put("badges", TextUtils.join(",", user.getBadges()));
        }
        if (user.getEquipment() != null) {
            values.put("equipment", TextUtils.join(",", user.getEquipment()));
        }
        values.put("active_days_streak",user.getActiveDaysStreak());
        values.put("last_activity_date",user.getLastActivityDate().getTime());
        long newRowId = db.insert(SQLiteHelper.T_USERS, null, values);
        db.close();
        return newRowId;
    }
    public void deleteUser(String uid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLiteHelper.T_USERS,  "id"+ " = ?", new String[]{uid});
        db.close();
    }
    public int updateUserActivationStatus(String uid, boolean isActivated) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_activated", isActivated ? 1 : 0);
        int rowsAffected = db.update(SQLiteHelper.T_USERS, values, "id" + " = ?", new String[]{uid});
        db.close();

        return rowsAffected;
    }
    public void updateStreakData(String uid, Date date, int newStreak){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("active_days_streak",newStreak);
        values.put("last_activity_date",date.getTime());
        db.update(SQLiteHelper.T_USERS,values,"id"+"=?",new String[]{uid});
    }
    public void updateUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", user.getUid());
        values.put("email", user.getEmail());
        values.put("username", user.getUsername());
        values.put("avatar", user.getAvatar());
        values.put("level", user.getLevel());
        values.put("xp", user.getXp());
        values.put("is_activated", user.isActivated() ? 1 : 0);
        values.put("registration_time", user.getRegistrationTime().getTime());
        values.put("title", user.getTitle());
        values.put("power_points", user.getPowerPoints());
        values.put("coins", user.getCoins());
        if (user.getBadges() != null) {
            values.put("badges", TextUtils.join(",", user.getBadges()));
        }
        if (user.getEquipment() != null) {
            values.put("equipment", TextUtils.join(",", user.getEquipment()));
        }
        values.put("active_days_streak",user.getActiveDaysStreak());
        values.put("last_activity_date",user.getLastActivityDate().getTime());
        db.update(SQLiteHelper.T_USERS,values,"id"+" = ?",new String[]{user.getUid()});
    }
}
