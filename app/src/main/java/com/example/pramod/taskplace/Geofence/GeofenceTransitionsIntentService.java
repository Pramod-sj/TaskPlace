package com.example.pramod.taskplace.Geofence;

/**
 * Created by pramod on 7/1/18.
 */
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.example.pramod.taskplace.Activities.Notificationpage;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeofenceTransitionsIntentService extends JobIntentService{
    /**
     * Convenience method for enqueuing work in to this service.
     */
    String taskTitle;
    String taskDesc;
    String taskPlace;

    HashMap<String,String> hp=new HashMap<>();
    private static final int JOB_ID = 573;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference taskDetailsCloudEndPoint;
    protected ArrayList<Geofence> mGeofenceList;
    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    Context context;
    String task_id=null;
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

    String getGeofenceTransitionDetails(GeofencingEvent event) {
        String e = "";
        String transitionString = GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        List triggeringIDs = new ArrayList();
        int geofenceTransition = event.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            e = "You just Entered";
            Log.i("details",event.getTriggeringGeofences().toString());
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            e = "You just Left";
        }
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        task_id=triggeringIDs.get(0).toString();
        Log.i("task",task_id);
        return String.format("%s %s", e, triggeringIDs.get(0));
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts MainActivity.
        //getGeofencesFromDatabase();
        //Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        //notificationIntent.putExtra("NotifyPage", "ViewTask");
        Intent i=new Intent(getApplicationContext(),Notificationpage.class);
        i.putExtra("task_id",task_id);
        startActivity(i);

        // Get a PendingIntent containing the entire back stack.
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class).addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String u=preferences.getString("notifications_new_message_ringtone",null);
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // Define the notification settings.
        NotificationCompat.Builder builder1 = builder.setColor(Color.RED)
                .setContentTitle(taskTitle)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentText(taskDesc)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setSound(Uri.parse(u));
        // Fire and notify the built Notification.
        if(preferences.getBoolean("notifications_new_message_vibrate",false)==true){
            builder1.setVibrate(new long[]{150, 300, 150, 400});
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());*/
    }
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


}