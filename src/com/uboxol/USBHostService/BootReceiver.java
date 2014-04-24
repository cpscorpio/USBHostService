package com.uboxol.USBHostService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootReceiver extends BroadcastReceiver
{
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Tools.info( intent.getAction());
        /**
         * 如果为开机广播则开启service
         * */
        if (BOOT_COMPLETED.equals(intent.getAction())) {
            MainService.startService(context);
        }
    }
}