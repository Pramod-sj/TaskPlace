/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.pramod.taskplace.LocationService;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    GoogleApiClient mGoogleApiClient;
    Context context;
    private static final String TAG = "LUBroadcastReceiver";
    public static final String ACTION_PROCESS_UPDATES = "com.example.pramod.taskplace.action" + ".PROCESS_UPDATES";
    LocationRequest locationRequest;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if(LocationRequestHelper.getRequestingTrigger(context)==false) {
                if (TaskPlace.getGoogleApiHelper().isConnected()) {
                    mGoogleApiClient = TaskPlace.getGoogleApiHelper().getGoogleApiClient();
                    Toast.makeText(context, String.valueOf(TaskPlace.getGoogleApiHelper().getGoogleApiClient().isConnected()), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(context, "no api client", Toast.LENGTH_SHORT).show();
                LocationServiceMethods methods = new LocationServiceMethods(context, mGoogleApiClient);
                methods.createLocationRequest();
                methods.requestLocationUpdates();
            }
        }
        if (intent.getAction().equals(ACTION_PROCESS_UPDATES)) {
            Log.i("hello","bro");
            LocationResult result = LocationResult.extractResult(intent);
            if (result == null) {
                LocationServiceMethods methods=new LocationServiceMethods(context);
                methods.checkProvider();
            }
            else{
                List<Location> locations = result.getLocations();
                String latlng=locations.get(0).getLatitude()+":"+locations.get(0).getLongitude();
                LocationRequestHelper.setLocationRequesting(context,latlng);
                LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);locationResultHelper.checkDistanceBetween(locations.get(0));
                locationResultHelper.saveResults();
                Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));
            }
        }

    }

}
