package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Pair;

import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.model.enums.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public boolean updateOccurrence(RepeatingTaskOccurrence occurrence){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected  = -1;

        db.beginTransaction();
        try{
            ContentValues occurrenceValues = new ContentValues();
            occurrenceValues.put("repeatingTaskId", occurrence.getRepeatingTaskId());
            occurrenceValues.put("occurrenceStatus", occurrence.getOccurrenceStatus().name());
            occurrenceValues.put("occurrenceDate", occurrence.getOccurrenceDate().toString());
            rowsAffected = db.update(SQLiteHelper.T_R_TASK_OCCURRENCE,
                    occurrenceValues,
                    "occurrenceId = ?",
                    new String[]{String.valueOf(occurrence.getId())});

            if (rowsAffected > 0) {
                db.setTransactionSuccessful(); // Potvrdi promene
                Log.d("DB_SUCCESS", "Occurrence updated successfully for ID: " + occurrence.getId());
            }

        } catch (Exception e) {
            Log.e("DB_ERROR", "Failed to insert occurrence: " + e.getMessage());
            rowsAffected = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return rowsAffected != -1;
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

    public int getUserTaskCountByStatus(Date startDate, Date endDate, TaskStatus status, String userUid) {
        int totalCount = 0;

        if(startDate == null){
            LocalDate specificLocalDate = LocalDate.of(2024, 8, 1);
            startDate = Date.from(specificLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        String statusString = status.toString();


        String countTasksQuery = "SELECT " +
                "(SELECT COUNT(st.taskId) FROM " + SQLiteHelper.T_SINGLE_TASKS + " AS st " +
                "JOIN " + SQLiteHelper.T_TASKS + " AS t ON st.taskId = t.id " +
                "WHERE t.creatorId = ? AND t.status = ? AND st.taskDate BETWEEN ? AND ?) AS singleTaskCount, " +
                "(SELECT COUNT(o.occurrenceId) FROM " + SQLiteHelper.T_R_TASK_OCCURRENCE + " AS o " +
                "JOIN " + SQLiteHelper.T_REPEATING_TASKS + " AS rt ON o.repeatingTaskId = rt.taskId " +
                "JOIN " + SQLiteHelper.T_TASKS + " AS t ON rt.taskId = t.id " +
                "WHERE t.creatorId = ? AND o.occurrenceStatus = ? AND o.occurrenceDate BETWEEN ? AND ?) AS occurrenceCount;";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {

            String[] selectionArgs = {
                    userUid,
                    statusString,
                    startDateString,
                    endDateString,
                    userUid,
                    statusString,
                    startDateString,
                    endDateString
            };

            cursor = db.rawQuery(countTasksQuery, selectionArgs);


            if (cursor != null && cursor.moveToFirst()) {
                int singleTaskCount = cursor.getInt(0);
                int occurrenceCount = cursor.getInt(1);

                totalCount = singleTaskCount + occurrenceCount;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return totalCount;
    }

    public boolean isThereTaskWithCategory(int categoryId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean taskExists = false;

        try {
            String query = "SELECT 1 FROM " + SQLiteHelper.T_TASKS +
                    " WHERE categoryId = ? LIMIT 1";

            String[] selectionArgs = { String.valueOf(categoryId) };

            cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.getCount() > 0) {
                taskExists = true;
            }
        } catch (SQLiteException e) {
            Log.e("DB_ERROR", "Greška prilikom provere postojanja taska za kategoriju: " + categoryId, e);
        } finally {

            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return taskExists;
    }


//  ============================= LAKI STATISIKA ===============================================

    public Map<String, Integer> getTaskStatusCounts(String userId) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("created", 0);
        counts.put("completed", 0);
        counts.put("not_completed", 0);
        counts.put("canceled", 0);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor createdCursor = db.rawQuery("SELECT COUNT(*) FROM " + SQLiteHelper.T_TASKS + " WHERE creatorId = ?", new String[]{userId});
        if (createdCursor.moveToFirst()) {
            counts.put("created", createdCursor.getInt(0));
        }
        createdCursor.close();

        String singleTaskQuery = "SELECT status, COUNT(*) FROM " + SQLiteHelper.T_TASKS +
                " WHERE creatorId = ? AND taskType = 'SINGLE' GROUP BY status";
        Cursor singleCursor = db.rawQuery(singleTaskQuery, new String[]{userId});
        while (singleCursor.moveToNext()) {
            TaskStatus status = TaskStatus.valueOf(singleCursor.getString(0));
            int count = singleCursor.getInt(1);
            updateCountsMap(counts, status, count);
        }
        singleCursor.close();

        String repeatingTaskQuery = "SELECT ro.occurrenceStatus, COUNT(*) FROM " + SQLiteHelper.T_R_TASK_OCCURRENCE + " ro" +
                " JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON ro.repeatingTaskId = rt.taskId" +
                " JOIN " + SQLiteHelper.T_TASKS + " t ON rt.taskId = t.id" +
                " WHERE t.creatorId = ? GROUP BY ro.occurrenceStatus";
        Cursor repeatingCursor = db.rawQuery(repeatingTaskQuery, new String[]{userId});
        while (repeatingCursor.moveToNext()) {
            TaskStatus status = TaskStatus.valueOf(repeatingCursor.getString(0));
            int count = repeatingCursor.getInt(1);
            updateCountsMap(counts, status, count);
        }
        repeatingCursor.close();

        db.close();
        return counts;
    }
    private void updateCountsMap(Map<String, Integer> counts, TaskStatus status, int count) {
        switch (status) {
            case COMPLETED:
                counts.put("completed", counts.get("completed") + count);
                break;
            case FAILED:
                counts.put("not_completed", counts.get("not_completed") + count);
                break;
            case CANCELED:
                counts.put("canceled", counts.get("canceled") + count);
                break;
        }
    }
    public List<Pair<String, String>> getAllTaskEventsForStreak(String userId) {
        List<Pair<String, String>> events = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT date, status FROM (" +
                "SELECT st.taskDate as date, t.status as status FROM " + SQLiteHelper.T_TASKS + " t JOIN " + SQLiteHelper.T_SINGLE_TASKS + " st ON t.id = st.taskId WHERE t.creatorId = ? AND (t.status = ? OR t.status = ?) " +
                "UNION ALL " +
                "SELECT ro.occurrenceDate as date, ro.occurrenceStatus as status FROM " + SQLiteHelper.T_TASKS + " t JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId JOIN " + SQLiteHelper.T_R_TASK_OCCURRENCE + " ro ON rt.taskId = ro.repeatingTaskId WHERE t.creatorId = ? AND (ro.occurrenceStatus = ? OR ro.occurrenceStatus = ?)" +
                ") AS all_events ORDER BY date ASC";

        String completedStatus = TaskStatus.COMPLETED.name();
        String failedStatus = TaskStatus.FAILED.name();

        Cursor cursor = db.rawQuery(query, new String[]{userId, completedStatus, failedStatus, userId, completedStatus, failedStatus});
        while (cursor.moveToNext()) {
            events.add(new Pair<>(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        db.close();
        return events;
    }

    public Map<String, Integer> getCompletedTasksCountByCategory(String userId) {
        Map<String, Integer> categoryCounts = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String completedStatus = TaskStatus.COMPLETED.toString();
        String query = "SELECT c.name, COUNT(*) FROM " + SQLiteHelper.T_CATEGORIES + " c JOIN (" +
                "SELECT categoryId FROM " + SQLiteHelper.T_TASKS + " t WHERE t.creatorId = ? AND t.taskType = 'SINGLE' AND t.status = ? " +
                "UNION ALL " +
                "SELECT t.categoryId FROM " + SQLiteHelper.T_TASKS + " t " +
                "JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId " +
                "JOIN " + SQLiteHelper.T_R_TASK_OCCURRENCE + " ro ON rt.taskId = ro.repeatingTaskId " +
                "WHERE t.creatorId = ? AND ro.occurrenceStatus = ?" +
                ") AS completed_tasks ON c.id = completed_tasks.categoryId GROUP BY c.name";

        Cursor cursor = db.rawQuery(query, new String[]{userId, completedStatus, userId, completedStatus});
        while (cursor.moveToNext()) {
            categoryCounts.put(cursor.getString(0), cursor.getInt(1));
        }
        cursor.close();
        db.close();
        return categoryCounts;
    }
    public Map<String, Double> getAverageDifficultyXpOverTime(String userId) {
        Map<String, Double> avgXpPerDay = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String completedStatus = TaskStatus.COMPLETED.name();

        String difficultyCaseStatement = "CASE t.difficulty " +
                "WHEN '" + Difficulty.EASY.name()   + "' THEN 1 " +
                "WHEN '" + Difficulty.NORMAL.name() + "' THEN 3 " +
                "WHEN '" + Difficulty.HARD.name()   + "' THEN 7 " +
                "WHEN '" + Difficulty.EPIC.name()   + "' THEN 20 " +
                "ELSE 0 END";

        String query = "SELECT date, AVG(difficulty_xp) FROM (" +
                "SELECT st.taskDate as date, " + difficultyCaseStatement + " as difficulty_xp FROM " + SQLiteHelper.T_TASKS + " t JOIN " + SQLiteHelper.T_SINGLE_TASKS + " st ON t.id = st.taskId WHERE t.creatorId = ? AND t.status = ? " +
                "UNION ALL " +
                "SELECT ro.occurrenceDate as date, " + difficultyCaseStatement + " as difficulty_xp FROM " + SQLiteHelper.T_TASKS + " t JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId JOIN " + SQLiteHelper.T_R_TASK_OCCURRENCE + " ro ON rt.taskId = ro.repeatingTaskId WHERE t.creatorId = ? AND ro.occurrenceStatus = ?" +
                ") AS xp_data GROUP BY date ORDER BY date ASC";

        Cursor cursor = db.rawQuery(query, new String[]{userId, completedStatus, userId, completedStatus});
        while (cursor.moveToNext()) {
            avgXpPerDay.put(cursor.getString(0), cursor.getDouble(1));
        }
        cursor.close();
        db.close();

        Log.d("Statistics_AvgDifficulty", "Prosečna težina po danu (prilagođeno): " + avgXpPerDay.toString());
        return avgXpPerDay;
    }
    public Map<String, Integer> getXpEarnedLast7Days(String userId) {
        Map<String, Integer> xpPerDay = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sevenDaysAgo = LocalDate.now().minusDays(7).toString();
        String completedStatus = TaskStatus.COMPLETED.name();

        String query = "SELECT date, SUM(valueXP) FROM (" +
                "SELECT st.taskDate as date, t.valueXP FROM " + SQLiteHelper.T_TASKS + " t JOIN " + SQLiteHelper.T_SINGLE_TASKS + " st ON t.id = st.taskId WHERE t.creatorId = ? AND t.status = ? AND st.taskDate >= ? " +
                "UNION ALL " +
                "SELECT ro.occurrenceDate as date, t.valueXP FROM " + SQLiteHelper.T_TASKS + " t JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId JOIN " + SQLiteHelper.T_R_TASK_OCCURRENCE + " ro ON rt.taskId = ro.repeatingTaskId WHERE t.creatorId = ? AND ro.occurrenceStatus = ? AND ro.occurrenceDate >= ?" +
                ") AS xp_data GROUP BY date ORDER BY date ASC";

        Cursor cursor = db.rawQuery(query, new String[]{userId, completedStatus, sevenDaysAgo, userId, completedStatus, sevenDaysAgo});
        while (cursor.moveToNext()) {
            xpPerDay.put(cursor.getString(0), cursor.getInt(1));
        }
        cursor.close();
        db.close();
        Log.e("Test",xpPerDay.toString());
        return xpPerDay;
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

    public boolean pauseAllOccurrences(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("occurrenceStatus", TaskStatus.PAUSED.toString());

            String whereClause = "repeatingTaskId = ? AND occurrenceStatus = ?";

            String[] whereArgs = {
                    String.valueOf(taskId),
                    TaskStatus.ACTIVE.toString()
            };

            rowsAffected = db.update(
                    SQLiteHelper.T_R_TASK_OCCURRENCE,
                    values,
                    whereClause,
                    whereArgs
            );

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            Log.e("DB_ERROR", "Greška pri pauziranju taskova za task ID: " + taskId, e);
        } finally {
            db.endTransaction();
            db.close();
        }

        Log.d("DB_UPDATE", "Pauzirano " + rowsAffected + " aktivnih taskova.");
        return success;
    }

    public boolean unpauseAllOccurrences(int taskId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("occurrenceStatus", TaskStatus.ACTIVE.toString());

            String whereClause = "repeatingTaskId = ? AND occurrenceStatus = ?";

            String[] whereArgs = {
                    String.valueOf(taskId),
                    TaskStatus.PAUSED.toString()
            };

            rowsAffected = db.update(
                    SQLiteHelper.T_R_TASK_OCCURRENCE,
                    values,
                    whereClause,
                    whereArgs
            );

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            Log.e("DB_ERROR", "Greška pri pauziranju taskova za task ID: " + taskId, e);
        } finally {
            db.endTransaction();
            db.close();
        }

        Log.d("DB_UPDATE", "Pauzirano " + rowsAffected + " aktivnih taskova.");
        return success;
    }





    //KOD ZA WORKER-a
    // Metoda koja vraća SVE single taskove
    public List<SingleTask> getAllSingleTasksForAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SingleTask> tasks = new ArrayList<>();
        TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);

        // Upit je isti, samo bez WHERE dela
        String query = "SELECT * FROM " + SQLiteHelper.T_TASKS + " t " +
                "INNER JOIN " + SQLiteHelper.T_SINGLE_TASKS + " st ON t.id = st.taskId";

        Cursor cursor = db.rawQuery(query, null); // Nema selectionArgs

        try {
            if (cursor.moveToFirst()) {
                do {
                    // ... (ceo kod za parsiranje kursora je isti kao u tvojoj getAllSingleTasks metodi)
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

    // Metoda koja vraća SVE repeating taskove
    public List<RepeatingTask> getAllRepeatingTasksForAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<RepeatingTask> tasks = new ArrayList<>();
        TaskCategoryLocalDataSource categoryDataSource = new TaskCategoryLocalDataSource(context);

        // Upit je isti, samo bez WHERE dela
        String query = "SELECT * FROM " + SQLiteHelper.T_TASKS + " t " +
                "INNER JOIN " + SQLiteHelper.T_REPEATING_TASKS + " rt ON t.id = rt.taskId";

        Cursor cursor = db.rawQuery(query, null); // Nema selectionArgs

        try {
            if (cursor.moveToFirst()) {
                do {
                    // ... (ceo kod za parsiranje kursora je isti kao u tvojoj getAllRepeatingTasks metodi)
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

}