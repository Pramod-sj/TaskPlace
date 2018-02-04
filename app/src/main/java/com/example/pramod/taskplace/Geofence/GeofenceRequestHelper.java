package com.example.pramod.taskplace.Geofence;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by pramod on 3/2/18.
 */

public class GeofenceRequestHelper {
    public static final String geoKey="GEOFENCE_STATUS";
    public static void setGeoReuesting(Context context,boolean value){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(geoKey,value).commit();
    }
    public static boolean getGeoReuesting(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(geoKey,false);
    }
}
