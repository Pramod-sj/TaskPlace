package org.expertprogramming.taskplace.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.expertprogramming.taskplace.NotificationHelper;

/**
 * Created by pramod on 26/2/18.
 */

public class AlarmBroadCastReceiver extends BroadcastReceiver {
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
        NotificationHelper helper=new NotificationHelper(mContext,task_id,tasktitle,place,sr_no);
        helper.showNotificationFromAlarm();
    }
}
