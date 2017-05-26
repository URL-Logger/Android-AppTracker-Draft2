package com.apres.cmps116.url_logger;

/**
 * Created by cedriclinares on 3/7/17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

import static android.content.Context.MODE_PRIVATE;

public class AppsUsageItem {
    public String pkgName;
    public String appName;
    public long lastStartup;
    public String lastStartupStr;
    public long fgTime;
    public long firsttime;
    public long lastime;
    public long currenttime;
    public int mLaunchCount;

    //==================================================================================
    private SharedPreferences appData;      //Will contain app timestamps
    private SharedPreferences.Editor editor;
    private Context context;

    public AppsUsageItem(Context context){
        this.context = context;
    }

    //============= Setters ============
    public void setForeground(long foreground){
        editor.putLong("FOREGROUND_TIME", foreground);
        editor.commit();
    }
    public void setLastTimeUsed(long lastTimeUsed){
        editor.putLong("LAST_TIME", lastTimeUsed);
        editor.commit();
    }

    public void setStartTime(long startTime){
        editor.putLong("START_TIME", startTime);
        editor.commit();
    }

    public void setEndTime(long endTime){
        editor.putLong("END_TIME", endTime);
        editor.commit();
    }

    public void setTotalTime(long totalTime){
        editor.putLong("TOTAL_TIME", totalTime);
        editor.commit();
    }

    public void setAppName(String apName){
        editor.putString("APP_NAME", apName);
        editor.commit();
    }
    //============= Getters ============
    public long getForeground(){
        return appData.getLong("FOREGROUND_TIME", 0);
    }
    public long getLastTimeUsed(){
        return appData.getLong("LAST_TIME", 0);
    }

    public long getStartTime(){
        return appData.getLong("START_TIME", 0);
    }

    public long getEndTime(){
        return appData.getLong("END_TIME", 0);
    }

    public long getTotalTime(){
        return appData.getLong("TOTAL_TIME", 0);
    }

    public String getAppName(){
        return appData.getString("APP_NAME", "");
    }
    public void createAppData(String apName){
        appData = context.getSharedPreferences(apName, MODE_PRIVATE);
        editor = appData.edit();
    }

    public boolean appDataExists(String apName){
        File f = new File("/data/data/com.apres.cmps116.url_logger/shared_prefs/" + apName + ".xml");
        if(f.exists()) {
            appData = context.getSharedPreferences(apName, MODE_PRIVATE);
            editor = appData.edit();
            return true;
        }
        else
            return false;
    }
    //=====================================================================================

    public String userid;

    public int getlaunch() {
        return mLaunchCount;
    }
    public String getpackagename() {
        return appName ;
    }
    public long getfirsttime() {
        return firsttime;
    }
    public long getlasttime() {
        return lastime;
    }
    public long getlastused() {
        return lastStartup;
    }
    public long getforeground() {
        return fgTime;
    }



    public static class AppNameComparator implements Comparator<AppsUsageItem> {
        private Collator mCollator = Collator.getInstance();

        @Override
        public int compare(AppsUsageItem lhs, AppsUsageItem rhs) {
            return mCollator.compare(lhs.appName, rhs.appName);
        }
    }

    public static class LastStartupComparator implements Comparator<AppsUsageItem> {
        @Override
        public int compare(AppsUsageItem lhs, AppsUsageItem rhs) {
            if (lhs.lastStartup < rhs.lastStartup) {
                return 1;
            } else if (lhs.lastStartup > rhs.lastStartup) {
                return -1;
            }
            return 0;
        }
    }

    public static class FgTimeComparator implements Comparator<AppsUsageItem> {
        @Override
        public int compare(AppsUsageItem lhs, AppsUsageItem rhs) {
            if (lhs.fgTime < rhs.fgTime) {
                return 1;
            } else if (lhs.fgTime > rhs.fgTime) {
                return -1;
            }
            return 0;
        }
    }
}
