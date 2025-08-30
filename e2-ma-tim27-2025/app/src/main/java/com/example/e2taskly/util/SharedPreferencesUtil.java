package com.example.e2taskly.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String PREFERENCES_NAME = "E2TasklyPreferences";
    private static final String KEY_USER_UID = "user_uid";
    private final SharedPreferences sharedPreferences;

    public SharedPreferencesUtil(Context context){
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,Context.MODE_PRIVATE);
    }
    public void saveUserSession(String uid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_UID, uid);
        editor.apply();
    }

    public String getActiveUserUid() {
        return sharedPreferences.getString(KEY_USER_UID, null);
    }

    public void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_UID);
        editor.apply();
    }
}
