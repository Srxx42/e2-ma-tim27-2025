package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        values.put("friends_ids",TextUtils.join(",", user.getFriendIds()));
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
        values.put("alliance_id", user.getAllianceId());
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
}
