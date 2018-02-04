package com.example.pramod.taskplace.LocationService;

/**
 * Created by pramod on 3/2/18.
 */
import android.content.Context;
import android.preference.PreferenceManager;


public class LocationRequestHelper {

    public final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    public final static String LOCATION_UPDATES = "location-updates";

    public static void setRequesting(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    public static boolean getRequesting(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    public static void setLocationRequesting(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(LOCATION_UPDATES,String.valueOf(value))
                .apply();
    }

    public static String getLocationRequesting(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOCATION_UPDATES,"");
    }
}