package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.e2taskly.model.Boss;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BossLocalDataSource {

    private SQLiteHelper dbHelper;
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public BossLocalDataSource(Context context){this.dbHelper = new SQLiteHelper(context);}

    public long createBoss(Boss boss){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("enemyId",boss.getEnemyId());
        values.put("bossLevel",boss.getBossLevel());
        values.put("bossHp",boss.getBossHp());
        values.put("bossGold",boss.getBossGold());
        values.put("isBossBeaten",boss.isBossBeaten() ? 1 : 0);
        values.put("didUserFightIt", boss.isDidUserFightIt() ? 1 : 0);
        values.put("isAllianceBoss",boss.isAllianceBoss() ? 1 : 0);
        if (boss.getBossAppearanceDate() != null) {
            values.put("bossAppearanceDate", boss.getBossAppearanceDate().format(formatter));
        }

        long newRowId = -1;
        try {
            newRowId = db.insertOrThrow(SQLiteHelper.T_BOSS, null, values);
            Log.d("DB_SUCCESS", "Row inserted with ID: " + newRowId);
        } catch (SQLiteException e) {
            Log.e("DB_ERROR", "SQLite error: " + e.getMessage());
        } finally {
            db.close();
        }
        return newRowId;
    }

    public Boss getById(int bossId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Boss boss = null;

        Cursor cursor = db.query(SQLiteHelper.T_BOSS, null, "bossId = ?", new String[]{String.valueOf(bossId)}, null, null, null);

        if(cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("bossId"));
            String enemyId = cursor.getString(cursor.getColumnIndexOrThrow("enemyId"));
            int bossLevel = cursor.getInt(cursor.getColumnIndexOrThrow("bossLevel"));
            float bossHp = cursor.getFloat(cursor.getColumnIndexOrThrow("bossHp"));
            float bossGold = cursor.getFloat(cursor.getColumnIndexOrThrow("bossGold"));
            boolean isBossBeaten = cursor.getInt(cursor.getColumnIndexOrThrow("isBossBeaten")) == 1;
            boolean didUserFightIt = cursor.getInt(cursor.getColumnIndexOrThrow("didUserFightIt")) == 1;
            boolean isAllianceBoss = cursor.getInt(cursor.getColumnIndexOrThrow("isAllianceBoss")) == 1;
            String bossAppearanceDateStr = cursor.getString(cursor.getColumnIndexOrThrow("bossAppearanceDate"));
            LocalDate bossAppearanceDate = null;
            if (bossAppearanceDateStr != null) {
                bossAppearanceDate = LocalDate.parse(bossAppearanceDateStr, formatter);
            }

            boss = new Boss(id,enemyId,bossLevel,bossHp,bossGold,isBossBeaten,didUserFightIt,isAllianceBoss,bossAppearanceDate);
        }

        cursor.close();
        db.close();

        return boss;
    }

    public Boss getByEnemyId(String enemyId, boolean isAlliance){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Boss boss = null;
        String[] selectionArgs = {enemyId, isAlliance ? "1" : "0"};

        Cursor cursor = db.query(SQLiteHelper.T_BOSS, null, "enemyId = ? AND isAllianceBoss = ?", selectionArgs, null, null, null);

        if(cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("bossId"));
            String eId = cursor.getString(cursor.getColumnIndexOrThrow("enemyId"));
            int bossLevel = cursor.getInt(cursor.getColumnIndexOrThrow("bossLevel"));
            float bossHp = cursor.getFloat(cursor.getColumnIndexOrThrow("bossHp"));
            float bossGold = cursor.getFloat(cursor.getColumnIndexOrThrow("bossGold"));
            boolean isBossBeaten = cursor.getInt(cursor.getColumnIndexOrThrow("isBossBeaten")) == 1;
            boolean didUserFightIt = cursor.getInt(cursor.getColumnIndexOrThrow("didUserFightIt")) == 1;
            boolean isAllianceBoss = cursor.getInt(cursor.getColumnIndexOrThrow("isAllianceBoss")) == 1;
            String bossAppearanceDateStr = cursor.getString(cursor.getColumnIndexOrThrow("bossAppearanceDate"));
            LocalDate bossAppearanceDate = null;
            if (bossAppearanceDateStr != null) {
                bossAppearanceDate = LocalDate.parse(bossAppearanceDateStr, formatter);
            }

            boss = new Boss(id,eId,bossLevel,bossHp,bossGold,isBossBeaten,didUserFightIt,isAllianceBoss,bossAppearanceDate);
        }

        cursor.close();
        db.close();

        return boss;
    }

    public boolean updateBoss(Boss boss){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("enemyId", boss.getEnemyId());
        values.put("bossLevel", boss.getBossLevel());
        values.put("bossHp", boss.getBossHp());
        values.put("bossGold", boss.getBossGold());
        values.put("isBossBeaten", boss.isBossBeaten() ? 1 : 0);
        values.put("didUserFightIt", boss.isDidUserFightIt() ? 1 : 0);
        values.put("isAllianceBoss", boss.isAllianceBoss() ? 1 : 0);
        if (boss.getBossAppearanceDate() != null) {
            values.put("bossAppearanceDate", boss.getBossAppearanceDate().format(formatter));
        }

        int rowsAffected = 0;
        try{
            rowsAffected = db.update(SQLiteHelper.T_BOSS, values, "bossId = ?", new String[]{String.valueOf(boss.getBossId())});
            Log.d("DB_SUCCESS", "Updated rows: " + rowsAffected);
        } catch(SQLiteException e){
            Log.e("DB_ERROR", "SQLite update error: " + e.getMessage());
        }finally {
            db.close();
        }

        return rowsAffected > 0;
    }
}