package com.example.pramod.taskplace;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
/**
 * Created by pramod on 2/3/18.
 */
public class ServiceNotification{
    Context context;
    NotificationManager notificationManager;
    NotificationChannel channel;
    String ChannelID="Service";
    public ServiceNotification(Context context){
        this.context=context;
        notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel=new NotificationChannel(ChannelID,"CHANNEL",NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void createServiceNotification(){
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context)
                .setContentTitle("TaskPlace Services are active")
                .setOngoing(true)
                .setAutoCancel(false)
                .setChannelId(ChannelID)
                .setSmallIcon(R.drawable.splashlogo)
                .setPriority(Notification.PRIORITY_MAX);
        notificationManager.notify(100,builder.build());
    }
    public void stopNotification(){
        notificationManager.cancel(100);
    }
}
