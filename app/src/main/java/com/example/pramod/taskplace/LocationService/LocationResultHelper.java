package com.example.pramod.taskplace.LocationService;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationChannel;
import android.util.Log;

import com.example.pramod.taskplace.BroadCastReceiver.AlarmBroadCastReceiver;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.Activities.ScrollingActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to process location results.
 */
public class LocationResultHelper {
    ArrayList<String> locationData=new ArrayList<>();
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    private static Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;
    private String tasktitle,taskdesc,place,task_;
    private int sr_no;
    AlarmManager alarmManager;

    public LocationResultHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;
    }

    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     */
    private String getLocationResultTitle() {
        String numLocationsReported = mContext.getResources().getQuantityString(R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    private String getLocationResultText() {
        if (mLocations.isEmpty()) {
            return mContext.getString(R.string.unknown_location);
        }
        //String latlng=mLocations.get(0).getLatitude()+":"+mLocations.get(0).getLongitude();
       // LocationRequestHelper.setLocationRequesting(mContext,latlng);
        StringBuilder sb = new StringBuilder();
        for (Location location : mLocations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     */
    void saveResults() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle() + "\n" +
                        getLocationResultText())
                .apply();
    }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */


    /**
     * Displays a notification with the location results.
     */

    void showNotification(int sr_no,String place,String tasktitle,String task_id) {
        Log.i("showNotification()","inside noti");
        alarmManager= (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(mContext,AlarmBroadCastReceiver.class);
        intent.putExtra("task_id",task_id);
        intent.putExtra("task_place",place);
        intent.putExtra("task_title",tasktitle);
        intent.putExtra("sr_no",String.valueOf(sr_no));
        PendingIntent pendingIntent=PendingIntent.getBroadcast(mContext,sr_no,intent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,0,pendingIntent);
        //notification channel is required for Android oreo
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDistanceBetween(Location currLoc){
        DatabaseHelper db=new DatabaseHelper(mContext);
        SQLiteDatabase sql=db.getReadableDatabase();
        String query="select * from Task";
        Location destloc=new Location("");
        Cursor cursor = sql.rawQuery(query, null);
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(mContext);
        while(cursor.moveToNext()){
            destloc.setLatitude(Double.valueOf(cursor.getString(6)));
            destloc.setLongitude(Double.valueOf(cursor.getString(7)));
            if(currLoc.distanceTo(destloc)<Integer.parseInt(preferences.getString("radius","100"))){
                taskdesc=cursor.getString(4);
                tasktitle=cursor.getString(3);
                sr_no= cursor.getInt(0);
                place=cursor.getString(2);
                if (LocationRequestHelper.getNotificationFlag(mContext) == true) {
                        showNotification(sr_no, place, tasktitle, cursor.getString(1));
                        LocationRequestHelper.setNotificationFlag(mContext, false);
                }

            }
            else{
                LocationRequestHelper.setNotificationFlag(mContext,true);
            }
        }
    }

}

