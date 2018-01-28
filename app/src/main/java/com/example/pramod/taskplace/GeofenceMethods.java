package com.example.pramod.taskplace;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pramod on 28/1/18.
 */

public class GeofenceMethods {
    Context context;
    GoogleApiClient mGoogleApiClient;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    GoogleMap mMap;
    ArrayList<Geofence> mGeofenceList=new ArrayList<Geofence>();
    String map;
    public HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    GeofenceMethods(Context context,GoogleApiClient mGoogleApiClient){
        this.context=context;
        map="";
        this.mGoogleApiClient=mGoogleApiClient;
    }
    GeofenceMethods(Context context, GoogleApiClient mGoogleApiClient,GoogleMap mMap,HashMap<String,LatLng> LANDMARKS,ArrayList<Geofence> mGeofenceList){
        this.context=context;
        this.mGoogleApiClient=mGoogleApiClient;
        this.mGeofenceList=mGeofenceList;
        this.mMap=mMap;
        map="map";
        this.LANDMARKS=LANDMARKS;
        preferences= PreferenceManager.getDefaultSharedPreferences(context);
        editor=preferences.edit();
    }
    GeofenceMethods(Context context, GoogleApiClient mGoogleApiClient,HashMap<String,LatLng> LANDMARKS,ArrayList<Geofence> mGeofenceList){
        this.context=context;
        this.mGeofenceList=mGeofenceList;
        this.mGoogleApiClient=mGoogleApiClient;
        this.LANDMARKS=LANDMARKS;
        map="";
        preferences= PreferenceManager.getDefaultSharedPreferences(context);
        editor=preferences.edit();
    }
    public void startGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Log.i("not conn","API not connected");
            return;
        }
        try {
            if (preferences.getString("FLAG", null).equals("allowed")) {
                //Toast.makeText(getActivity().getApplicationContext(), "adding geofence", Toast.LENGTH_SHORT).show();
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(mGeofenceList), getGeofencePendingIntent())
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("FLAG", "notallowed");
                                    editor.commit();
                                    Log.i("FLAG","not allowed");
                                    if(map.equals("map")) {
                                        for (Map.Entry<String, LatLng> entry : LANDMARKS.entrySet()) {
                                            addMarker(entry.getKey(), new LatLng(entry.getValue().latitude, entry.getValue().longitude));
                                        }
                                    }
                                    // Result processed in onResult().
                                }
                                else {
                                    //request high frequency permission
                                    //Toast.makeText(getActivity().getApplicationContext(), "Geofences cannot be Added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }


        }
        catch(SecurityException securityException){
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }

    }
    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> mGeofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the
        //same pending intent back when calling addgeoFences()
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void removeGeofence(ArrayList<String> ids){

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        //if(!checkPermissions()){
        //    Snackbar.make(getActivity().findViewById(R.id.linearlayoutmap),"Permission Required",Snackbar.LENGTH_SHORT).show();
        //    return;
        //}
        if(ids.isEmpty()){
            Toast.makeText(context,"Cannot remove no geofence found",Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,ids).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("FLAG", "allowed");
                    editor.commit();
                    Log.i("FLAG","allowed");

                    Log.i("Geofence","successfully geofence removed");
                    //populateGeofences();
                    if(map.equals("map")) {
                        mMap.clear();
                    }
                }
                else{
                    Log.i("Geofence","Cannot remove geofence");

                }
            }
        });
    }
    private void addMarker(String key, LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
                .title("G:" + key)
                .position(latLng));
        mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(100.0f)
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor(Color.argb(80, 150,150,150)));
    }

    /*public void populateGeofences() {
        mGeofenceList=new ArrayList<Geofence>();
        if(!mGeofenceList.isEmpty()){
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
            Log.i("FLAG",preferences.getString("FLAG",null));
            if(preferences.getString("FLAG", null).equals("notallowed")){
                for (Map.Entry<String, LatLng> entry : LANDMARKS.entrySet()) {
                    addMarker(entry.getKey(),new LatLng(entry.getValue().latitude, entry.getValue().longitude));
                }
            }
        }
        catch (Exception e){}
        Toast.makeText(getActivity().getApplicationContext(),"Populated",Toast.LENGTH_SHORT).show();
    }*/


}
