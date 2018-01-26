package com.example.pramod.taskplace;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by pramod on 25/1/18.
 */

public class OnRecentClearService extends Service {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ClearFromRecentService", "Service Started");
        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        editor.putString("APPSTATUS", "enter");
        editor.commit();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("ClearFromRecentService", "END");
        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        editor.putString("APPSTATUS", "exit");
        editor.commit();
        stopSelf();
    }
}
