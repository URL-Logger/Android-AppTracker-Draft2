package com.apres.cmps116.url_logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cedriclinares on 4/30/17.
 */

// Logic to deal with collecting data on boot

public class BootReceiver extends BroadcastReceiver { //Checks when phone is turned on


    public void onReceive(Context context, Intent intent){
       //Log.d("On Boot", "Boot Recieved");

        SharedPreferences loginSettings = context.getSharedPreferences("LoginPrefs", MODE_PRIVATE);
       // Intent serviceIntent = new Intent(context, MyService.class);
        //context.startService(serviceIntent);
        boolean isChecked = loginSettings.getBoolean("ischecked", true);
      if (isChecked == true) { //If service was on before turn it on again
        //    Log.d("On Boot", "Service Started");
            Intent serviceIntent = new Intent(context, MyService.class);
            context.startService(serviceIntent);
        }
    }

}
