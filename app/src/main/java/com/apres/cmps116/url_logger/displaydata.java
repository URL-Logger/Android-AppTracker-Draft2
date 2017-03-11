package com.apres.cmps116.url_logger;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.apres.cmps116.url_logger.LoginActivity.userid;

/**
 * Created by cedriclinares on 2/18/17.
 */

public class displaydata extends AppCompatActivity {

    Button statsBtn;
    LinearLayout statslist;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_data);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        statslist = (LinearLayout) findViewById(R.id.statslayout);

        if (UStats.getUsageStatsList(this).isEmpty()){
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        startService(new Intent(displaydata.this, MyService.class));




                statsBtn = (Button) findViewById(R.id.stats_btn);
                statsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                        Map<String, UsageStats> usageStatsList = UStats.getUsageStatsList(displaydata.this);
                       // statslist.removeAllViews();
                        int count=0;
                                List<AppsUsageItem> results = new ArrayList<AppsUsageItem>();
                                PackageManager pm = getPackageManager();
                                for (UsageStats usage : usageStatsList.values()) {
                                    AppsUsageItem item = new AppsUsageItem();
                                    item.pkgName = usage.getPackageName();
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
                                    sendData(userid,item.appName, System.currentTimeMillis(), usage.getFirstTimeStamp(),
                                            usage.getLastTimeStamp(), item.lastStartup, item.fgTime);
                                    try {
                                        item.appName = pm.getApplicationInfo(item.pkgName, 0).loadLabel(pm).toString();
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    results.add(item);
                                }
                                Collections.sort(results, new AppsUsageItem.AppNameComparator());

                        Log.d("Count",""+count);
                        //UStats.printCurrentUsageStatus(displaydata.this);
                            }
                        }, 0, 5000);//put here time 5000 milliseconds=5 second
                    }
                });




    }

    void sendData(String userid, String appid, long timestamp, long start, long end, long last, long total){
        String url = "http://sample-env.zssmubuwik.us-west-1.elasticbeanstalk.com/post_android.php";
        final String requestBody = "UserID=" + userid+ "&AppID=" + appid + "&Timestamp=" + timestamp +
                "&StartTime=" + start + "&EndTime=" + end + "&LastTime=" + last + "&TotalTime=" + total;
        //+ "&Launch=" + launch;


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

}
