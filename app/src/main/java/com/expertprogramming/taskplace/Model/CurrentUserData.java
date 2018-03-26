package com.expertprogramming.taskplace.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by pramod on 17/1/18.
 */

public class CurrentUserData {
    Context context;
    public CurrentUserData(Context context){
        this.context=context;
    }
    public void setCurrentUID(String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UID", value);
        editor.commit();
    }

    public String getCurrentUID() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("UID", null);
    }
    public void setCurrentUserEmail(String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("EMAIL", value);
        editor.commit();
    }

    public String getCurrentUserEmail() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("EMAIL", null);
    }

}
