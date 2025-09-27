package com.example.e2taskly.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "e2taskly.db";
    private static final int DATABASE_VERSION = 7;
    public static final String T_USERS = "users";

    public static final String T_CATEGORIES = "taskCategories";
    public static final String T_ALLIANCES = "alliances";
    public static final String T_ALLIANCE_INVITES = "alliance_invites";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + T_USERS + " (" +
                "id TEXT PRIMARY KEY, " +
                "email TEXT NOT NULL UNIQUE, " +
                "username TEXT NOT NULL UNIQUE, " +
                "avatar TEXT NOT NULL, " +
                "level INTEGER NOT NULL DEFAULT 1, " +
                "xp INTEGER NOT NULL DEFAULT 0, " +
                "is_activated INTEGER NOT NULL, " +
                "registration_time INTEGER NOT NULL," +
                "title TEXT DEFAULT 'Rookie', " +
                "power_points INTEGER DEFAULT 0, " +
                "coins INTEGER DEFAULT 0, " +
                "badges TEXT, " +
                "equipment TEXT, " +
                "active_days_streak INTEGER, " +
                "last_activity_date INTEGER, " +
                "friends_ids TEXT," +
                "alliance_id TEXT," +
                "fcm_token TEXT" +
                ")");

        db.execSQL("create  table " + T_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE," +
                "colorhex TEXT NOT NULL UNIQUE" +
                ")");
        db.execSQL("CREATE TABLE " + T_ALLIANCES + " (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "leader_id TEXT NOT NULL, " +
                "member_ids TEXT, " +
                "mission_started TEXT NOT NULL," +
                "current_mission_id TEXT," +
                "FOREIGN KEY(leader_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE" +
                ")");
        db.execSQL("CREATE TABLE " + T_ALLIANCE_INVITES + " (" +
                "id TEXT PRIMARY KEY, " +
                "alliance_id TEXT NOT NULL, " +
                "alliance_name TEXT NOT NULL, " +
                "sender_id TEXT NOT NULL, " +
                "inviter_username TEXT NOT NULL, " +
                "receiver_id TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "status TEXT NOT NULL" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
//        db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
//        onCreate(db);
        if (oldVersion < 2) {
            // Add the new column if it doesn't exist
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN active_days_streak INTEGER DEFAULT 0;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN friends_ids TEXT");
        }
        if(oldVersion < 4){
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN alliance_id  TEXT");
        }
        if(oldVersion < 5){
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ALLIANCES + " (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "leader_id TEXT NOT NULL, " +
                    "member_ids TEXT, " +
                    "mission_status TEXT NOT NULL," +
                    "current_mission_id TEXT," +
                    "FOREIGN KEY(leader_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE" +
                    ")");
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ALLIANCE_INVITES + " (" +
                    "id TEXT PRIMARY KEY, " +
                    "alliance_id TEXT NOT NULL, " +
                    "alliance_name TEXT NOT NULL, " +
                    "sender_id TEXT NOT NULL, " +
                    "inviter_username TEXT NOT NULL, " +
                    "receiver_id TEXT NOT NULL, " +
                    "timestamp INTEGER NOT NULL, " +
                    "status TEXT NOT NULL" +
                    ")");
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN fcm_token TEXT");
        }
    }
}
