package com.example.pramod.taskplace.LocationService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.example.pramod.taskplace.Activities.MainActivity;
import com.example.pramod.taskplace.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import es.dmoral.toasty.Toasty;

import static android.content.ContentValues.TAG;

/**
 * Created by pramod on 7/2/18.
 */

public class LocationServiceMethods {
    GoogleApiClient mGoogleApiClient;
    Context context;
    LocationRequest locationRequest;
    LocationManager lm;
    public LocationServiceMethods(Context context,GoogleApiClient mGoogleApiClient){
        this.mGoogleApiClient=mGoogleApiClient;
        this.context=context;
        this.lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }
    public LocationServiceMethods(Context context){
        this.context=context;
        this.lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    public void checkProvider(){
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Toasty.error(context, "There is no way to get location", Toast.LENGTH_SHORT).show();
        }
        else {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            }
            else{
                Toasty.warning(context, "App may not work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public int getLocationMode() {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }
    public void createLocationRequest(){
        locationRequest=new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=pref.edit();
        if(pref.getBoolean("mode",false)==true){
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        else if(pref.getBoolean("mode",false)==false) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }


    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, getPendingIntent());
            LocationRequestHelper.setRequestingTrigger(context,false);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                getPendingIntent());
        LocationRequestHelper.setRequestingTrigger(context,true);
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
