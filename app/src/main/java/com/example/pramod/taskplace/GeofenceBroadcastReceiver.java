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
package com.example.pramod.taskplace;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Receiver for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver implements GoogleApiClient.OnConnectionFailedListener {
    Context contextBootReceiver;
    HashMap<String, LatLng> LANDMARKS;
    GoogleApiClient mGoogleApiClient;
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
        // Enqueues a JobIntentService passing the context and intent as parameters
        //GeofenceTransitionsIntentService.enqueueWork(context, intent);  //not sure if should create a new intent or not.
        //contextBootReceiver = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        contextBootReceiver=context;
        //SharedPreferences sharedPrefs;
        //SharedPreferences.Editor editor;
        LocationManager locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                mGoogleApiClient = new GoogleApiClient.Builder(contextBootReceiver)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(@Nullable Bundle bundle) {

                            }

                            @Override
                            public void onConnectionSuspended(int i) {

                            }
                        })
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();


                mGoogleApiClient.connect();
                getGeofencesFromDatabase();
                //We got our GPS stuff up, add our geofences!
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // isLocationModeAvailable for API >=19, isLocationServciesAvailable for API <19
            //sharedPrefs = context.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
            //editor = sharedPrefs.edit();
            //editor.remove("Geofences added");
            //editor.commit();
            mGeofencePendingIntent = null;
            mGoogleApiClient = new GoogleApiClient.Builder(contextBootReceiver)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();


            mGoogleApiClient.connect();
            getGeofencesFromDatabase();

        }
        else{
            GeofenceTransitionsIntentService.enqueueWork(context,intent);
        }
    }

    public void getGeofencesFromDatabase() {
        LANDMARKS = new HashMap<String, LatLng>();
        taskDetailsCloudEndPoint = FirebaseDatabase.getInstance().getReference().child("Users");
        //Toast.makeText(contextBootReceiver, "Getting data", Toast.LENGTH_SHORT).show();

        taskDetailsCloudEndPoint.child(new CurrentUserData(contextBootReceiver).getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //Toast.makeText(contextBootReceiver, "not populating", Toast.LENGTH_SHORT).show();
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
                populateGeofences();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void populateGeofences() {
        mGeofenceList = new ArrayList<Geofence>();
        if (!mGeofenceList.isEmpty()) {
            mGeofenceList.clear();
        }
        try {
            for (Map.Entry<String, LatLng> entry : LANDMARKS.entrySet()) {
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(entry.getKey())
                        .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 100.0f)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
            }
        } catch (Exception e) {
        }
        Log.i("Populated", "Populated");
        startGeofences();
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
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(contextBootReceiver, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the
        //same pending intent back when calling addgeoFences()
        return PendingIntent.getService(contextBootReceiver, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void startGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }
        try {
            Log.i("adding geofence", "adding");
            if (ActivityCompat.checkSelfPermission(contextBootReceiver, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("FLAG", "notallowed");
                                editor.commit();
                                Log.i("Geofences", "added");
                                // Result processed in onResult().
                            } else {
                                //request high frequency permission
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("FLAG", "notallowed");
                                editor.commit();
                                Intent i=new Intent(contextBootReceiver,MainActivity.class);
                                contextBootReceiver.startActivity(i);
                                Log.i("Geofences", "cannot add");
                            }
                        }
                    });

        }
        catch(SecurityException securityException){
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }

    }

}
