package com.example.pramod.taskplace.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.pramod.taskplace.LocationService.LocationRequestHelper;

/**
 * Created by pramod on 26/2/18.
 */
public class SwipeActionEvent extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //set flag to true so that to receive message again
        LocationRequestHelper.setNotificationFlag(context, true);
    }

}
