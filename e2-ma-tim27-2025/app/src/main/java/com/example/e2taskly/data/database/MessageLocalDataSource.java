package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e2taskly.model.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageLocalDataSource {
    private final SQLiteHelper dbHelper;
    public MessageLocalDataSource(Context context){
        this.dbHelper = new SQLiteHelper(context);
    }
    public void saveMessage(Message message){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", message.getMessageId());
        values.put("alliance_id", message.getAllianceId());
        values.put("sender_id", message.getSenderId());
        values.put("sender_username", message.getSenderUsername());
        values.put("text", message.getText());
        if (message.getTimestamp() != null) {
            values.put("timestamp", message.getTimestamp().getTime());
        }

        db.insertWithOnConflict(SQLiteHelper.T_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    public List<Message> getMessagesForAlliance(String allianceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Message> messages = new ArrayList<>();
        Cursor cursor = db.query(SQLiteHelper.T_MESSAGES,
                null, "alliance_id = ?", new String[]{allianceId}, null, null, "timestamp ASC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message message = cursorToMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }
    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        message.setAllianceId(cursor.getString(cursor.getColumnIndexOrThrow("alliance_id")));
        message.setSenderId(cursor.getString(cursor.getColumnIndexOrThrow("sender_id")));
        message.setSenderUsername(cursor.getString(cursor.getColumnIndexOrThrow("sender_username")));
        message.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
        message.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))));
        return message;
    }
}
