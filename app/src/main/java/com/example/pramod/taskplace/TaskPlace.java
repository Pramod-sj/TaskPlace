package com.example.pramod.taskplace;

import android.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * Created by pramod on 28/1/18.
 */

public class TaskPlace extends Application {
    @SuppressLint("RestrictedApi")
    public void onCreate(){
        super.onCreate();
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(false) //default: true
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .errorDrawable(R.drawable.customactivityoncrash_error_image) //default: bug image
                //.restartActivity(MainActivity.class) //default: null (your app's launch activity)
                //.errorActivity(CustomErrorActivity.class) //default: null (default error activity)
                .eventListener(null) //default: null
                .apply();
        CustomActivityOnCrash.install(this);

    }
}
