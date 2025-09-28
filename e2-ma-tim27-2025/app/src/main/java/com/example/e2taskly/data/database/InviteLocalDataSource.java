package com.example.e2taskly.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e2taskly.model.AllianceInvite;
import com.example.e2taskly.model.enums.Status;

import java.util.ArrayList;
import java.util.List;

public class InviteLocalDataSource {
    private final SQLiteHelper dbHelper;
    public InviteLocalDataSource(Context context){
        this.dbHelper = new SQLiteHelper(context);
    }
    public void saveOrUpdateInvites(List<AllianceInvite> invites) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (AllianceInvite invite : invites) {
                ContentValues values = new ContentValues();
                values.put("id", invite.getInviteId());
                values.put("alliance_id", invite.getAllianceId());
                values.put("alliance_name", invite.getAllianceName());
                values.put("sender_id", invite.getSenderId());
                values.put("inviter_username", invite.getInviterUsername());
                values.put("receiver_id", invite.getReceiverId());
                if (invite.getTimestamp() != null) {
                    values.put("timestamp", invite.getTimestamp().getTime());
                }
                values.put("status", invite.getStatus().name());
                db.replace("alliance_invites", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public List<AllianceInvite> getPendingInvites(String recipientId) {
        List<AllianceInvite> invites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                SQLiteHelper.T_ALLIANCE_INVITES,
                null,
                "receiver_id = ? AND status = ?",
                new String[]{recipientId, "PENDING"},
                null,
                null,
                "timestamp DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                AllianceInvite invite = new AllianceInvite();
                invite.setInviteId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                invite.setAllianceId(cursor.getString(cursor.getColumnIndexOrThrow("alliance_id")));
                invite.setAllianceName(cursor.getString(cursor.getColumnIndexOrThrow("alliance_name")));
                invite.setSenderId(cursor.getString(cursor.getColumnIndexOrThrow("sender_id")));
                invite.setInviterUsername(cursor.getString(cursor.getColumnIndexOrThrow("inviter_username")));
                invite.setReceiverId(cursor.getString(cursor.getColumnIndexOrThrow("receiver_id")));

                int timestampIndex = cursor.getColumnIndex("timestamp");
                if (timestampIndex != -1 && !cursor.isNull(timestampIndex)) {
                    invite.setTimestamp(new java.util.Date(cursor.getLong(timestampIndex)));
                }

                String statusStr = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                invite.setStatus(Status.valueOf(statusStr));

                invites.add(invite);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return invites;
    }

    public void deleteInvite(String inviteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("alliance_invites", "id = ?", new String[]{inviteId});
        db.close();
    }
}
