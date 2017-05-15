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
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.ArrayList;
import android.content.pm.PackageManager;
import java.lang.reflect.Field;
import java.util.Collections;
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {


    private NotificationManager mNM;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private RequestQueue mRequestQueue;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    Timer timer = new Timer();
    int tickCount;
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

    void CollectData(){

        tickCount=0;
            timer.scheduleAtFixedRate(new TimerTask() { //timer to capture data every 5 seconds
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
                            int launchCount = 0;
                            try {
                                launchCount = (Integer)mLaunchCount.get(usage);

                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            item.mLaunchCount = launchCount;
                            item.lastStartup = System.currentTimeMillis();
                            item.fgTime = usage.getTotalTimeInForeground();
                            item.appName = item.pkgName;

                            try {
                                item.appName = pm.getApplicationInfo(item.pkgName, 0).loadLabel(pm).toString();
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            results.add(item);
                           /* saveResults(results);
                            break;*/
                        }
                        saveResults(results); //buffer results
                        Collections.sort(results, new AppsUsageItem.AppNameComparator()); //sort buffer

                        Log.d("Count",""+count);
                        Log.d("test", "" + results);
                        tickCount++;
                        if (tickCount == 4){
                            tickCount=0;
                            getResults();
                        }
                    }
            }, 0, 5000 );//put here time 5000 milliseconds=5 second*/

    }


    @Override
    //When background service starts
    public int onStartCommand(Intent intent, int flags, int startId) {
     Log.d("Start Command", "Inside");

        showNotification();
        CollectData();

        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        Log.d("On alarm", "test two");

        return START_STICKY_COMPATIBILITY; //Continues running when user leaves
    }

    void test(Intent intent) {
        Log.d("alaram test", "");
    }

    void saveResults(List results) { //buffer app results

        sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(results);
        Log.d("Json test", json);
        editor = sharedPreferences.edit();
        editor.putString("AppsUsageItem", String.valueOf(json));
        editor.commit();
    }

    void getResults() { //Get results back

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Gson gson = new Gson();
        sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
       // editor = sharedPreferences.edit();


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
            //String start = dateFormat.format(item.firsttime);
            String start =simpleDateFormat.format(item.firsttime);
            Log.d("test", start);
            String end = simpleDateFormat.format(item.lastStartup);
            String last = simpleDateFormat.format(item.lastime);
            Log.d("test", last);
            long total = TimeUnit.MILLISECONDS.toSeconds(item.fgTime);
            int count = item.mLaunchCount;
            sendData(userid,appid, start, end,last, total, count); //send data to database
        }
        editor.clear();
        editor.commit();

    }
    
     void sendData(String userid, String appid,  String start, String end,String last, long total, int launch) {

         String url = "http://sample-env.zssmubuwik.us-west-1.elasticbeanstalk.com/post_android.php";
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