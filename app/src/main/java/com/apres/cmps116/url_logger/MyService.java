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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
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

import static com.apres.cmps116.url_logger.displaydata.statsBtn;

public class MyService extends Service {

    static NotificationManager mNM;
    private RequestQueue mRequestQueue;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    Timer timer = new Timer();
    Compare[] statArray = new Compare[5000];
    List<Compare> statsList = new ArrayList<>();
    public SharedPreferences loginSettings;
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Prevent crashes
            startActivity(intent);
        }
        Log.d("OnCreate", "Started Service");

        loginSettings = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

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
                name = pm.getApplicationInfo(usage.getPackageName(), 0).loadLabel(pm).toString(); //Get app name
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            statArray[i] = new Compare(); //Initialize all Compare objects
            statArray[i].packageName = usage.getPackageName();
            statArray[i].appName = name;
            if (statArray[i].packageName.equals("com.apres.cmps116.url_logger")) { //Only app that is opened when service is started
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

    int appExists(String name){
        for(int i=0; statArray[i] != null; ++i) {
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

        if (!usage.getPackageName().equals("com.sec.android.app.launcher")) { //Launcher used everytime app is opened
            if (usage.getLastTimeUsed() > statArray[i].last) { //If lastTimeUsed is different
                if (!statArray[i].open) {  //if app is closed
                    String tempName = statArray[i].appName;
                    String tempPkg = statArray[i].packageName;
                    statArray[i] = new Compare(); //Need to create new instance to fix list bug
                    statArray[i].appName = tempName;
                    statArray[i].packageName = tempPkg;
                    statArray[i].last = usage.getLastTimeUsed(); //update last time used
                    statArray[i].open = true;
                    statArray[i].openTime = usage.getLastTimeUsed();
                } else { //If app is already opened

                    statArray[i].last = usage.getLastTimeUsed();
                    statArray[i].open = false;
                    statArray[i].closeTime = usage.getLastTimeUsed();
                    statArray[i].launch = launchCount;
                    statsList.add(statArray[i]);
                    if (statsList.size() == 3) { //if buffer is full
                        sendData(statsList);
                    }
                }
            }
        }
    }

    void analyzeData(Map<String, UsageStats> usageStats){

        for (UsageStats usage : usageStats.values()) {

            int i = appExists(usage.getPackageName()); //Check if app was added to statArray
            if (i != -1) {
                    compareFunc(usage, i, getLaunchCount(usage)); //if true
            }
            else{ //add it to statArray
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
                i = appExists(usage.getPackageName()); //check again to get new index
                compareFunc(usage, i, getLaunchCount(usage));
            }
        }
    }

    void CollectData(){

            Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
            initializeStats(usageStatsList);

            timer.scheduleAtFixedRate(new TimerTask() { //timer to capture data every 5 seconds
                    @Override
                    public void run() {
                        Calendar cal = Calendar.getInstance();
                        int hour = cal.get(Calendar.HOUR_OF_DAY);//check hour
                        int min = cal.get(Calendar.MINUTE);
                        int sec = cal.get(Calendar.SECOND);
                        if (hour == 12 && min == 0 && sec ==0 && statsList.size()!=0 ) //Send data once a day
                            {sendData(statsList);}
                        Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
                        analyzeData(usageStatsList);
                    }
            }, 0, 1000 );//put here time 1000 milliseconds=1 second
    }

    @Override
    //When background service starts
    public int onStartCommand(Intent intent, int flags, int startId) { //When process starts
     Log.d("Start Command", "Inside");

        showNotification();
        CollectData();
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        //Log.d("On alarm", "test two");

        return START_STICKY_COMPATIBILITY; //Continues running when user leaves
    }

    String concatData(final List<Compare> results){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dataString = "";

        for (int i =0; i<results.size(); i++) {
            String userid = loginSettings.getString("userid", null);
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
        if (statsList.size() > 0)
            {sendData(statsList);} //Send whats in buffer before destorying process
        mNM.cancel(NOTIFICATION); //Stop notification
        timer.cancel(); //stop timer
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