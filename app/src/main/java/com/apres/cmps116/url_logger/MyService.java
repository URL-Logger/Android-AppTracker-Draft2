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

   Timer mTimer = new Timer();


    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }


    @Override
    public void onCreate() {

        if (UStats.getUsageStatsList(this).isEmpty()){ //Check if permissions are granted
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
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
    }

    private Runnable mytask = new Runnable() {
        @Override
        public void run() {

            mTimer.scheduleAtFixedRate(new TimerTask() { //timer to capture data every 5 seconds
                @Override
                public void run() {
                    Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this); //get the usageStats
                    int count=0;
                    List<AppsUsageItem> results = new ArrayList<AppsUsageItem>();
                    PackageManager pm = getPackageManager();
                    for (UsageStats usage : usageStatsList.values()) { //extract data from each app in usageStats
                        AppsUsageItem item = new AppsUsageItem();
                        item.pkgName = usage.getPackageName();
                        item.firsttime = usage.getFirstTimeStamp();
                        item.lastime = usage.getLastTimeUsed();
                        item.currenttime = System.currentTimeMillis();
                        Field mLaunchCount = null;
                        try {
                            mLaunchCount=UsageStats.class.getDeclaredField("mLaunchCount");

                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                        item.lastStartup = usage.getLastTimeUsed();
                        item.fgTime = usage.getTotalTimeInForeground();
                        item.appName = item.pkgName;

                        try {
                            item.appName = pm.getApplicationInfo(item.pkgName, 0).loadLabel(pm).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        results.add(item);
                    }
                    saveResults(results); //buffer results
                    Collections.sort(results, new AppsUsageItem.AppNameComparator()); //sort buffer

                    Log.d("Count",""+count);
                    Log.d("test", "" + results);
                }
            }, 0, 5000);//put here time 5000 milliseconds=5 second*/
        }
    };

    @Override
    //When background service starts
    public int onStartCommand(Intent intent, int flags, int startId) {
       if(!this.isRunning) {
            this.isRunning = true;
            thread.start();//start the task (collect data)
        }
        getResults(); //get saved results
        showNotification();
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        Log.d("On alarm", "test two");

        return START_STICKY; //Continues running when user leaves
    }

    void test(Intent intent) {
        Log.d("alaram test", "");
        //getResults();
    }

    void saveResults(List results) { //buffer app results

        sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(results);

        editor = sharedPreferences.edit();
        editor.putString("AppsUsageItem", String.valueOf(json));
        editor.commit();
        //Log.d("test for shared", String.valueOf(json));
        //getResults();
    }

    void getResults() { //Get results back

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
            sendData(userid,appid, start, end,last, total, count); //send data to database
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
         mRequestQueue.add(stringRequest);//add request to queue
     }



    @Override
    public void onDestroy() { //Destroys background function
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        mTimer.cancel();
        mTimer.purge();
        this.isRunning = false;
        this.thread.interrupt();

        //Tell the user we stopped.
        Toast.makeText(this, "Service has stopped", Toast.LENGTH_SHORT).show();
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
    private void showNotification() { //details for notification

        CharSequence text = getText(R.string.local_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, displaydata.class), 0);

        // Set info for the views that show in the notification panel.
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