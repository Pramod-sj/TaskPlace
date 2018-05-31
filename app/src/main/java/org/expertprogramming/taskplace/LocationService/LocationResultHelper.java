package org.expertprogramming.taskplace.LocationService;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import org.expertprogramming.taskplace.Database.DatabaseHelper;
import org.expertprogramming.taskplace.NotificationHelper;
import org.expertprogramming.taskplace.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * Class to process location results.
 */
public class LocationResultHelper {
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    private static Context mContext;
    private List<Location> mLocations;
    private String tasktitle,taskdesc,place;
    private int sr_no;
    NotificationHelper notificationHelper;
    public LocationResultHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;
        notificationHelper=new NotificationHelper(context);
    }
    private String getLocationResultTitle() {
        String numLocationsReported = mContext.getResources().getQuantityString(R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    private String getLocationResultText() {
        if (mLocations.isEmpty()) {
            return mContext.getString(R.string.unknown_location);
        }
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
    void saveResults() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle() + "\n" +
                        getLocationResultText())
                .apply();
    }
    static String getSavedLocationResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }
    public void checkDistanceBetween(Location currLoc){
        DatabaseHelper db=new DatabaseHelper(mContext);
        SQLiteDatabase sql=db.getReadableDatabase();
        String query="select * from Task";
        Location destloc=new Location("");
        Cursor cursor = sql.rawQuery(query, null);
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("FLAG",false);
        boolean flag=false;
        TreeMap<Float,String> distance_list=new TreeMap<>();
        while(cursor.moveToNext()){
            destloc.setLatitude(Double.valueOf(cursor.getString(6)));
            destloc.setLongitude(Double.valueOf(cursor.getString(7)));
            //
            distance_list.put(currLoc.distanceTo(destloc),cursor.getString(2));
            //
            if(currLoc.distanceTo(destloc)<=Integer.parseInt(preferences.getString("radius","100"))){
                flag=true;
                taskdesc=cursor.getString(4);
                tasktitle=cursor.getString(3);
                sr_no= cursor.getInt(0);
                place= cursor.getString(2);
                if (LocationRequestHelper.getNotificationFlag(mContext) == true) {
                    notificationHelper.showNotification(sr_no, place, tasktitle, cursor.getString(1),0);
                    LocationRequestHelper.setNotificationFlag(mContext, false);
                    Log.i("FLAG","set to false");

                }
            }
            else{
                if(!flag) {
                    LocationRequestHelper.setNotificationFlag(mContext, true);
                    Log.i("FLAG", "set to true");
                }

            }
        }
        Notification notification=notificationHelper.updateServiceNotification(distance_list.firstEntry().getValue(),distance_list.firstEntry().getKey());
        notificationHelper.getNotificationManager().notify(NotificationHelper.SERVICE_NOTIFICATION_ID,notification);
        distance_list.clear();

    }

}

