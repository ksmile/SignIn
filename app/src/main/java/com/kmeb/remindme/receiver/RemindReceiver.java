package com.kmeb.remindme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.kmeb.remindme.service.RemindService;

/**
 * Created by mooning on 2015/7/7.
 */
public class RemindReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("time", Context.MODE_PRIVATE);
        Boolean isServiceOpened = pref.getBoolean("isServiceOpened", false);
        if(isServiceOpened) {
            Intent i = new Intent(context, RemindService.class);
            RemindService.isFromReceiver = true;
            context.startService(i);
        }

    }
}
