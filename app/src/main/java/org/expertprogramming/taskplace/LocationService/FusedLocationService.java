package org.expertprogramming.taskplace.LocationService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.expertprogramming.taskplace.NotificationHelper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.ContentValues.TAG;

/**
 * Created by pramod on 18/3/18.
 */

public class FusedLocationService extends Service {
    LocationRequest locationRequest;
    Context context;
    FusedLocationProviderClient fusedLocationProviderClient;
    NotificationManager mNotificationManager;
    public static final int DEFAULT_INTERVAL=10000;
    public static final int FASTEST_INTERVAL=5000;

    NotificationHelper helper;
    @Override
    public void onCreate(){
        context=getApplicationContext();
        helper=new NotificationHelper(context);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        locationRequest=createLocationRequest();
        startLocationUpdate();
        startServiceInForeground();

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){

        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }
    public LocationRequest createLocationRequest(){
        locationRequest=new LocationRequest();
        locationRequest.setInterval(DEFAULT_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        if(pref.getBoolean("mode",false)==true){
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            Log.i("locationRequest","Power Saving Mode");
        }
        else if(pref.getBoolean("mode",false)==false) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Log.i("locationRequest","High Accuracy Mode");
        }
        return locationRequest;
    }
    @Override
    public void onDestroy(){
        stopLocationUpdates();
        stopServiceInForeground();
    }
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    public void startServiceInForeground(){
        Notification notification=helper.getServiceNotification();
        getNotificationManager().notify(NotificationHelper.SERVICE_NOTIFICATION_ID,notification);
        startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID,notification);
    }
    public void stopServiceInForeground(){
        stopForeground(true);
    }
    public void startLocationUpdate(){
        try {
            Log.i(TAG, "Starting location updates");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "Started location updates");
                    LocationRequestHelper.setRequestingTrigger(context,false);
                }
            });

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    public void stopLocationUpdates() {
        try {
            Log.i(TAG, "Removing location updates");
            fusedLocationProviderClient.removeLocationUpdates(getPendingIntent()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "Successfully Removed location updates");
                    LocationRequestHelper.setRequestingTrigger(context, true);

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
