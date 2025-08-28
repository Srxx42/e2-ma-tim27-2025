package com.example.e2taskly.data.repository.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.e2taskly.model.User;

public class UserLocalDataSource {
    private SQLiteHelper dbHelper;

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
        values.put("is_activated", user.isActivated() ? 1 : 0);
        values.put("registration_time", user.getRegistrationTime().getTime());

        long newRowId = db.insert(SQLiteHelper.T_USERS, null, values);
        db.close();
        return newRowId;
    }
}
