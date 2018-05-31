package org.expertprogramming.taskplace;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.expertprogramming.taskplace.R;
import org.expertprogramming.taskplace.Activities.MainActivity;
import org.expertprogramming.taskplace.Activities.ScrollingActivity;
import org.expertprogramming.taskplace.BroadCastReceiver.AlarmBroadCastReceiver;

public class NotificationHelper {
    public static final int SERVICE_NOTIFICATION_ID=100000000;

    SharedPreferences preferences;
    String url;
    Context context;
    AlarmManager alarmManager;
    String task_id,tasktitle,place;
    int sr_no;
    NotificationManager mNotificationManager;
    final private static String PRIMARY_CHANNEL = "default";
    public NotificationHelper(Context context){
        this.context=context;
    }
    public NotificationHelper(Context context,String task_id,String tasktitle,String place,int sr_no){
        this.context=context;
        this.task_id=task_id;
        this.tasktitle=tasktitle;
        this.place=place;
        this.sr_no=sr_no;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        url = preferences.getString("notifications_new_message_ringtone","content://settings/system/alarm_alert");
        createChannel();
    }

    public void showNotification(int sr_no,String place,String tasktitle,String task_id,int snooze) {
        Log.i("showNotification()","inside noti");

        alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(context,AlarmBroadCastReceiver.class);
        intent.putExtra("task_id",task_id);
        intent.putExtra("task_place",place);
        intent.putExtra("task_title",tasktitle);
        intent.putExtra("sr_no",String.valueOf(sr_no));
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,sr_no,intent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,snooze,pendingIntent);
        //notification channel is required for Android oreo
    }


    public NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    @SuppressLint("WrongConstant")
    public void showNotificationFromAlarm() {

        Intent notificationIntent = new Intent(context, ScrollingActivity.class);
        notificationIntent.putExtra("task_id", task_id);
        notificationIntent.putExtra("sr_no",String.valueOf(sr_no));
        notificationIntent.putExtra("place",place);
        notificationIntent.putExtra("task_title",tasktitle);
        notificationIntent.putExtra("NotifyPage", "fromNotif");

        if (Integer.parseInt(preferences.getString("not_type", "0")) == 0) {
            notificationIntent.putExtra("not_type","2");
            PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, PRIMARY_CHANNEL);
            notification.setSmallIcon(R.drawable.ic_task_white_24dp);
            notification.setContentTitle(place);
            notification.setContentText(tasktitle);
            notification.setAutoCancel(true);
            notification.setOnlyAlertOnce(true);
            notification.setFullScreenIntent(notificationPendingIntent,true);
            notification.setContentIntent(notificationPendingIntent);
            notification.setSound(Uri.parse(url));
            if (preferences.getBoolean("notifications_new_message_vibrate", true)) {
                notification.setVibrate(new long[]{0,1000,900,1000,900,1000,900,1000,0});
            }
            getNotificationManager().notify(sr_no, notification.build());
            wakeUp();
        } else {
            notificationIntent.putExtra("not_type","1");
            context.startActivity(notificationIntent);
        }
    }
    public void wakeUp(){
        PowerManager powerManager= (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(!powerManager.isScreenOn()){
            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
            wl_cpu.acquire(10000);
        }
    }
    public void createChannel(){
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(PRIMARY_CHANNEL, context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setVibrationPattern(new long[]{0,1000,900,1000,900,1000,900,1000,900,1000,0});
            AudioAttributes audioAttributes=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(Uri.parse(url), audioAttributes);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getNotificationManager().createNotificationChannel(channel);
        }
    }

    public Notification getServiceNotification(){
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel=new NotificationChannel("Service_Channel","service", NotificationManager.IMPORTANCE_MIN);
            getNotificationManager().createNotificationChannel(channel);
        }
        Intent i=new Intent(context, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setAutoCancel(false)
                .setChannelId("Service_Channel")
                .setContentTitle("TaskPlace Services are running")
                .setSmallIcon(R.drawable.splashlogo)
                .setShowWhen(false)
                .setContentIntent(pendingIntent);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        return builder.build();
    }
    public Notification updateServiceNotification(String place, Float distance){
        String body=String.valueOf(distance.intValue())+" m away from "+place;
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel=new NotificationChannel("Service_Channel","service", NotificationManager.IMPORTANCE_MIN);
            getNotificationManager().createNotificationChannel(channel);
        }
        Intent i=new Intent(context, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setAutoCancel(false)
                .setChannelId("Service_Channel")
                .setContentTitle("TaskPlace Services are running")
                .setContentText(body)
                .setSmallIcon(R.drawable.splashlogo)
                .setShowWhen(false)
                .setContentIntent(pendingIntent);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        return builder.build();
    }

}
