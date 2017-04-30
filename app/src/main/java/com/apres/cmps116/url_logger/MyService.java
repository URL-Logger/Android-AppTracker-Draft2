package com.apres.cmps116.url_logger;

/**
 * Created by cedriclinares on 3/1/17.
 */

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.ArrayList;
import android.content.pm.PackageManager;
import java.lang.reflect.Field;
import java.util.Collections;
import android.content.SharedPreferences;
import com.android.volley.RequestQueue;
import android.app.usage.UsageStats;
import android.app.AlarmManager;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.util.Calendar;
import java.util.List;

public class MyService extends Service {


    LinearLayout statslist;
    private NotificationManager mNM;
    private PendingIntent pendingIntent;

    private boolean isRunning;
    private Context context;
    private Thread thread;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private RequestQueue mRequestQueue;

    //displaydata send;



    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    /*public MyService(displaydata context) {
        send=context;
    }*/


    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }


    @Override
    public void onCreate() {

        if (UStats.getUsageStatsList(this).isEmpty()){
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        //Toast.makeText(context, " test", Toast.LENGTH_SHORT);
        Log.d("OnCreate", "Started Service");
        this.context = this;
        this.isRunning = false;
        this.thread = new Thread(mytask);

        Intent alarmIntent = new Intent(this.context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MyService.this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000;

        //* Set the alarm to start at 10:30 AM *//*
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 30);

        //* Repeating on every 12 hours interval *//*
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                interval, pendingIntent);


        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //startService(new Intent(String.valueOf(MyService.class)));
        Log.d("OnCreate", "Before Notification");
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        Log.d("OnCreate", "After Notification");

        //getResults();

    }


    private Runnable mytask = new Runnable() {
        @Override
        public void run() {

            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
                    // statslist.removeAllViews();
                    int count=0;
                    List<AppsUsageItem> results = new ArrayList<AppsUsageItem>();
                    PackageManager pm = getPackageManager();
                    for (UsageStats usage : usageStatsList.values()) {
                        AppsUsageItem item = new AppsUsageItem();
                        item.pkgName = usage.getPackageName();
                        item.firsttime = usage.getFirstTimeStamp();
                        item.lastime = usage.getLastTimeUsed();
                        item.currenttime = System.currentTimeMillis();
                        // if (item.pkgName.equals("com.miniclip.eightballpool")){
                        Field mLaunchCount = null;
                        try {
                            mLaunchCount=UsageStats.class.getDeclaredField("mLaunchCount");

                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                        int launchCount = 0;
                        try {
                            launchCount = (Integer)mLaunchCount.get(usage);
                            //    Log.d("Eightball", "Count: " + launchCount);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        //  Log.d("Countcheck", usageStatsList)

                        // }
                        item.lastStartup = usage.getLastTimeUsed();
                        item.fgTime = usage.getTotalTimeInForeground();
                        //          item.lastStartupStr = getString(R.string.apps_usage_last_startup,
                        //                 DateTimeUtils.generateFileName(item.lastStartup));
                        //          item.fgTime = usage.getTotalTimeInForeground();
                        //         item.fgTimeStr = getString(R.string.apps_usage_fg_time_used,
                        //                 DateTimeUtils.getReadableTimeUsage(item.fgTime));
                        item.appName = item.pkgName;
                        String userid = LoginActivity.userid;
                        //Log.d("Count","" + userid);
                        //sendData(userid,item.appName, item.currenttime, item.firsttime,
                        //item.lastime, item.lastStartup, item.fgTime);
                        //sendData(userid,item.appName, System.currentTimeMillis(), usage.getFirstTimeStamp(),
                        //usage.getLastTimeStamp(), item.lastStartup, item.fgTime);


                        try {
                            item.appName = pm.getApplicationInfo(item.pkgName, 0).loadLabel(pm).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        //saveResults(item);
                        //getResults();
                        results.add(item);
                        //saveResults(results);
                        //*saveResults(userid, item.appName, item.currenttime, item.firsttime,
                        //.lastime, item.lastStartup, item.fgTime);*//*
                        //Log.d("test for results", "" +item.pkgName);
                    }
                    saveResults(results);
                    //getResults();
                    Collections.sort(results, new AppsUsageItem.AppNameComparator());

                    Log.d("Count",""+count);
                    Log.d("test", "" + results);
                    //UStats.printCurrentUsageStatus(displaydata.this);
                }
            }, 0, 5000);//put here time 5000 milliseconds=5 second*/
            stopSelf();
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       if(!this.isRunning) {
            this.isRunning = true;
            thread.start();
        }
        //test();
        getResults();
        showNotification();
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        Log.d("On alarm", "test two");

        return START_STICKY;
    }

    void test(Intent intent) {
        Log.d("alaram test", "");
        //getResults();
    }
    void saveResults(List results) {

        sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(results);

        editor = sharedPreferences.edit();
        editor.putString("AppsUsageItem", String.valueOf(json));
        editor.commit();
        //Log.d("test for shared", String.valueOf(json));
        //getResults();
    }

    void getResults() {

        Gson gson = new Gson();
        sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);

        String jsonfile = sharedPreferences.getString("AppsUsageItem", "");
        List<AppsUsageItem> results = gson.fromJson(jsonfile, new TypeToken<List<AppsUsageItem>>(){}.getType());

        for (int i =0; i<results.size(); i++) {
            AppsUsageItem item = new AppsUsageItem();
            String userid = LoginActivity.userid;
            item.appName = results.get(i).pkgName;
            item.firsttime = results.get(i).firsttime;
            item.lastime = results.get(i).lastime;
            item.lastStartup = results.get(i).lastStartup;
            item.fgTime = results.get(i).fgTime;
            item.mLaunchCount = results.get(i).mLaunchCount;
            String appid = item.appName;
            long start = item.firsttime;
            long end = item.lastime;
            long last = item.lastStartup;
            long total = item.fgTime;
            int count = item.mLaunchCount;
            /*Log.d("test for shared", String.valueOf(time));
            Log.d("test for shared", String.valueOf(appid));*/
            sendData(userid,appid, start, end,last, total, count);
        }

    }


     void sendData(String userid, String appid,  long start, long end, long last, long total, int launch) {

         String url = "http://utelem.jaradshelton.com/post_android.php";
         final String requestBody = "UserID=" + userid+ "&AppID=" + appid +
                 "&StartTime=" + start + "&EndTime=" + end + "&LastTime=" + last + "&TotalTime=" + total
         + "&Launch=" + launch;


         MySingleton volley = MySingleton.getInstance(getApplicationContext());
         mRequestQueue = volley.getRequestQueue();
// Request a string response from the provided URL.
         StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
             @Override
             public void onResponse(String response) {
                 Log.i("VOLLEY", response);

             }
         }, new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError error) {
                 Log.e("VOLLEY", error.toString());
             }
         }) {
             @Override
             public String getBodyContentType() {
                 return "application/x-www-form-urlencoded; charset=utf-8";
             }

             @Override
             public byte[] getBody() throws AuthFailureError {
                 try {
                     return requestBody == null ? null : requestBody.getBytes("utf-8");
                 } catch (UnsupportedEncodingException uee) {
                     VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                     return null;
                 }
             }

             @Override
             protected Response<String> parseNetworkResponse(NetworkResponse response) {
                 String responseString = "";
                 if (response != null) {
                     responseString = String.valueOf(response.statusCode);
                     // can get more details such as response.headers
                 }
                 return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
             }
         };
         mRequestQueue.add(stringRequest);
     }



    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        this.isRunning = false;
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        showNotification();
        Toast.makeText(this, "Has stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, displaydata.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}