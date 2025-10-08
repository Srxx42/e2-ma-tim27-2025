package com.example.e2taskly.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.e2taskly.model.enums.TaskStatus;

import java.time.LocalDate;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "e2taskly.db";
    private static final int DATABASE_VERSION = 13;

    public static final String T_USERS = "users";

    public static final String T_CATEGORIES = "taskCategories";
    public static final String T_ALLIANCES = "alliances";
    public static final String T_ALLIANCE_INVITES = "alliance_invites";
    public static final String T_MESSAGES = "messages";

    public static final String T_TASKS = "tasks";
    public static final String T_SINGLE_TASKS = "single_tasks";
    public static final String T_REPEATING_TASKS = "repeating_tasks";
    public static final String T_R_TASK_OCCURRENCE = "r_task_occurrences";
    public static final String T_BOSS = "boss";
    public static final String T_EQUIPMENT_TEMPLATES = "equipment_templates";
    public static final String T_USER_INVENTORY = "user_inventory";
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
                "fcm_token TEXT, " +
                "level_up_date INTEGER, " +
                "attack_chance INTEGER " +
                ")");

        db.execSQL("CREATE TABLE " + T_ALLIANCES + " (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "leader_id TEXT NOT NULL, " +
                "member_ids TEXT, " +
                "mission_status TEXT NOT NULL," +
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
                "status TEXT NOT NULL," +
                "FOREIGN KEY(alliance_id) REFERENCES " + T_ALLIANCES + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(sender_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(receiver_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE" +
                ")");
        db.execSQL("CREATE TABLE " + T_MESSAGES + " (" +
                "id TEXT PRIMARY KEY, " +
                "alliance_id TEXT NOT NULL, " +
                "sender_id TEXT NOT NULL, " +
                "sender_username TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "FOREIGN KEY(alliance_id) REFERENCES " + T_ALLIANCES + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(sender_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE" +
                ")");


        db.execSQL("create  table " + T_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "creatorId TEXT NOT NULL ," +
                "name TEXT NOT NULL ," +
                "colorhex TEXT NOT NULL " +
                ")");


        // GLAVNA TABELA SA ZAJEDNIČKIM POLJIMA
        String createTaskTable = "CREATE TABLE " + T_TASKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "creatorId TEXT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "categoryId INTEGER, " +
                "taskType TEXT NOT NULL, " +
                "status TEXT, " +
                "importance TEXT, " +
                "difficulty TEXT, " +
                "valueXP INTEGER, " +
                "deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY(categoryId) REFERENCES " + T_CATEGORIES + "(id)" +
                ");";

        // TABELA SAMO ZA SINGLE TASK POLJA
        String createSingleTaskTable = "CREATE TABLE " + T_SINGLE_TASKS + " (" +
                "taskId INTEGER PRIMARY KEY, " +
                "taskDate TEXT NOT NULL, " +
                "FOREIGN KEY(taskId) REFERENCES " + T_TASKS + "(id) ON DELETE CASCADE" +
                ");";

        // TABELA SAMO ZA REPEATING TASK POLJA
        String createRepeatingTaskTable = "CREATE TABLE " + T_REPEATING_TASKS + " (" +
                "taskId INTEGER PRIMARY KEY, " +
                "repeatingType TEXT, " +
                "interval INTEGER, " +
                "startingDate TEXT NOT NULL, " +
                "finishingDate TEXT NOT NULL, " +
                "FOREIGN KEY(taskId) REFERENCES " + T_TASKS + "(id) ON DELETE CASCADE" +
                ");";

        //TABELA ZA OCCURRENCES REPEATING TASKA
        String createOccurrenceTable = "CREATE TABLE " + T_R_TASK_OCCURRENCE + " (" +
                "occurrenceId INTEGER PRIMARY KEY, " +
                "repeatingTaskId INTEGER NOT NULL, " +
                "occurrenceDate TEXT NOT NULL, "+
                "occurrenceStatus TEXT NOT NULL, " +
                "FOREIGN KEY(repeatingTaskId) REFERENCES " + T_REPEATING_TASKS + "(taskId) on DELETE CASCADE" +
                ");";

        //TABELA ZA BOSS-a
        String createBossTabel = "CREATE TABLE " + T_BOSS + " (" +
                "bossId INTEGER PRIMARY KEY, " +
                "enemyId TEXT NOT NULL, " +
                "bossLevel INTEGER NOT NULL, " +
                "bossHp FLOAT NOT NULL, " +
                "bossGold FLOAT NOT NULL, " +
                "isBossBeaten INTEGER DEFAULT 0 NOT NULL, " +
                "didUserFightIt INTEGER DEFAULT 0 NOT NULL, " +
                "isAllianceBoss INTEGER DEFAULT 0 NOT NULL," +
                "bossAppearanceDate TEXT " +
                ");";

        db.execSQL(createBossTabel);
        db.execSQL(createTaskTable);
        db.execSQL(createSingleTaskTable);
        db.execSQL(createRepeatingTaskTable);
        db.execSQL(createOccurrenceTable);
        db.execSQL("CREATE TABLE " + T_EQUIPMENT_TEMPLATES + " (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT," +
                "description TEXT," +
                "type TEXT," +
                "bonus_type TEXT," +
                "bonus_value REAL," +
                "duration_in_fights INTEGER," +
                "cost_percentage INTEGER," +
                "upgrade_cost_percentage INTEGER)");
        db.execSQL("CREATE TABLE " + T_USER_INVENTORY + " (" +
                "inventory_id TEXT PRIMARY KEY," +
                "user_id TEXT," +
                "template_id TEXT," +
                "is_activated INTEGER," +
                "fights_remaining INTEGER," +
                "current_bonus_value REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN active_days_streak INTEGER DEFAULT 0;");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN last_activity_date INTEGER DEFAULT 0;");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN friends_ids TEXT");

        }
        if (oldVersion < 4) {

            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN alliance_id  TEXT");

        }
        if (oldVersion < 5){

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
        if (oldVersion < 8) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_MESSAGES + " (" +
                    "id TEXT PRIMARY KEY, " +
                    "alliance_id TEXT NOT NULL, " +
                    "sender_id TEXT NOT NULL, " +
                    "sender_username TEXT NOT NULL, " +
                    "text TEXT NOT NULL, " +
                    "timestamp INTEGER NOT NULL, " +
                    "FOREIGN KEY(alliance_id) REFERENCES " + T_ALLIANCES + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(sender_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE" +
                    ")");
        }
        if (oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS " + T_ALLIANCE_INVITES);
            db.execSQL("CREATE TABLE " + T_ALLIANCE_INVITES + " (" +
                    "id TEXT PRIMARY KEY, " +
                    "alliance_id TEXT NOT NULL, " +
                    "alliance_name TEXT NOT NULL, " +
                    "sender_id TEXT NOT NULL, " +
                    "inviter_username TEXT NOT NULL, " +
                    "receiver_id TEXT NOT NULL, " +
                    "timestamp INTEGER NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY(alliance_id) REFERENCES " + T_ALLIANCES + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(sender_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(receiver_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE" +
                    ")");
        }

        if(oldVersion < 10){
            db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + T_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + T_SINGLE_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + T_REPEATING_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + T_R_TASK_OCCURRENCE);
            db.execSQL("DROP TABLE IF EXISTS " + T_BOSS);

            db.execSQL("create  table " + T_CATEGORIES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "creatorId TEXT NOT NULL ," +
                    "name TEXT NOT NULL ," +
                    "colorhex TEXT NOT NULL " +
                    ")");

            // GLAVNA TABELA SA ZAJEDNIČKIM POLJIMA
            String createTaskTable = "CREATE TABLE " + T_TASKS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "creatorId TEXT, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "categoryId INTEGER, " +
                    "taskType TEXT NOT NULL, " +
                    "status TEXT, " +
                    "importance TEXT, " +
                    "difficulty TEXT, " +
                    "valueXP INTEGER, " +
                    "deleted INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(categoryId) REFERENCES " + T_CATEGORIES + "(id)" +
                    ");";

            // TABELA SAMO ZA SINGLE TASK POLJA
            String createSingleTaskTable = "CREATE TABLE " + T_SINGLE_TASKS + " (" +
                    "taskId INTEGER PRIMARY KEY, " +
                    "taskDate TEXT NOT NULL, " +
                    "FOREIGN KEY(taskId) REFERENCES " + T_TASKS + "(id) ON DELETE CASCADE" +
                    ");";

            // TABELA SAMO ZA REPEATING TASK POLJA
            String createRepeatingTaskTable = "CREATE TABLE " + T_REPEATING_TASKS + " (" +
                    "taskId INTEGER PRIMARY KEY, " +
                    "repeatingType TEXT, " +
                    "interval INTEGER, " +
                    "startingDate TEXT NOT NULL, " +
                    "finishingDate TEXT NOT NULL, " +
                    "FOREIGN KEY(taskId) REFERENCES " + T_TASKS + "(id) ON DELETE CASCADE" +
                    ");";

            //TABELA ZA OCCURRENCES REPEATING TASKA
            String createOccurrenceTable = "CREATE TABLE " + T_R_TASK_OCCURRENCE + " (" +
                    "occurrenceId INTEGER PRIMARY KEY, " +
                    "repeatingTaskId INTEGER NOT NULL, " +
                    "occurrenceDate TEXT NOT NULL, "+
                    "occurrenceStatus TEXT NOT NULL, " +
                    "FOREIGN KEY(repeatingTaskId) REFERENCES " + T_REPEATING_TASKS + "(taskId) on DELETE CASCADE" +
                    ");";

            //TABELA ZA BOSS-a
            String createBossTabel = "CREATE TABLE " + T_BOSS + " (" +
                    "bossId INTEGER PRIMARY KEY, " +
                    "enemyId TEXT NOT NULL, " +
                    "bossLevel INTEGER NOT NULL, " +
                    "bossHp FLOAT NOT NULL, " +
                    "bossGold FLOAT NOT NULL, " +
                    "isBossBeaten INTEGER DEFAULT 0 NOT NULL, " +
                    "didUserFightIt INTEGER DEFAULT 0 NOT NULL, " +
                    "isAllianceBoss INTEGER DEFAULT 0 NOT NULL," +
                    "bossAppearanceDate TEXT " +
                    ");";

            db.execSQL(createBossTabel);
            db.execSQL(createTaskTable);
            db.execSQL(createSingleTaskTable);
            db.execSQL(createRepeatingTaskTable);
            db.execSQL(createOccurrenceTable);

        } if (oldVersion < 11){

            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN title TEXT DEFAULT 'Rookie'");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN power_points INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN coins INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN badges TEXT");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN equipment TEXT");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN level_up_date INTEGER");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN attack_chance INTEGER");
        }

        if(oldVersion<12){
            db.execSQL("DROP TABLE IF EXISTS " + T_EQUIPMENT_TEMPLATES);
            db.execSQL("DROP TABLE IF EXISTS " + T_USER_INVENTORY);
            db.execSQL("CREATE TABLE " + T_EQUIPMENT_TEMPLATES + " (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT," +
                    "description TEXT," +
                    "type TEXT," +
                    "bonus_type TEXT," +
                    "bonus_value REAL," +
                    "duration_in_fights INTEGER," +
                    "cost_percentage INTEGER," +
                    "upgrade_cost_percentage INTEGER)");
            db.execSQL("CREATE TABLE " + T_USER_INVENTORY + " (" +
                    "inventory_id TEXT PRIMARY KEY," +
                    "user_id TEXT," +
                    "template_id TEXT," +
                    "is_activated INTEGER," +
                    "fights_remaining INTEGER," +
                    "current_bonus_value REAL)");
        }
    }
}
