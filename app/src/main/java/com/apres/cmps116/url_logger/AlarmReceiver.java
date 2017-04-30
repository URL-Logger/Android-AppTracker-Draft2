package com.apres.cmps116.url_logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by bereket on 4/24/17.
 */

public class AlarmReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent background = new Intent(context, MyService.class);
        context.startService(background);

    }

}
