package com.apres.cmps116.url_logger;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ToggleButton;

/**
 * Created by cedriclinares on 2/18/17.
 */

public class displaydata extends AppCompatActivity {

    ToggleButton statsBtn;
    public static final String PREFRENCES_FILE_NAME = "MyAppPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_data);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final Intent serviceIntent = new Intent (displaydata.this, MyService.class);
        statsBtn = (ToggleButton) findViewById(R.id.stats_btn);
        statsBtn.setOnClickListener(new View.OnClickListener() { //Logic for toggle button
            @Override
            public void onClick(View v) {
                if (statsBtn.isChecked()){ //if On
                    statsBtn.setTextOn("On");
                    statsBtn.setChecked(true);
                    startService(serviceIntent);
                }
                else{ //if Off
                    statsBtn.setTextOff("Off");
                    statsBtn.setChecked(false);
                    stopService(serviceIntent);
                }
            }
        });
    }

}


