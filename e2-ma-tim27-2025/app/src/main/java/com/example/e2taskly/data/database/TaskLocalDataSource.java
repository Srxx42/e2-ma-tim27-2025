package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.model.enums.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskLocalDataSource {

    private SQLiteHelper dbHelper;
    private Context context;

    public TaskLocalDataSource(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }

    // Tvoja addTask metoda - ostaje nepromenjena, ispravna je
    public long addTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newRowId = -1;

        db.beginTransaction();
        try {
            ContentValues commonValues = new ContentValues();
            commonValues.put("creatorId", task.getCreatorId());
            commonValues.put("name", task.getName());
            commonValues.put("description", task.getDescription());
            commonValues.put("categoryId", task.getCategory().getId());
            commonValues.put("taskType", task.getType().name()); // Preimenovano u getTaskType() radi konzistentnosti sa modelom
            commonValues.put("status", task.getStatus().name());
            commonValues.put("importance", task.getImportance().name());
            commonValues.put("difficulty", task.getDifficulty().name());
            commonValues.put("valueXP", task.getValueXP());
            commonValues.put("deleted", task.isDeleted() ? 1 : 0);

            newRowId = db.insertOrThrow(SQLiteHelper.T_TASKS, null, commonValues);

            if (task instanceof SingleTask) {
                SingleTask singleTask = (SingleTask) task;
                ContentValues singleValues = new ContentValues();
                singleValues.put("taskId", newRowId);
                singleValues.put("taskDate", singleTask.getTaskDate().toString());
                db.insertOrThrow(SQLiteHelper.T_SINGLE_TASKS, null, singleValues);

            } else if (task instanceof RepeatingTask) {
                RepeatingTask repeatingTask = (RepeatingTask) task;
                ContentValues repeatingValues = new ContentValues();
                repeatingValues.put("taskId", newRowId);
                repeatingValues.put("repeatingType", repeatingTask.getRepeatingType().name());
                repeatingValues.put("interval", repeatingTask.getInterval());
                repeatingValues.put("startingDate", repeatingTask.getStartingDate().toString());
                repeatingValues.put("finishingDate", repeatingTask.getFinishingDate().toString());
                db.insertOrThrow(SQLiteHelper.T_REPEATING_TASKS, null, repeatingValues);
            }

            db.setTransactionSuccessful();
            Log.d("DB_SUCCESS", "Task inserted successfully with ID: " + newRowId);

        } catch (Exception e) {
            Log.e("DB_ERROR", "Failed to insert task: " + e.getMessage());
            newRowId = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return newRowId;
    }

    // Tvoja getAllSingleTasks metoda - ostaje nepromenjena, ispravna je
    public List<SingleTask> getAllSingleTasks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SingleTask> tasks = new ArrayList<>();
        TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);

        String query = "SELECT * FROM " + SQLiteHelper.T_TASKS + " t " +
                "INNER JOIN " + SQLiteHelper.T_SINGLE_TASKS + " st ON t.id = st.taskId";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                TaskCategory category = categoryDataSource.getCategoryById(cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")));
                SingleTask task = new SingleTask(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("creatorId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        category,
                        TaskType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("taskType"))),
                        TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
                        Importance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("importance"))),
                        Difficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("difficulty"))),
                        cursor.getInt(cursor.getColumnIndexOrThrow("valueXP")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("deleted")) == 1,
                        LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("taskDate")))
                );
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    // Tvoja getAllRepeatingTasks metoda - ISPRAVLJENA da koristi 'startingDate' i 'finishingDate'
    public List<RepeatingTask> getAllRepeatingTasks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<RepeatingTask> tasks = new ArrayList<>();
        TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);

        String query = "SELECT * FROM " + SQLiteHelper.T_TASKS + " t " +
                "INNER JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                TaskCategory category = categoryDataSource.getCategoryById(cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")));
                RepeatingTask task = new RepeatingTask(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("creatorId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        category,
                        TaskType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("taskType"))),
                        TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
                        Importance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("importance"))),
                        Difficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("difficulty"))),
                        cursor.getInt(cursor.getColumnIndexOrThrow("valueXP")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("deleted")) == 1,
                        RepeatingType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("repeatingType"))),
                        cursor.getInt(cursor.getColumnIndexOrThrow("interval")),
                        // --- ISPRAVKA OVDE ---
                        LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("startingDate"))),
                        LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("finishingDate")))
                );
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    // Metoda getTaskById - DOPUNJENA
    public Task getTaskById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Task task = null;

        Cursor commonCursor = db.query(SQLiteHelper.T_TASKS, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (commonCursor.moveToFirst()) {
            TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);
            TaskType taskType = TaskType.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("taskType")));
            TaskCategory category = categoryDataSource.getCategoryById(commonCursor.getInt(commonCursor.getColumnIndexOrThrow("categoryId")));

            if (taskType == TaskType.SINGLE) {
                Cursor specificCursor = db.query(SQLiteHelper.T_SINGLE_TASKS, null, "taskId = ?", new String[]{String.valueOf(id)}, null, null, null);
                if (specificCursor.moveToFirst()) {
                    task = new SingleTask(
                            id,
                            commonCursor.getString(commonCursor.getColumnIndexOrThrow("creatorId")),
                            commonCursor.getString(commonCursor.getColumnIndexOrThrow("name")),
                            commonCursor.getString(commonCursor.getColumnIndexOrThrow("description")),
                            category,
                            taskType,
                            TaskStatus.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("status"))),
                            Importance.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("importance"))),
                            Difficulty.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("difficulty"))),
                            commonCursor.getInt(commonCursor.getColumnIndexOrThrow("valueXP")),
                            commonCursor.getInt(commonCursor.getColumnIndexOrThrow("deleted")) == 1,
                            LocalDate.parse(specificCursor.getString(specificCursor.getColumnIndexOrThrow("taskDate")))
                    );
                }
                specificCursor.close();
            }
            // --- POČETAK DOPUNE ---
            else if (taskType == TaskType.REPEATING) {
                Cursor specificCursor = db.query(SQLiteHelper.T_REPEATING_TASKS, null, "taskId = ?", new String[]{String.valueOf(id)}, null, null, null);
                if (specificCursor.moveToFirst()) {
                    task = new RepeatingTask(
                            id,
                            commonCursor.getString(commonCursor.getColumnIndexOrThrow("creatorId")),
                            commonCursor.getString(commonCursor.getColumnIndexOrThrow("name")),
                            commonCursor.getString(commonCursor.getColumnIndexOrThrow("description")),
                            category,
                            taskType,
                            TaskStatus.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("status"))),
                            Importance.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("importance"))),
                            Difficulty.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("difficulty"))),
                            commonCursor.getInt(commonCursor.getColumnIndexOrThrow("valueXP")),
                            commonCursor.getInt(commonCursor.getColumnIndexOrThrow("deleted")) == 1,
                            RepeatingType.valueOf(specificCursor.getString(specificCursor.getColumnIndexOrThrow("repeatingType"))),
                            specificCursor.getInt(specificCursor.getColumnIndexOrThrow("interval")),
                            LocalDate.parse(specificCursor.getString(specificCursor.getColumnIndexOrThrow("startingDate"))),
                            LocalDate.parse(specificCursor.getString(specificCursor.getColumnIndexOrThrow("finishingDate")))
                    );
                }
                specificCursor.close();
            }
            // --- KRAJ DOPUNE ---
        }
        commonCursor.close();
        db.close();
        return task;
    }

    // Metoda updateTask - DOPUNJENA
    public boolean updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            // --- POČETAK DOPUNE ---
            // KORAK 1: Ažuriranje zajedničkih podataka u glavnoj tabeli
            ContentValues commonValues = new ContentValues();
            commonValues.put("creatorId", task.getCreatorId());
            commonValues.put("name", task.getName());
            commonValues.put("description", task.getDescription());
            commonValues.put("categoryId", task.getCategory().getId());
            commonValues.put("taskType", task.getType().name());
            commonValues.put("status", task.getStatus().name());
            commonValues.put("importance", task.getImportance().name());
            commonValues.put("difficulty", task.getDifficulty().name());
            commonValues.put("valueXP", task.getValueXP());
            commonValues.put("deleted", task.isDeleted() ? 1 : 0);

            rowsAffected = db.update(SQLiteHelper.T_TASKS, commonValues, "id = ?", new String[]{String.valueOf(task.getId())});

            if (rowsAffected > 0) {
                // KORAK 2: Ažuriranje specifičnih podataka u odgovarajućoj tabeli
                if (task instanceof SingleTask) {
                    SingleTask singleTask = (SingleTask) task;
                    ContentValues singleValues = new ContentValues();
                    singleValues.put("taskDate", singleTask.getTaskDate().toString());
                    db.update(SQLiteHelper.T_SINGLE_TASKS, singleValues, "taskId = ?", new String[]{String.valueOf(task.getId())});

                } else if (task instanceof RepeatingTask) {
                    RepeatingTask repeatingTask = (RepeatingTask) task;
                    ContentValues repeatingValues = new ContentValues();
                    repeatingValues.put("repeatingType", repeatingTask.getRepeatingType().name());
                    repeatingValues.put("interval", repeatingTask.getInterval());
                    repeatingValues.put("startingDate", repeatingTask.getStartingDate().toString());
                    repeatingValues.put("finishingDate", repeatingTask.getFinishingDate().toString());
                    db.update(SQLiteHelper.T_REPEATING_TASKS, repeatingValues, "taskId = ?", new String[]{String.valueOf(task.getId())});
                }
            }
            // --- KRAJ DOPUNE ---
            db.setTransactionSuccessful();
            Log.d("DB_SUCCESS", "Task updated successfully for ID: " + task.getId());
        } catch (Exception e) {
            Log.e("DB_ERROR", "Failed to update task: " + e.getMessage());
            rowsAffected = 0; // Resetuj na 0 ako je došlo do greške
        } finally {
            db.endTransaction();
            db.close();
        }
        return rowsAffected > 0;
    }

    // Tvoja deleteTaskById metoda - ostaje nepromenjena, ispravna je
    public boolean deleteTaskById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(SQLiteHelper.T_TASKS, "id = ?", new String[]{String.valueOf(id)});
            Log.d("DB_SUCCESS", "Deleted task rows: " + rowsAffected);
        } catch (SQLiteException e) {
            Log.e("DB_ERROR", "SQLite task delete error: " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }
}