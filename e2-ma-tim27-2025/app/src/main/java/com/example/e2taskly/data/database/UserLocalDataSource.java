package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.e2taskly.model.User;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
        values.put("active_days_streak",user.getActiveDaysStreak());
        values.put("last_activity_date",user.getLastActivityDate().getTime());
        values.put("friends_ids",TextUtils.join(",", user.getFriendIds()));
        values.put("level_up_date", user.getLevelUpDate().toString().trim());
        values.put("attack_chance", user.getAttackChance());
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
        values.put("active_days_streak",user.getActiveDaysStreak());
        values.put("last_activity_date",user.getLastActivityDate().getTime());
        values.put("alliance_id", user.getAllianceId());
        if (user.getLevelUpDate() != null) {
            values.put("level_up_date", user.getLevelUpDate().toString().trim());
        } else {
            values.putNull("level_up_date");
        }
        values.put("attack_chance", user.getAttackChance());
        db.update(SQLiteHelper.T_USERS,values,"id"+" = ?",new String[]{user.getUid()});
    }
    public int updateUserAllianceId(String uid, String allianceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("alliance_id", allianceId);

        int rowsAffected = db.update(SQLiteHelper.T_USERS, values, "id = ?", new String[]{uid});
        db.close();

        return rowsAffected;
    }
    public void addFriend(String currentUid, String friendUid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String currentFriends = null;
        Cursor cursor = db.query(SQLiteHelper.T_USERS,
                new String[]{"friends_ids"},
                "id=?",
                new String[]{currentUid},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                currentFriends = cursor.getString(0);
            }
            cursor.close();
        }

        if (currentFriends == null || currentFriends.isEmpty()) {
            currentFriends = friendUid;
        } else {
            String[] existing = currentFriends.split(",");
            for (String f : existing) {
                if (f.equals(friendUid)) {
                    db.close();
                    return;
                }
            }
            currentFriends = currentFriends + "," + friendUid;
        }
        ContentValues values = new ContentValues();
        values.put("friends_ids", currentFriends);
        db.update(SQLiteHelper.T_USERS, values, "id=?", new String[]{currentUid});
        db.close();
    }
    public void removeFriend(String currentUid, String friendUid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String currentFriends = null;
        Cursor cursor = db.query(SQLiteHelper.T_USERS,
                new String[]{"friends_ids"},
                "id=?",
                new String[]{currentUid},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                currentFriends = cursor.getString(0);
            }
            cursor.close();
        }

        if (currentFriends == null || currentFriends.isEmpty()) {
            db.close();
            return;
        }

        StringBuilder updatedFriends = new StringBuilder();
        String[] existing = currentFriends.split(",");
        for (String f : existing) {
            if (!f.equals(friendUid) && !f.isEmpty()) {
                if (updatedFriends.length() > 0) {
                    updatedFriends.append(",");
                }
                updatedFriends.append(f);
            }
        }

        ContentValues values = new ContentValues();
        values.put("friends_ids", updatedFriends.toString());
        db.update(SQLiteHelper.T_USERS, values, "id=?", new String[]{currentUid});
        db.close();
    }
    public void updateUserFcmToken(String uid, String token) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fcm_token", token);
        db.update(SQLiteHelper.T_USERS, values, "id = ?", new String[]{uid});
        db.close();
    }
    public User getUser(String uid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.T_USERS,
                null, // Sve kolone
                "id = ?",
                new String[]{uid},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setUid(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow("avatar")));
            user.setLevel(cursor.getInt(cursor.getColumnIndexOrThrow("level")));
            user.setXp(cursor.getInt(cursor.getColumnIndexOrThrow("xp")));
            user.setActivated(cursor.getInt(cursor.getColumnIndexOrThrow("is_activated")) == 1);
            user.setRegistrationTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("registration_time"))));
            user.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            user.setPowerPoints(cursor.getInt(cursor.getColumnIndexOrThrow("power_points")));
            user.setCoins(cursor.getInt(cursor.getColumnIndexOrThrow("coins")));

            String badgesStr = cursor.getString(cursor.getColumnIndexOrThrow("badges"));
            if (badgesStr != null && !badgesStr.isEmpty()) {
                user.setBadges(new ArrayList<>(Arrays.asList(badgesStr.split(","))));
            }

            String equipmentStr = cursor.getString(cursor.getColumnIndexOrThrow("equipment"));

            user.setActiveDaysStreak(cursor.getInt(cursor.getColumnIndexOrThrow("active_days_streak")));
            user.setLastActivityDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("last_activity_date"))));

            String friendIdsStr = cursor.getString(cursor.getColumnIndexOrThrow("friends_ids"));
            if (friendIdsStr != null && !friendIdsStr.isEmpty()) {
                user.setFriendIds(new ArrayList<>(Arrays.asList(friendIdsStr.split(","))));
            }

            user.setAllianceId(cursor.getString(cursor.getColumnIndexOrThrow("alliance_id")));
            user.setFcmToken(cursor.getString(cursor.getColumnIndexOrThrow("fcm_token")));

            user.setLevelUpDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("level_up_date"))));
            user.setAttackChance(cursor.getInt(cursor.getColumnIndexOrThrow("attack_chance")));

            cursor.close();
        }
        db.close();
        return user;
    }
    public void updateUserCoins(String uid, int newCoinAmount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("coins", newCoinAmount);
        db.update(SQLiteHelper.T_USERS, values, "id = ?", new String[]{uid});
        db.close();
    }
}
