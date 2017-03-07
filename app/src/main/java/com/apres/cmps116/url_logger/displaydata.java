package com.apres.cmps116.url_logger;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
                        List<UsageStats> usageStatsList = UStats.getUsageStatsList(displaydata.this);
                       // statslist.removeAllViews();
                        int count=0;
                        for (UsageStats u : usageStatsList){
                            if (u.getTotalTimeInForeground()!=0){
                                //TextView tv = new TextView(displaydata.this);
                                Log.d(u.getPackageName(),"First:"+u.getFirstTimeStamp()+
                                        " Last:"+u.getLastTimeStamp()+" Total:" + u.getTotalTimeInForeground());
                                //tv.setText(u.getPackageName() + ":\t" + u.getTotalTimeInForeground());
                               // statslist.addView(tv);
                                String userid = LoginActivity.userid;
                                String appid = u.getPackageName();
                                long time = System.currentTimeMillis();
                                long start = u.getFirstTimeStamp();
                                long end = u.getLastTimeStamp();
                                long last = u.getLastTimeUsed();
                                long total = u.getTotalTimeInForeground();
                                sendData(userid, appid, time, start, end, last, total);
                                count++;
                            }
                        }
                        Log.d("Count",""+count);
                        //UStats.printCurrentUsageStatus(displaydata.this);
                            }
                        }, 0, 5000);//put here time 5000 milliseconds=5 second
                    }
                });




    }

    void sendData(String userid, String appid, long timestamp, long start, long end, long last, long total){
        String url = "http://sample-env.zssmubuwik.us-west-1.elasticbeanstalk.com/post_android.php";
        final String requestBody = "UserID=" + userid+ "&AppID=" + appid + "&Timestamp=" + timestamp + "&StartTime=" + start + "&EndTime=" + end + "&LastTime=" + last + "&TotalTime=" + total;


        MySingleton volley = MySingleton.getInstance(getApplicationContext());
        mRequestQueue = volley.getRequestQueue();
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("VOLLEY", response);
               // Intent intent = new Intent(LoginActivity.this, displaydata.class);
               // startActivity(intent);
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
