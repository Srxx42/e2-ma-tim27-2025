package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.model.enums.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskLocalDataSource {

    private SQLiteHelper dbHelper;
    private Context context;

    public TaskLocalDataSource(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }
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
            commonValues.put("taskType", task.getType().name());
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
    public List<SingleTask> getAllSingleTasks(String creatorID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SingleTask> tasks = new ArrayList<>();
        TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);

        String query = "SELECT * FROM " + SQLiteHelper.T_TASKS + " t " +
                "INNER JOIN " + SQLiteHelper.T_SINGLE_TASKS + " rt ON t.id = rt.taskId " +
                "WHERE creatorId = ?";

        String[] selectionArgs = {creatorID};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        try {
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
        } finally {
            cursor.close();
            db.close();
         }
        return tasks;
    }

    public List<RepeatingTask> getAllRepeatingTasks(String creatorID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<RepeatingTask> tasks = new ArrayList<>();
        TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);

        String query = "SELECT * FROM " + SQLiteHelper.T_TASKS + " t " +
                "INNER JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId " +
                "WHERE creatorId = ?";

        String[] selectionArgs = { creatorID };
        Cursor cursor = db.rawQuery(query, selectionArgs);

        try {
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
                            LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("startingDate"))),
                            LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("finishingDate")))
                    );
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return tasks;
    }

    // Metoda getTaskById
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
            // AKO JE REPEATING TAKS U PITANJU
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
        }
        commonCursor.close();
        db.close();
        return task;
    }


    public boolean updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {

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
                // Ažuriranje specifičnih podataka u odgovarajućoj tabeli
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

                    if (repeatingTask.getOccurrences() != null) {
                        for (RepeatingTaskOccurrence occurrence : repeatingTask.getOccurrences()) {
                            ContentValues occurrenceValues = new ContentValues();
                            occurrenceValues.put("occurrenceStatus", occurrence.getOccurrenceStatus().name());
                            occurrenceValues.put("occurrenceDate", occurrence.getOccurrenceDate().toString());
                            db.update(SQLiteHelper.T_R_TASK_OCCURRENCE,
                                    occurrenceValues,
                                    "occurrenceId = ?",
                                    new String[]{String.valueOf(occurrence.getId())});
                        }
                    }
                }
            }

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


    public boolean saveTaskOccurrence(RepeatingTaskOccurrence occurence){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newRowId = -1;

        db.beginTransaction();
        try{
            ContentValues commonValues = new ContentValues();
            commonValues.put("repeatingTaskId", occurence.getRepeatingTaskId());
            commonValues.put("occurrenceDate",occurence.getOccurrenceDate().toString());
            commonValues.put("occurrenceStatus",occurence.getOccurrenceStatus().name());

            newRowId = db.insertOrThrow(SQLiteHelper.T_R_TASK_OCCURRENCE,null,commonValues);

            db.setTransactionSuccessful();
            Log.d("DB_SUCCESS", "Task inserted successfully with ID: " + newRowId);
        } catch (Exception e) {
            Log.e("DB_ERROR", "Failed to insert occurrence: " + e.getMessage());
            newRowId = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return newRowId != -1;
    }

    public List<RepeatingTaskOccurrence> getAllTaskOccurrences(int taskId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<RepeatingTaskOccurrence> occurrences = new ArrayList<>();

        Cursor commonCursor = db.query(SQLiteHelper.T_R_TASK_OCCURRENCE, null, "repeatingTaskId = ?", new String[]{String.valueOf(taskId)}, null, null, null);

        if (commonCursor.moveToFirst()) {
            do {
                RepeatingTaskOccurrence occurence = new RepeatingTaskOccurrence(
                        commonCursor.getInt(commonCursor.getColumnIndexOrThrow("occurrenceId")),
                        commonCursor.getInt(commonCursor.getColumnIndexOrThrow("repeatingTaskId")),
                        LocalDate.parse(commonCursor.getString(commonCursor.getColumnIndexOrThrow("occurrenceDate"))),
                        TaskStatus.valueOf(commonCursor.getString(commonCursor.getColumnIndexOrThrow("occurrenceStatus")))
                );

                occurrences.add(occurence);

            } while (commonCursor.moveToNext());
        }
        commonCursor.close();
        db.close();
        return occurrences;
    }

    public boolean deleteFutureOccurrences(int repeatingTaskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            String today = LocalDate.now().toString();

            int deletedRows = db.delete(SQLiteHelper.T_R_TASK_OCCURRENCE,
                    "repeatingTaskId = ? AND occurrenceDate > ?",
                    new String[]{String.valueOf(repeatingTaskId), today});

            Log.d("DB_DELETE", "Obrisano " + deletedRows + " budućih instanci.");

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            Log.e("DB_ERROR", "Greška pri otkazivanju budućih taskova", e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }

    public boolean deleteAllOccurrences(int repeatingTaskId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            int deletedRows = db.delete(SQLiteHelper.T_R_TASK_OCCURRENCE,
                    "repeatingTaskId = ?",
                    new String[]{String.valueOf(repeatingTaskId)});

            Log.d("DB_DELETE", "Obrisano " + deletedRows + " budućih instanci.");

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            Log.e("DB_ERROR", "Greška pri otkazivanju budućih taskova", e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }




}