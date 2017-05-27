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
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
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
import java.util.concurrent.TimeUnit;

public class MyService extends Service {

    private NotificationManager mNM;
    private RequestQueue mRequestQueue;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    Timer timer = new Timer();
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

    void initializeStats(Map<String, UsageStats> usageStats , Compare[] statArray){
        int i =0;
        for (UsageStats usage : usageStats.values()) {
            statArray[i] = new Compare();
            statArray[i].appName = usage.getPackageName();
            statArray[i].last = usage.getLastTimeUsed();
           // if (usage.getPackageName().equals("com.apres.cmps116.url_logger"))
            //   {statArray[i].open = true;}
            //else
            {statArray[i].open = false;}
            statArray[i].openTime = 0;
            statArray[i].closeTime = 0;
            i++;
        }
    }

    void analyzeData(Map<String, UsageStats> usageStats , Compare[] statArray, List<Compare> statsList){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        for (UsageStats usage : usageStats.values()) {
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

            for(int i =0; i < usageStats.size()-1;i++){
                if(statArray[i].appName.equals(usage.getPackageName())){//(statArray[i].appName)){
                    if (usage.getLastTimeUsed() != statArray[i].last){ //If lastTimeUsed is different
                        if(!statArray[i].open){  //If app is already opened
                            statArray[i].last = usage.getLastTimeUsed();
                            statArray[i].open = true;
                            statArray[i].openTime = usage.getLastTimeUsed();
                        }
                        else{
                            statArray[i].last = usage.getLastTimeUsed();
                            statArray[i].open = false;
                            statArray[i].closeTime = usage.getLastTimeUsed();
                            statArray[i].launch = launchCount;
                            statsList.add(statArray[i]);
                            if (statsList.size() > 120)
                                {sendData(statsList);}
                        }
                    }

                }
            }
        }
    }

    void CollectData(final Compare[] Stats){
            final Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
          //  final List<AppsUsageItem> results = new ArrayList<AppsUsageItem>();
            final List<Compare> statsList = new List<Compare>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<Compare> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(Compare compare) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends Compare> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends Compare> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public Compare get(int index) {
                return null;
            }

            @Override
            public Compare set(int index, Compare element) {
                return null;
            }

            @Override
            public void add(int index, Compare element) {

            }

            @Override
            public Compare remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<Compare> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<Compare> listIterator(int index) {
                return null;
            }

            @NonNull
            @Override
            public List<Compare> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
            timer.scheduleAtFixedRate(new TimerTask() { //timer to capture data every 5 seconds
                    @Override
                    public void run() {
                        Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);

                        analyzeData(usageStatsList, Stats, statsList);
                    }
            }, 0, 1000 );//put here time 5000 milliseconds=5 second*/

    }


    @Override
    //When background service starts
    public int onStartCommand(Intent intent, int flags, int startId) {
     Log.d("Start Command", "Inside");

        showNotification();
        Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(MyService.this);
        final Compare[] Stats = new Compare[usageStatsList.size()];
        initializeStats(usageStatsList, Stats);
        CollectData(Stats);

        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        Log.d("On alarm", "test two");

        return START_STICKY_COMPATIBILITY; //Continues running when user leaves
    }

    String concatData(final List<Compare> results){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dataString = "";

        for (int i =0; i<results.size(); i++) {
            String userid = LoginActivity.userid;
            String appid = results.get(i).appName;
            String start =simpleDateFormat.format(results.get(i).openTime);
            Log.d("test", start);
            String end = simpleDateFormat.format(results.get(i).closeTime);
            long diff = (results.get(i).closeTime - results.get(i).openTime)/1000;
          //  String last = simpleDateFormat.format(results.get(i).lastime);
            Log.d("end", end);
           // long total = TimeUnit.MILLISECONDS.toSeconds(results.get(i).fgTime);
            int launch = results.get(i).launch;
            dataString = dataString + "&UserID[]=" + userid + "&AppID[]=" + appid +"&StartTime[]=" + start
                    + "&EndTime[]=" + end + "&LastTime[]=" +
                    0 + "&TotalTime[]=" + diff + "&Launch[]=" + launch;
        }
        results.clear();
        return dataString;

    }

    void sendData(final List<Compare> results){
        String userid = LoginActivity.userid;
        String url = "http://sample-env.zssmubuwik.us-west-1.elasticbeanstalk.com/post_android.php?temp";
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