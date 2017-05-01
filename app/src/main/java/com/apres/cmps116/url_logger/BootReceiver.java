package com.apres.cmps116.url_logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by cedriclinares on 4/30/17.
 */

// Logic to deal with collecting data on boot

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent){
        Toast.makeText(context, "On Boot method", Toast.LENGTH_LONG).show();
        Intent serviceIntent = new Intent (context, MyService.class);
        context.startService(serviceIntent);
    }
}
