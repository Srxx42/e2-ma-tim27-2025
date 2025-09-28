package com.example.e2taskly.data.database;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.enums.MissionStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllianceLocalDataSource {
    private final SQLiteHelper dbHelper;
    public AllianceLocalDataSource(Context context){this.dbHelper = new SQLiteHelper(context);}
    public void saveOrUpdateAlliance(Alliance alliance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", alliance.getAllianceId());
        values.put("name", alliance.getName());
        values.put("leader_id", alliance.getLeaderId());
        values.put("member_ids", TextUtils.join(",", alliance.getMemberIds()));

        if (alliance.getMissionStatus() != null) {
            values.put("mission_status", alliance.getMissionStatus().name());
        } else {
            values.put("mission_status", MissionStatus.NOT_STARTED.name());
        }
        values.put("current_mission_id", alliance.getCurrentMissionId());
        db.replace(SQLiteHelper.T_ALLIANCES, null, values);
        db.close();
    }
    public Alliance getAllianceById(String allianceId) {
        if (allianceId == null || allianceId.isEmpty()) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.T_ALLIANCES,
                null, "id = ?", new String[]{allianceId}, null, null, null);

        Alliance alliance = null;
        if (cursor != null && cursor.moveToFirst()) {
            alliance = new Alliance();
            alliance.setAllianceId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            alliance.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            alliance.setLeaderId(cursor.getString(cursor.getColumnIndexOrThrow("leader_id")));

            String memberIdsStr = cursor.getString(cursor.getColumnIndexOrThrow("member_ids"));
            if (memberIdsStr != null && !memberIdsStr.isEmpty()) {
                alliance.setMemberIds(new ArrayList<>(Arrays.asList(memberIdsStr.split(","))));
            } else {
                alliance.setMemberIds(new ArrayList<>());
            }
            String missionStatusStr = cursor.getString(cursor.getColumnIndexOrThrow("mission_status"));
            if (missionStatusStr != null) {
                try {
                    alliance.setMissionStatus(MissionStatus.valueOf(missionStatusStr));
                } catch (IllegalArgumentException e) {
                    alliance.setMissionStatus(MissionStatus.NOT_STARTED);
                }
            } else {
                alliance.setMissionStatus(MissionStatus.NOT_STARTED);
            }

            alliance.setCurrentMissionId(cursor.getString(cursor.getColumnIndexOrThrow("current_mission_id")));

            cursor.close();
        }
        db.close();
        return alliance;
    }
    public void deleteAlliance(String allianceId) {
        if (allianceId == null || allianceId.isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLiteHelper.T_ALLIANCES, "id = ?", new String[]{allianceId});
        db.close();
    }
    public void addMemberToAlliance(String allianceId, String memberIdToAdd) {
        if (allianceId == null || memberIdToAdd == null) return;

        Alliance currentAlliance = getAllianceById(allianceId);
        if (currentAlliance == null) {
            Log.e("DB_ERROR","Alliance with id " + allianceId + " not found locally. Cannot add member.");
            return;
        }

        List<String> memberIds = currentAlliance.getMemberIds();
        if (!memberIds.contains(memberIdToAdd)) {
            memberIds.add(memberIdToAdd);

            String newMemberIdsStr = TextUtils.join(",", memberIds);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("member_ids", newMemberIdsStr);

            db.update(SQLiteHelper.T_ALLIANCES, values, "id = ?", new String[]{allianceId});
            db.close();
        }
    }
    public void removeMemberFromAlliance(String allianceId, String memberIdToRemove) {
        if (allianceId == null || memberIdToRemove == null) return;

        Alliance currentAlliance = getAllianceById(allianceId);
        if (currentAlliance == null) {
            Log.e("DB_ERROR", "Alliance with id " + allianceId + " not found locally. Cannot remove member.");
            return;
        }

        List<String> memberIds = currentAlliance.getMemberIds();
        if (memberIds.contains(memberIdToRemove)) {
            memberIds.remove(memberIdToRemove);

            String newMemberIdsStr = TextUtils.join(",", memberIds);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("member_ids", newMemberIdsStr);

            db.update(SQLiteHelper.T_ALLIANCES, values, "id = ?", new String[]{allianceId});
            db.close();
        }
    }

}
