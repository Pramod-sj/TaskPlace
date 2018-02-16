package com.example.pramod.taskplace.LocationService;
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
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.app.NotificationChannel;
import android.util.Log;
import com.example.pramod.taskplace.Activities.MainActivity;
import com.example.pramod.taskplace.Activities.Notificationpage;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.R;
import com.expertprogramming.taskplace.ScrollingActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to process location results.
 */
class LocationResultHelper {
    ArrayList<String> locationData=new ArrayList<>();
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final private static String PRIMARY_CHANNEL = "default";
    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;
    private String sr_no,tasktitle,taskdesc,place;
    LocationResultHelper(Context context, List<Location> locations) {
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
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * Displays a notification with the location results.
     */

    void showNotification(String sr_no,String place,String tasktitle,String taskdesc,String task_id) {

        //notification channel is required for Android oreo
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(PRIMARY_CHANNEL, mContext.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }

        //Intent i=new Intent(mContext,LocationUpdatesBroadcastReceiver.class);
        //PendingIntent broadIntent=PendingIntent.getBroadcast(mContext,0,i,0);
        //swipe delete
        Intent swipeActionIntent=new Intent(mContext,SwipeActionEvent.class);
        PendingIntent swipePendingIntent=PendingIntent.getBroadcast(mContext,1,swipeActionIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        //end of swipe delete

        Intent actionIntent=new Intent(mContext,ActionEvent.class);
        Log.i("not_id",task_id);
        actionIntent.putExtra("task_id",task_id);
        actionIntent.putExtra("not_id",sr_no);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(mContext,1,actionIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent notificationIntent = new Intent(mContext, ScrollingActivity.class);
        notificationIntent.putExtra("task_id",task_id);
        notificationIntent.putExtra("NotifyPage","fromNotif");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(mContext);
        String url=preferences.getString("notifications_new_message_ringtone","content://settings/system/notification_sound");
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext,PRIMARY_CHANNEL);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setContentTitle(place);
        notification.setContentText(tasktitle);
        notification.setAutoCancel(true);
        notification.addAction(R.drawable.ic_logout_black_24dp,"MARK AS DONE",pendingIntent);
        notification.setContentIntent(notificationPendingIntent);
        notification.setSound(Uri.parse(url));
        if(preferences.getBoolean("notifications_new_message_vibrate",true)){
            notification.setVibrate(new long[]{50,70,100,120});
        }
        notification.setDeleteIntent(swipePendingIntent);
        getNotificationManager().notify(Integer.parseInt(sr_no),notification.build());
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
                sr_no=cursor.getString(0);
                place=cursor.getString(2);
                if(LocationRequestHelper.getNotificationFlag(mContext)==true) {
                    showNotification(sr_no, place, tasktitle, taskdesc, cursor.getString(1));
                    LocationRequestHelper.setNotificationFlag(mContext,false);
                }
            }
        }

    }
    public static class ActionEvent extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
                DatabaseHelper db = new DatabaseHelper(context);
                SQLiteDatabase sql = db.getWritableDatabase();
                FirebaseDatabaseHelper helper=new FirebaseDatabaseHelper(context);
                helper.removeDatafromFirebase(intent.getStringExtra("task_id"));
                sql.delete("Task", "task_id=?", new String[]{intent.getStringExtra("task_id")});
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Integer.parseInt(intent.getStringExtra("not_id")));
                LocationRequestHelper.setNotificationFlag(context, true);
        }
    }
    public static class SwipeActionEvent extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //set flag to true so that to receive message again
            LocationRequestHelper.setNotificationFlag(context, true);
        }

    }

}

