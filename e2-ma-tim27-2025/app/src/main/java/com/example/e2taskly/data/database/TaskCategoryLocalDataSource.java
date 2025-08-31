package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.e2taskly.model.TaskCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskCategoryLocalDataSource {

    private SQLiteHelper dbHelper;

    public TaskCategoryLocalDataSource(Context context){this.dbHelper = new SQLiteHelper(context);}

    public long addCategory(TaskCategory category){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", category.getName());
        values.put("colorhex",category.getColorHex());

        long newRowId = -1;
        try {
            newRowId = db.insertOrThrow(SQLiteHelper.T_CATEGORIES, null, values);
            Log.d("DB_SUCCESS", "Row inserted with ID: " + newRowId);
        } catch (SQLiteConstraintException e) {
            Log.e("DB_ERROR", "Constraint failed: " + e.getMessage());
        } catch (SQLiteException e) {
            Log.e("DB_ERROR", "SQLite error: " + e.getMessage());
        } finally {
            db.close();
        }
        return newRowId;
    }

    public List<TaskCategory> getAllCategories(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<TaskCategory> categories = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLiteHelper.T_CATEGORIES,null);

        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String hexColor = cursor.getString(cursor.getColumnIndexOrThrow("colorhex"));

                TaskCategory category = new TaskCategory(id,hexColor,name);
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return categories;


    }
}
