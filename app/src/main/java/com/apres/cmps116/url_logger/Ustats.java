package com.apres.cmps116.url_logger;

/**
 * Created by cedriclinares on 2/18/17.
 */

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

class UStats {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");
    public static final String TAG = UStats.class.getSimpleName();
    @SuppressWarnings("ResourceType")
    static long startTime = 1483228800;

    //This function is what aggregates all of the usage stats together
    public static Map<String,UsageStats> getUsageStatsList(MyService context){
        UsageStatsManager usm = getUsageStatsManager((Context) context);
        Calendar calendar = Calendar.getInstance();
        //long startTime = calendar.getTimeInMillis();
        long endTime = calendar.getTimeInMillis();
         //January 1st 2017

        if (endTime==startTime+31536000) {startTime = endTime;}

        Log.d(TAG, "Range start:" + dateFormat.format(startTime) );
        Log.d(TAG, "Range end:" + dateFormat.format(endTime));

        Map<String,UsageStats> usageStatsList = usm.queryAndAggregateUsageStats(startTime,endTime);
        return usageStatsList;
    }


    public static void printUsageStats(List<UsageStats> usageStatsList){
        for (UsageStats u : usageStatsList){
            Log.d(TAG, "Pkg: " + u.getPackageName() +  "\t" + "ForegroundTime: "
                    + u.getTotalTimeInForeground()) ;
        }

    }

    @SuppressWarnings("ResourceType")
    private static UsageStatsManager getUsageStatsManager(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
        return usm;
    }
}
