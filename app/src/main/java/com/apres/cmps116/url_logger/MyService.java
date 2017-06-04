package com.apres.cmps116.url_logger;

/**
 * Created by cedriclinares on 3/1/17.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.lang.reflect.Field;
import com.android.volley.RequestQueue;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

import java.util.List;

public class MyService extends Service {

    private NotificationManager mNM;
    private RequestQueue mRequestQueue;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    Timer timer = new Timer();
    Compare[] statArray = new Compare[2000];
    List<Compare> statsList = new ArrayList<Compare>();
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

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Log.d("OnCreate", "Before Notification");
        // Display a notification about us starting.  We put an icon in the status bar.
        Log.d("OnCreate", "After Notification");
    }

    Compare[] initializeStats(Map<String, UsageStats> usageStats){

        int i =0;
        for (UsageStats usage : usageStats.values()) {

            String name = "";
            PackageManager pm = getPackageManager();
            try {
                name = pm.getApplicationInfo(usage.getPackageName(), 0).loadLabel(pm).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            statArray[i] = new Compare();
            statArray[i].packageName = usage.getPackageName();
            statArray[i].appName = name;
            if (statArray[i].packageName.equals("com.apres.cmps116.url_logger")) {
                statArray[i].open = true;
                statArray[i].openTime = System.currentTimeMillis();
            }
            else{
                statArray[i].open = false;
                statArray[i].openTime = 0;

            }
            statArray[i].last = usage.getLastTimeUsed();
            statArray[i].closeTime = 0;
            i++;
        }
        return statArray;
    }

    int appExists(Map<String, UsageStats> usageStats, String name){
        for(int i=0; statArray[i] != null; i++) {
            if (statArray[i].packageName.equals(name)) {
                Log.d("Crash site", String.valueOf(i));
                return i;
            }
        }
        return -1;
    }

    int getLaunchCount(UsageStats usage){
        Field mLaunchCount = null; //Getting launch count
        try {
            mLaunchCount=UsageStats.class.getDeclaredField("mLaunchCount");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        int launchCount = 0;
        try {
            launchCount = (Integer)mLaunchCount.get(usage);//Getting launch count

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return launchCount;
    }

    void compareFunc(UsageStats usage, int i, int launchCount){
        if (!usage.getPackageName().equals("com.sec.android.app.launcher")) {
            if (usage.getLastTimeUsed() > statArray[i].last) { //If lastTimeUsed is different
                if (!statArray[i].open) {  //if app is closed
                    String tempName = statArray[i].appName;
                    String tempPkg = statArray[i].packageName;
                    statArray[i] = new Compare(); //Need to create new instance to fix list bug
                    statArray[i].appName = tempName;
                    statArray[i].packageName = tempPkg;
                    statArray[i].last = usage.getLastTimeUsed();
                    statArray[i].open = true;
                    statArray[i].openTime = usage.getLastTimeUsed();
                } else { //If app is already opened

                    statArray[i].last = usage.getLastTimeUsed();
                    statArray[i].open = false;
                    statArray[i].closeTime = usage.getLastTimeUsed();
                    statArray[i].launch = launchCount;
                    statsList.add(statArray[i]);
                    if (statsList.size() == 3) {
                        sendData(statsList);
                    }
                }
            }
        }
    }

    void analyzeData(Map<String, UsageStats> usageStats){

        for (UsageStats usage : usageStats.values()) {

            int i = appExists(usageStats, usage.getPackageName());
            if (i != -1) {
                    compareFunc(usage, i, getLaunchCount(usage));
            }
            else{
                String name = "";
                PackageManager pm = getPackageManager();
                try {
                    name = pm.getApplicationInfo(usage.getPackageName(), 0).loadLabel(pm).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Compare temp = new Compare();
                temp.packageName=usage.getPackageName();
                temp.appName = name;
                temp.last= usage.getLastTimeUsed();
                statArray[usageStats.size()-1] = temp;
                i = appExists(usageStats, usage.getPackageName());
                compareFunc(usage, i, getLaunchCount(usage));
            }
        }
    }

    void CollectData(){
            final Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
            Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
            initializeStats(usageStatsList);

            timer.scheduleAtFixedRate(new TimerTask() { //timer to capture data every 5 seconds
                    @Override
                    public void run() {
                        Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
                        analyzeData(usageStatsList);
                    }
            }, 0, 1000 );//put here time 1000 milliseconds=1 second
    }

    @Override
    //When background service starts
    public int onStartCommand(Intent intent, int flags, int startId) {
     Log.d("Start Command", "Inside");

        showNotification();
        Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
        CollectData();
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        //Log.d("On alarm", "test two");

        return START_STICKY_COMPATIBILITY; //Continues running when user leaves
    }

    String concatData(final List<Compare> results){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dataString = "";

        for (int i =0; i<results.size(); i++) {
            String userid = LoginActivity.userid;
            String appid = results.get(i).packageName;
            String appName = results.get(i).appName;
            String start =simpleDateFormat.format(results.get(i).openTime);
            Log.d("test", start);
            String end = simpleDateFormat.format(results.get(i).closeTime);
            long diff = (results.get(i).closeTime - results.get(i).openTime)/1000;
            Log.d("end", end);
            int launch = results.get(i).launch;
            dataString = dataString + "&UserID[]=" + userid + "&AppID[]=" + appid + "&AppName[]=" + appName +"&StartTime[]=" + start
                    + "&EndTime[]=" + end + "&TotalTime[]=" + diff + "&Launch[]=" + launch;
        }
        results.clear();
        return dataString;

    }

    void sendData(List<Compare> results){
        String url = "http://sample-env.zssmubuwik.us-west-1.elasticbeanstalk.com/post_android.php";
        final String requestBody = concatData(results);

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
        sendData(statsList);
        mNM.cancel(NOTIFICATION);
        timer.cancel();
        timer.purge();

        //Tell the user we stopped.
        Toast.makeText(this, "Service has stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
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