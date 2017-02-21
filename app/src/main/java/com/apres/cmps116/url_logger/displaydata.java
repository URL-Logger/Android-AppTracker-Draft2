package com.apres.cmps116.url_logger;

import android.app.Activity;
import android.app.usage.UsageStats;
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

import java.util.List;


/**
 * Created by cedriclinares on 2/18/17.
 */

public class displaydata extends AppCompatActivity {

    Button statsBtn;
    LinearLayout statslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_data);

        ActionBar  actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        statslist = (LinearLayout) findViewById(R.id.statslayout);

        if (UStats.getUsageStatsList(this).isEmpty()){
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        statsBtn = (Button) findViewById(R.id.stats_btn);
        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UsageStats> usageStatsList = UStats.getUsageStatsList(displaydata.this);
                statslist.removeAllViews();
                for (UsageStats u : usageStatsList){
                    if (u.getTotalTimeInForeground()!=0){
                        TextView tv = new TextView(displaydata.this);
                        tv.setText(u.getPackageName() + ":\t" + u.getTotalTimeInForeground());
                        statslist.addView(tv);
                    }
                }
                //UStats.printCurrentUsageStatus(displaydata.this);

            }
        });

    }

}
