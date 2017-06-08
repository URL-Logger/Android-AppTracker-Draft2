package com.apres.cmps116.url_logger;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


// Logic to deal with collecting data on boot

public class BootReceiver extends BroadcastReceiver { //Checks when phone is turned on


    public void onReceive(Context context, Intent intent){

        SharedPreferences loginSettings = context.getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isChecked = loginSettings.getBoolean("ischecked", true);
      if (isChecked == true) { //If service was on before turn it on again
            Intent serviceIntent = new Intent(context, MyService.class);
            context.startService(serviceIntent);
        }
    }

}
