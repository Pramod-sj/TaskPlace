package com.example.pramod.taskplace;

/**
 * Created by pramod on 7/1/18.
 */
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceTransitionsIntentService extends JobIntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    /**
     * Convenience method for enqueuing work in to this service.
     */
    HashMap<String,String> hp=new HashMap<>();
    private static final int JOB_ID = 573;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference taskDetailsCloudEndPoint;
    protected ArrayList<Geofence> mGeofenceList;
    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    Context context;
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            String e=getErrorString(event.getErrorCode());
            Log.i("Error",e);
            return;
        }
        String description = getGeofenceTransitionDetails(event);
        sendNotification(description);

    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {
        String e = "";
        String transitionString = GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        List triggeringIDs = new ArrayList();
        int geofenceTransition = event.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            e = "You just Entered";
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            e = "You just Left";
        }
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s %s have you completed your task", e, triggeringIDs.get(0));
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts MainActivity.
        //getGeofencesFromDatabase();
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.putExtra("NotifyPage", "ViewTask");
        // Get a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class).addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String u=preferences.getString("notifications_new_message_ringtone",null);
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // Define the notification settings.
        NotificationCompat.Builder builder1 = builder.setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Click to setup your task.... ;)")
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setSound(Uri.parse(u));
        // Fire and notify the built Notification.
        if(preferences.getBoolean("notifications_new_message_vibrate",false)==true){
            builder1.setVibrate(new long[]{150, 300, 150, 400});
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
    /*public void getGeofencesFromDatabase(){
        DatabaseReference taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        String UID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        taskDetailsCloudEndPoint.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!hp.isEmpty()){
                    hp.clear();
                }
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    String place_n=ds.child("place").getValue(String.class);
                    String content=ds.child("content").getValue(String.class);
                    hp.put(place_n,content);
                    //adding id i.e.place name for removing Geofences

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }*/// Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public void getGeofencesFromDatabase(){
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        taskDetailsCloudEndPoint.child(new CurrentUserData(context).getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    return;
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    float longi = ds.child("latlng").child("longitude").getValue(Float.class);
                    float lati = ds.child("latlng").child("latitude").getValue(Float.class);
                    String place_n = ds.child("place").getValue(String.class);
                    LANDMARKS.put(place_n, new LatLng(lati, longi));
                    //adding id i.e.place name for removing Geofences
                }
                populateGeofences();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        catch (Exception e){}
        startGeofences();
    }
    public void startGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.i("Success", "Success");
                            }
                            // Result processed in onResult().
                            else

                            {
                                //request high frequency permission

                                Log.i("failed", "failed");
                            }
                        }

                    });
        }
        catch(SecurityException securityException){
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }

    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the
        //same pending intent back when calling addgeoFences()
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}