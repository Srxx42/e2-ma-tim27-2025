package com.example.e2taskly.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.e2taskly.model.enums.TaskStatus;

import java.time.LocalDate;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "e2taskly.db";
    private static final int DATABASE_VERSION = 5;
    public static final String T_USERS = "users";

    public static final String T_CATEGORIES = "taskCategories";

    public static final String T_TASKS = "tasks";
    public static final String T_SINGLE_TASKS = "single_tasks";
    public static final String T_REPEATING_TASKS = "repeating_tasks";
    public static final String T_R_TASK_OCCURRENCE = "r_task_occurrences";
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
                "last_activity_date INTEGER " +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
//        db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
//        onCreate(db);
        if (oldVersion < 3) {
            // Add the new column if it doesn't exist
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN active_days_streak INTEGER DEFAULT 0;");
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN last_activity_date INTEGER DEFAULT 0;");

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


            db.execSQL(createTaskTable);
            db.execSQL(createSingleTaskTable);
            db.execSQL(createRepeatingTaskTable);
        }
        if (oldVersion < 4) {

            db.execSQL("create  table " + T_CATEGORIES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "creatorId TEXT NOT NULL ," +
                    "name TEXT NOT NULL ," +
                    "colorhex TEXT NOT NULL " +
                    ")");

            String createOccurrenceTable = "CREATE TABLE " + T_R_TASK_OCCURRENCE + " (" +
                    "occurrenceId INTEGER PRIMARY KEY, " +
                    "repeatingTaskId INTEGER NOT NULL, " +
                    "occurrenceDate TEXT NOT NULL, "+
                    "occurrenceStatus TEXT NOT NULL, " +
                    "FOREIGN KEY(repeatingTaskId) REFERENCES " + T_REPEATING_TASKS + "(taskId) on DELETE CASCADE" +
                    ");";

            db.execSQL(createOccurrenceTable);
        }
        if (oldVersion < 5){

            db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);

            db.execSQL("create  table " + T_CATEGORIES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "creatorId TEXT NOT NULL ," +
                    "name TEXT NOT NULL ," +
                    "colorhex TEXT NOT NULL " +
                    ")");

        }
    }
}
