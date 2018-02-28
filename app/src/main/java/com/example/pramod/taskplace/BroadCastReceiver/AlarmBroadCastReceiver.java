package com.example.pramod.taskplace.BroadCastReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.example.pramod.taskplace.Activities.ScrollingActivity;
import com.example.pramod.taskplace.R;

/**
 * Created by pramod on 26/2/18.
 */

public class AlarmBroadCastReceiver extends BroadcastReceiver {
    NotificationManager mNotificationManager;
    final private static String PRIMARY_CHANNEL = "default";
    Context mContext;
    String task_id,tasktitle,place;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        task_id=intent.getExtras().getString("task_id");
        tasktitle=intent.getExtras().getString("task_title");
        place=intent.getExtras().getString("task_place");
        showNotificationFromAlarm();
    }
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    public void showNotificationFromAlarm(){
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(PRIMARY_CHANNEL, mContext.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }
        //swipe delete
        //Intent swipeActionIntent=new Intent(mContext,SwipeActionEvent.class);
        //PendingIntent swipePendingIntent=PendingIntent.getBroadcast(mContext,1,swipeActionIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        //end of swipe delete
        Intent notificationIntent = new Intent(mContext, ScrollingActivity.class);
        notificationIntent.putExtra("task_id",task_id);
        notificationIntent.putExtra("NotifyPage","fromNotif");
        /*PendingIntent notificationPendingIntent=PendingIntent.getActivity(mContext,1,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(mContext);
        String url=preferences.getString("notifications_new_message_ringtone","content://settings/system/notification_sound");
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext,PRIMARY_CHANNEL);
        notification.setSmallIcon(android.R.drawable.ic_notification_overlay);
        notification.setContentTitle(place);
        notification.setContentText(tasktitle);
        notification.setAutoCancel(true);
        //notification.addAction(R.drawable.ic_logout_black_24dp,"MARK AS DONE",pendingIntent);
        notification.setContentIntent(notificationPendingIntent);
        notification.setSound(Uri.parse(url));
        if(preferences.getBoolean("notifications_new_message_vibrate",true)){
            notification.setVibrate(new long[]{50,120,200,300});
        }
        //notification.setDeleteIntent(swipePendingIntent);
        getNotificationManager().notify(sr_no,notification.build());*/
        wakeUp();
        mContext.startActivity(notificationIntent);
    }
    public void wakeUp(){
        PowerManager powerManager= (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if(!powerManager.isScreenOn()){
            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
            wl_cpu.acquire(10000);
        }
    }


}
