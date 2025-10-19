package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.e2taskly.model.SpecialMissionProgress;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MissionProgressLocalDataSource {

    private SQLiteHelper dbHelper;
    private static final String TAG = "MissionProgressDS";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd"); // yyyy-MM-dd

    public MissionProgressLocalDataSource(Context context) {
        this.dbHelper = new SQLiteHelper(context);
    }

    public long createMissionProgress(SpecialMissionProgress progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("userId", progress.getUserUid());
        values.put("allianceId", progress.getAllianceId());
        values.put("bossId", progress.getBossId());
        values.put("shoppingCount", progress.getShoppingCount());
        values.put("easyTaskCount", progress.getEasyTaskCount());
        values.put("hardTaskCount", progress.getHardTaskCount());
        values.put("successfulBossHitCount", progress.getSuccessfulBossHitCount());
        values.put("completedAll", progress.isCompletedAll() ? 1 : 0);
        values.put("didUserGetReward", progress.isDidUserGetReward() ? 1 : 0);
        values.put("messageCount", convertDateListToString(progress.getMessageCount()));

        long newRowId = -1;
        try {
            newRowId = db.insertOrThrow(SQLiteHelper.T_S_MISSON_PROGRESS, null, values);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error creating mission progress: " + e.getMessage());
        } finally {
            db.close();
        }
        return newRowId;
    }

    public boolean updateMissionProgress(SpecialMissionProgress progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("userId", progress.getUserUid());
        values.put("allianceId", progress.getAllianceId());
        values.put("bossId", progress.getBossId());
        values.put("shoppingCount", progress.getShoppingCount());
        values.put("easyTaskCount", progress.getEasyTaskCount());
        values.put("hardTaskCount", progress.getHardTaskCount());
        values.put("successfulBossHitCount", progress.getSuccessfulBossHitCount());
        values.put("completedAll", progress.isCompletedAll() ? 1 : 0);
        values.put("didUserGetReward", progress.isDidUserGetReward() ? 1 : 0);
        values.put("messageCount", convertDateListToString(progress.getMessageCount()));

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(SQLiteHelper.T_S_MISSON_PROGRESS, values, "smpId = ?", new String[]{String.valueOf(progress.getSmpId())});
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating mission progress: " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    public List<SpecialMissionProgress> getAllAlianceProgresses(String allianceId, String bossId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SpecialMissionProgress> progresses = new ArrayList<>();
        Cursor cursor = null;

        try {
            String selection = "allianceId = ? AND bossId = ?";
            String[] selectionArgs = {allianceId, bossId};
            cursor = db.query(SQLiteHelper.T_S_MISSON_PROGRESS, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    progresses.add(mapCursorToProgress(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting alliance progresses: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return progresses;
    }

    public SpecialMissionProgress getUserProgress(String userUid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpecialMissionProgress progress = null;
        Cursor cursor = null;

        try {
            String selection = "userId = ? AND didUserGetReward = ?";
            String[] selectionArgs = {userUid, String.valueOf(false)};
            cursor = db.query(SQLiteHelper.T_S_MISSON_PROGRESS, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                progress = mapCursorToProgress(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user progress: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return progress;
    }

    private SpecialMissionProgress mapCursorToProgress(Cursor cursor) throws ParseException {
        String smpId = cursor.getString(cursor.getColumnIndexOrThrow("smpId"));
        String userId = cursor.getString(cursor.getColumnIndexOrThrow("userId"));
        String allianceId = cursor.getString(cursor.getColumnIndexOrThrow("allianceId"));
        String bossId = cursor.getString(cursor.getColumnIndexOrThrow("bossId"));
        int shoppingCount = cursor.getInt(cursor.getColumnIndexOrThrow("shoppingCount"));
        int easyTaskCount = cursor.getInt(cursor.getColumnIndexOrThrow("easyTaskCount"));
        int hardTaskCount = cursor.getInt(cursor.getColumnIndexOrThrow("hardTaskCount"));
        int succesfullBossHitCount = cursor.getInt(cursor.getColumnIndexOrThrow("successfulBossHitCount"));
        boolean completedAll = cursor.getInt(cursor.getColumnIndexOrThrow("completedAll")) == 1;
        boolean didUserGetReward = cursor.getInt(cursor.getColumnIndexOrThrow("didUserGetReward")) == 1;
        List<Date> messageCount = convertStringToDateList(cursor.getString(cursor.getColumnIndexOrThrow("messageCount")));

        // Kreiranje objekta - pretpostavlja se da postoji odgovarajući konstruktor
        SpecialMissionProgress progress = new SpecialMissionProgress();
        progress.setSmpId(smpId);
        progress.setUserUid(userId);
        progress.setAllianceId(allianceId);
        progress.setBossId(bossId);
        progress.setShoppingCount(shoppingCount);
        progress.setEasyTaskCount(easyTaskCount);
        progress.setHardTaskCount(hardTaskCount);
        progress.setSuccessfulBossHitCount(succesfullBossHitCount);
        progress.setCompletedAll(completedAll);
        progress.setDidUserGetReward(didUserGetReward);
        progress.setMessageCount(messageCount);

        return progress;
    }

    private String convertDateListToString(List<Date> dateList) {
        if (dateList == null || dateList.isEmpty()) {
            return null;
        }
        return dateList.stream()
                .map(date -> DATE_FORMATTER.format(date)) // ✅
                .collect(Collectors.joining(","));
    }

    private List<Date> convertStringToDateList(String dateString) throws ParseException {
        List<Date> dateList = new ArrayList<>();
        if (dateString == null || dateString.isEmpty()) {
            return dateList;
        }
        String[] dateArray = dateString.split(",");
        for (String s : dateArray) {
            dateList.add(DATE_FORMATTER.parse(s)); // ✅
        }
        return dateList;
    }
}