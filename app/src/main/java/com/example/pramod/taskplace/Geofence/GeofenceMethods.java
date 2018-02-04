package com.example.pramod.taskplace.Geofence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.LocationService.LocationRequestHelper;
import com.example.pramod.taskplace.Activities.MainActivity;
import com.example.pramod.taskplace.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pramod on 28/1/18.
 */

public class GeofenceMethods {
    NotificationManager notificationManager;
    Context context;
    GoogleApiClient mGoogleApiClient;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ArrayList<Geofence> mGeofenceList=new ArrayList<Geofence>();
    public HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    public GeofenceMethods(Context context,GoogleApiClient mGoogleApiClient){
        this.context=context;
        this.mGoogleApiClient=mGoogleApiClient;
    }
    public GeofenceMethods(Context context, GoogleApiClient mGoogleApiClient,HashMap<String,LatLng> LANDMARKS){
        this.context=context;
        this.mGoogleApiClient=mGoogleApiClient;
        this.LANDMARKS=LANDMARKS;
        preferences= PreferenceManager.getDefaultSharedPreferences(context);
        editor=preferences.edit();
    }
    public void startGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Log.i("not conn","API not connected");
            return;
        }
        try {
            //Toast.makeText(getActivity().getApplicationContext(), "adding geofence", Toast.LENGTH_SHORT).show();
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(mGeofenceList), getGeofencePendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    LocationRequestHelper.setRequesting(context,false);
                                    GeofenceRequestHelper.setGeoReuesting(context,false);
                                    showOrHideNotification();
                                    Log.i("ADDED GEOFENCE",String.valueOf(GeofenceRequestHelper.getGeoReuesting(context)));
                                }
                                else {
                                    //request high frequency permission
                                    //Toast.makeText(getActivity().getApplicationContext(), "Geofences cannot be Added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

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
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void removeGeofence(final ArrayList<String> ids){
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,ids).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    /*SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("FLAG", "allowed");
                    editor.commit();
                    Log.i("FLAG","allowed");
                    Log.i("Geofence","successfully geofence removed");*/
                    GeofenceRequestHelper.setGeoReuesting(context,true);//true so that geofence can be added
                    LocationRequestHelper.setRequesting(context,true);
                    showOrHideNotification();
                    Log.i("REMOVED GEOFENCE",String.valueOf(GeofenceRequestHelper.getGeoReuesting(context)));
                }
                else{
                    Log.i("REMOVE GEOFENCE","CANNOT REMOVE GEOFENCE");

                }
            }
        });
    }
    public void populateGeofences() {
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
        }
        catch (Exception e){Log.i("ERROR WHILE POPULATING",String.valueOf(e));}
        startGeofences();

    }



    void showOrHideNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);
        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);
        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("TaskPlace")
                .setContentText("Service is active")
                .setAutoCancel(false)

                .setOngoing(true)
                .setContentIntent(notificationPendingIntent)
                .build();
        notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(GeofenceRequestHelper.getGeoReuesting(context)==true){
            notificationManager.cancel(0);
        }else{
            notificationManager.notify(0,notification);
        }
    }


    public void removeallgeofences(){
        ArrayList<String> places=new ArrayList<>();
        DatabaseHelper db=new DatabaseHelper(context);
        SQLiteDatabase sql=db.getReadableDatabase();
        Cursor cursor=sql.rawQuery("select * from TaskPlace",null);
        while (cursor.moveToNext()){
            places.add(cursor.getString(2));
        }
        removeGeofence(places);
    }
}
