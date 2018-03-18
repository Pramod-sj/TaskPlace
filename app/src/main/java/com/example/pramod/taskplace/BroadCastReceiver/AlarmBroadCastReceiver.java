package com.example.pramod.taskplace.BroadCastReceiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
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
    int sr_no;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        task_id=intent.getExtras().getString("task_id");
        tasktitle=intent.getExtras().getString("task_title");
        place=intent.getExtras().getString("task_place");
        sr_no= Integer.parseInt(intent.getExtras().getString("sr_no"));
        showNotificationFromAlarm();

    }
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    @SuppressLint("WrongConstant")
    public void showNotificationFromAlarm() {
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(PRIMARY_CHANNEL, mContext.getString(R.string.default_channel), NotificationManager.IMPORTANCE_MAX);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setVibrationPattern(new long[]{0,1000,900,1000,900,1000,900,1000,0});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getNotificationManager().createNotificationChannel(channel);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (Integer.parseInt(preferences.getString("not_type", "0")) == 0) {
            Intent notificationIntent = new Intent(mContext, ScrollingActivity.class);
            notificationIntent.putExtra("task_id", task_id);
            notificationIntent.putExtra("NotifyPage", "fromNotif");
            notificationIntent.putExtra("not_type","2");
            PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            String url = preferences.getString("notifications_new_message_ringtone","content://settings/system/alarm_alert");
            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext, PRIMARY_CHANNEL);
            notification.setSmallIcon(R.drawable.ic_task_white_24dp);
            notification.setContentTitle(place);
            notification.setContentText(tasktitle);
            notification.setAutoCancel(true);
            notification.setFullScreenIntent(notificationPendingIntent,true);
            //notification.addAction(R.drawable.ic_logout_black_24dp,"MARK AS DONE",pendingIntent);
            notification.setContentIntent(notificationPendingIntent);
            notification.setSound(Uri.parse(url));
            if (preferences.getBoolean("notifications_new_message_vibrate", true)) {
                notification.setVibrate(new long[]{0,1000,900,1000,900,1000,900,1000,0});
            }
            getNotificationManager().notify(sr_no, notification.build());
            wakeUp();
        } else {
            Intent notificationIntent = new Intent(mContext, ScrollingActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationIntent.putExtra("task_id", task_id);
            notificationIntent.putExtra("NotifyPage", "fromNotif");
            notificationIntent.putExtra("not_type","1");
            mContext.startActivity(notificationIntent);
        }
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
