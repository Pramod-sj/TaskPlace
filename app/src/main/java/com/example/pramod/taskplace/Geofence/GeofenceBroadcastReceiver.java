/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pramod.taskplace.Geofence;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.pramod.taskplace.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Receiver for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver{
    Context contextBootReceiver;
    HashMap<String, LatLng> LANDMARKS;
    GoogleApiClient mGoogleApiClient;
    DatabaseHelper db;
    SQLiteDatabase sql;
    private static ArrayList<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;
    private static final String TAG = "BootReceiver";
    DatabaseReference taskDetailsCloudEndPoint;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    SharedPreferences preferences;

    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     *                Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        contextBootReceiver= context;
        mGoogleApiClient = new GoogleApiClient.Builder(contextBootReceiver)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i("onConnected","Hello");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i("onConnectionSuspended","Hello");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i("onConnectionFailed","Hello");
                    }
                })
                .addApi(LocationServices.API)
                .build();
        Log.i("loc change",String.valueOf(mGoogleApiClient.isConnected()));
        LocationManager locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                if(!GeofenceRequestHelper.getGeoReuesting(contextBootReceiver)) {

                    mGoogleApiClient.connect();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("API", String.valueOf(mGoogleApiClient.isConnected()));
                            fetchOfflineData();
                        }
                    },2000);
                }
                //We got our GPS stuff up, add our geofences!
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            mGoogleApiClient.connect();
            if(!GeofenceRequestHelper.getGeoReuesting(contextBootReceiver)) {
                mGoogleApiClient.connect();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("API", String.valueOf(mGoogleApiClient.isConnected()));
                        fetchOfflineData();
                    }
                },2000);
            }

        }
        else{
            GeofenceTransitionsIntentService.enqueueWork(context,intent);
        }
    }
    public int fetchOfflineData() {
        db=new DatabaseHelper(contextBootReceiver);
        sql=db.getWritableDatabase();

        LANDMARKS = new HashMap<String, LatLng>();
        String query = "select * from PlaceDatabase";
        Cursor cursor = sql.rawQuery(query, null);
        while (cursor.moveToNext()) {
            if (cursor.getCount() == 0) {
                return 0;
            }
            LANDMARKS.put(cursor.getString(1), new LatLng(Float.parseFloat(cursor.getString(5)), Float.parseFloat(cursor.getString(6))));
        }
        Log.i("Populated", "started Populated");
        GeofenceMethods geofenceMethods=new GeofenceMethods(contextBootReceiver,mGoogleApiClient,LANDMARKS);
        geofenceMethods.populateGeofences();
        return LANDMARKS.size();
    }

    public void getGeofencesFromDatabase() {
        LANDMARKS = new HashMap<String, LatLng>();
        taskDetailsCloudEndPoint = FirebaseDatabase.getInstance().getReference().child("Users");
        //Toast.makeText(contextBootReceiver, "Getting data", Toast.LENGTH_SHORT).show();
        taskDetailsCloudEndPoint.child(new CurrentUserData(contextBootReceiver).getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(contextBootReceiver, "not populating", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    float longi = ds.child("latlng").child("longitude").getValue(Float.class);
                    float lati = ds.child("latlng").child("latitude").getValue(Float.class);
                    String place_n = ds.child("place").getValue(String.class);
                    LANDMARKS.put(place_n, new LatLng(lati, longi));
                    //adding id i.e.place name for removing Geofences
                }
                Log.i("Populated", "started Populated");
                GeofenceMethods geofenceMethods=new GeofenceMethods(contextBootReceiver,mGoogleApiClient,LANDMARKS);
                geofenceMethods.populateGeofences();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }


    private boolean isLocationModeAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= 19 && getLocationMode(context) != Settings.Secure.LOCATION_MODE_OFF) {
            return true;
        } else return false;
    }

    public boolean isLocationServciesAvailable(Context context) {
        if (Build.VERSION.SDK_INT < 19) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        } else return false;
    }

    public int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
