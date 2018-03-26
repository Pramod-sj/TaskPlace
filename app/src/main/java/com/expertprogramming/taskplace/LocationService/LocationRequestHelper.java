package com.expertprogramming.taskplace.LocationService;

/**
 * Created by pramod on 3/2/18.
 */
import android.content.Context;
import android.preference.PreferenceManager;


public class LocationRequestHelper {

    public final static String LOCATION_UPDATES_TRIGGER= "trigger";
    public final static String LOCATION_UPDATES = "location-updates";
    public final static String NOTIFICATION_FLAG="noti_flag";
    public static void setRequestingTrigger(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOCATION_UPDATES_TRIGGER, value)
                .apply();
    }

    public static boolean getRequestingTrigger(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LOCATION_UPDATES_TRIGGER, true);
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

    public static void setNotificationFlag(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(NOTIFICATION_FLAG,value)
                .apply();
    }

    public static boolean getNotificationFlag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(NOTIFICATION_FLAG,true);
    }
}